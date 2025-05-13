package com.creditsimulator.unit;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.application.worker.AsyncUploadWorker;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationMessageQueuePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncUploadWorkerTest {

    @Mock
    private SimulationJobPersistencePort jobPersistence;

    @Mock
    private SimulationMessageQueuePort messageQueuePort;

    @Mock
    private SimulationResultPersistencePort resultPersistence;

    @InjectMocks
    private AsyncUploadWorker worker;

    private final ObjectMapper objectMapper = new ObjectMapper(); // instância real

    private CreditSimulationJob createJob(UUID jobId) {
        return new CreditSimulationJob(
            jobId,
            SimulationJobStatus.PENDING,
            LocalDateTime.now(),
            null, 0, 0, 0,
            "Fabiana Costa",
            "fabiana@email.com"
        );
    }

    @BeforeEach
    void setUp() {
        worker = new AsyncUploadWorker(jobPersistence, messageQueuePort, resultPersistence, objectMapper);
    }

    @Test
    void shouldEnqueueValidCsvLines() throws Exception {
        String csv = """
            creditAmount,termInMonths,birthDate,taxType,fixedTax,currency
            10000,24,1990-05-20,AGE_BASED,,BRL
            5000,12,1995-10-01,AGE_BASED,,USD
            """;

        MultipartFile file = new MockMultipartFile("file", "simulations.csv", "text/csv", csv.getBytes());

        UUID jobId = UUID.randomUUID();
        CreditSimulationJob job = createJob(jobId);

        worker.processCsvAsync(file, job);

        verify(messageQueuePort, times(2)).enqueue(any(QueuedSimulationMessage.class));
        verify(jobPersistence).save(argThat(updatedJob ->
            updatedJob.status() == SimulationJobStatus.PROCESSING &&
                updatedJob.totalSimulations() == 2
        ));
    }

    @Test
    void shouldPersistErrorWhenLineIsInvalid() throws Exception {
        String csv = "creditAmount,termInMonths,birthDate,taxType,fixedTax,currency\n" +
            "invalid,12,1990-05-20,AGE_BASED,,BRL";

        MultipartFile file = new MockMultipartFile("file", "simulations.csv", "text/csv", csv.getBytes());

        UUID jobId = UUID.randomUUID();
        CreditSimulationJob job = createJob(jobId);

        worker.processCsvAsync(file, job);

        verify(resultPersistence).save(argThat(result ->
            result.jobId().equals(jobId) &&
                result.status() == SimulationResultStatus.ERROR &&
                result.errorMessage() != null
        ));

        verify(jobPersistence).save(argThat(updatedJob ->
            updatedJob.status() == SimulationJobStatus.PROCESSING &&
                updatedJob.totalSimulations() == 1
        ));
    }

    @Test
    void shouldMarkJobAsErrorOnFatalFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new RuntimeException("fail"));

        UUID jobId = UUID.randomUUID();
        CreditSimulationJob job = createJob(jobId);

        worker.processCsvAsync(file, job);

        verify(jobPersistence).save(argThat(updatedJob ->
            updatedJob.status() == SimulationJobStatus.ERROR
        ));
    }

    @Test
    void shouldThrowExceptionForInvalidHeader() throws Exception {
        String csv = "wrongHeader1,wrongHeader2\n" +
            "10000,24";

        MultipartFile file = new MockMultipartFile("file", "invalid.csv", "text/csv", csv.getBytes());

        UUID jobId = UUID.randomUUID();
        CreditSimulationJob job = createJob(jobId);

        assertDoesNotThrow(() -> worker.processCsvAsync(file, job)); // exceção capturada internamente

        verify(jobPersistence).save(argThat(updatedJob ->
            updatedJob.status() == SimulationJobStatus.ERROR
        ));
    }
}

