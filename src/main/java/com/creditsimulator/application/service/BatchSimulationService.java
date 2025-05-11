package com.creditsimulator.application.service;

import com.creditsimulator.application.worker.AsyncUploadWorker;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationMessageQueuePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchSimulationService implements BatchSimulationUseCase {
    private final SimulationJobPersistencePort jobPersistence;
    private final SimulationMessageQueuePort messageQueuePort;
    private final SimulationResultPersistencePort resultPersistence;
    private final AsyncUploadWorker asyncUploadWorker;
    private final ObjectMapper objectMapper;
    private static final int MAX_LINES = 10000;

    @Override
    public UUID process(MultipartFile file, String requesterName, String requesterEmail) throws IOException {
        if (file.isEmpty() || !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file: must be a non-empty CSV file.");
        }

        UUID jobId = UUID.randomUUID();

        var job = new CreditSimulationJob(
            jobId,
            SimulationJobStatus.PENDING,
            LocalDateTime.now(),
            null,
            0, 0, 0,
            requesterName,
            requesterEmail.toLowerCase()
        );

        jobPersistence.save(job);
        log.info("[UPLOAD] Created job {} for requester {} <{}>", jobId, requesterName, requesterEmail);

        // ✅ Calling async worker
        asyncUploadWorker.processCsvAsync(file, job);

        return jobId;
    }
}
