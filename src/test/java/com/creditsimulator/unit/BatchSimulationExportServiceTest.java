package com.creditsimulator.unit;

import com.creditsimulator.application.service.BatchSimulationExportService;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchSimulationExportServiceTest {

    @Mock
    private SimulationJobPersistencePort jobPersistence;

    @Mock
    private SimulationResultPersistencePort resultPersistence;

    @InjectMocks
    private BatchSimulationExportService service;

    private final UUID jobId = UUID.randomUUID();


    @Test
    void shouldReturnJobIfCompleted() {
        var jobDone = new CreditSimulationJob(jobId, SimulationJobStatus.DONE, null, null, 0, 0, 0, "name", "email");

        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(jobDone));

        Optional<CreditSimulationJob> result = service.getIfCompleted(jobId);

        assertThat(result).isPresent();
        assertThat(result.get().status()).isEqualTo(SimulationJobStatus.DONE);
    }

    @Test
    void shouldReturnEmptyIfJobNotCompleted() {
        var jobPending = new CreditSimulationJob(jobId, SimulationJobStatus.PROCESSING, null, null, 0, 0, 0, "name", "email");

        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(jobPending));

        Optional<CreditSimulationJob> result = service.getIfCompleted(jobId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueIfJobExists() {
        when(jobPersistence.findById(jobId)).thenReturn(Optional.of(mock(CreditSimulationJob.class)));

        assertThat(service.jobExists(jobId)).isTrue();
    }

    @Test
    void shouldReturnFalseIfJobDoesNotExist() {
        when(jobPersistence.findById(jobId)).thenReturn(Optional.empty());

        assertThat(service.jobExists(jobId)).isFalse();
    }

    @Test
    void shouldWriteCsvFileWithResults() throws Exception {
        var result = new CreditSimulationResult(
            UUID.randomUUID(),
            jobId,
            "{\"creditAmount\":10000}",
            "{\"totalAmount\":12345}",
            SimulationResultStatus.OK,
            null,
            LocalDateTime.now()
        );

        when(resultPersistence.findByJobId(jobId)).thenReturn(List.of(result));

        File file = service.writeCsvResponse(jobId);

        assertThat(file).exists();
        assertThat(file.getName()).startsWith("Results-" + jobId);
        assertThat(file.length()).isGreaterThan(0);

        // opcional: apagar após leitura
        file.deleteOnExit();
    }
}