package com.creditsimulator.unit;

import com.creditsimulator.application.service.BatchSimulationService;
import com.creditsimulator.application.worker.AsyncUploadWorker;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BatchSimulationServiceTest {

    @Mock
    private SimulationJobPersistencePort jobPersistence;

    @Mock
    private AsyncUploadWorker asyncUploadWorker;

    @InjectMocks
    private BatchSimulationService service;

    private final String requesterName = "Fabiana Costa";
    private final String requesterEmail = "fabiana@email.com";

    @Test
    void shouldThrowExceptionWhenFileIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.process(null, requesterName, requesterEmail);
        });
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() {
        MultipartFile emptyFile = new MockMultipartFile("file", "simulations.csv", "text/csv", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            service.process(emptyFile, requesterName, requesterEmail);
        });
    }

    @Test
    void shouldThrowExceptionWhenFileIsNotCsv() {
        MultipartFile invalidFile = new MockMultipartFile("file", "document.txt", "text/plain", "invalid".getBytes());

        assertThrows(IllegalArgumentException.class, () -> {
            service.process(invalidFile, requesterName, requesterEmail);
        });
    }

    @Test
    void shouldCreateJobAndTriggerAsyncUpload() {
        byte[] content = "creditAmount,termInMonths,birthDate,taxType,fixedTax,currency\n".getBytes();
        MultipartFile validCsv = new MockMultipartFile("file", "simulations.csv", "text/csv", content);

        UUID jobId = service.process(validCsv, requesterName, requesterEmail);

        // Verifica se o job foi salvo
        verify(jobPersistence, times(1)).save(argThat(job ->
            job.id().equals(jobId) &&
                job.status() == SimulationJobStatus.PENDING &&
                job.requesterName().equals(requesterName) &&
                job.requesterEmail().equals(requesterEmail.toLowerCase())
        ));

        // Verifica se o worker foi chamado
        verify(asyncUploadWorker, times(1)).processCsvAsync(eq(validCsv), any(CreditSimulationJob.class));
    }
}