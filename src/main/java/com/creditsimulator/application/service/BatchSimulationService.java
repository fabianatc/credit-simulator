package com.creditsimulator.application.service;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationMessageQueuePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import com.creditsimulator.shared.enums.TaxType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchSimulationService implements BatchSimulationUseCase {
    private final SimulationJobPersistencePort jobPersistence;
    private final SimulationMessageQueuePort messageQueuePort;
    private final SimulationResultPersistencePort resultPersistence;
    private final ObjectMapper objectMapper;
    private static final int MAX_LINES = 10000;

    @Override
    public UUID process(MultipartFile file, String requesterName, String requesterEmail) {
        if (file.isEmpty() || !Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file: must be a non-empty .csv file.");
        }

        UUID jobId = UUID.randomUUID();

        var job = new CreditSimulationJob(
            jobId,
            SimulationJobStatus.PENDING,
            LocalDateTime.now(),
            null,
            0,
            0,
            0,
            requesterName,
            requesterEmail.toLowerCase()
        );

        jobPersistence.save(job);
        log.info("[UPLOAD] Created job {} for requester {} <{}>", jobId, requesterName, requesterEmail);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVFormat myFormat = CSVFormat.DEFAULT.builder()
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setSkipHeaderRecord(true)
                .get();

            int lineCount = 0;

            for (CSVRecord record : myFormat.parse(reader)) {
                lineCount++;
                if (lineCount > MAX_LINES) {
                    throw new IllegalArgumentException("Maximum allowed lines exceeded (10000).");
                }

                try {
                    QueuedSimulationMessage message = parseLineToMessage(record, jobId);
                    messageQueuePort.enqueue(message);
                    log.debug("[UPLOAD][{}] Enqueued simulation line {}: {}", jobId, lineCount, message);
                } catch (Exception ex) {
                    log.error("[UPLOAD][{}] Error parsing/enqueuing line {}: {}", jobId, lineCount, ex.getMessage());

                    try {
                        String inputJson = objectMapper.writeValueAsString(record.toMap());
                        CreditSimulationResult failedResult = new CreditSimulationResult(
                            UUID.randomUUID(),
                            jobId,
                            inputJson,
                            null,
                            SimulationResultStatus.ERROR,
                            ex.getMessage(),
                            LocalDateTime.now()
                        );
                        resultPersistence.save(failedResult);
                        log.warn("[UPLOAD][{}] Saved parsing error result for line {}", jobId, lineCount);
                    } catch (Exception jsonEx) {
                        log.error("[UPLOAD][{}] Failed to persist error result: {}", jobId, jsonEx.getMessage());
                    }
                }
            }

            var updatedJob = new CreditSimulationJob(
                jobId,
                SimulationJobStatus.PROCESSING,
                job.createdAt(),
                null,
                lineCount,
                0,
                0,
                requesterName,
                requesterEmail.toLowerCase()
            );

            jobPersistence.save(job);

            log.info("[UPLOAD][{}] Successfully queued {} simulations", jobId, lineCount);

        } catch (IOException ex) {
            log.error("[UPLOAD][{}] Failed to parse CSV: {}", jobId, ex.getMessage());
            try {
                throw new IOException("Failed to parse CSV file", ex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return jobId;
    }

    private QueuedSimulationMessage parseLineToMessage(CSVRecord record, UUID jobId) {
        return new QueuedSimulationMessage(
            jobId,
            new BigDecimal(record.get("creditAmount")),
            Integer.parseInt(record.get("termInMonths")),
            LocalDate.parse(record.get("birthDate")),
            TaxType.valueOf(record.get("taxType")),
            record.get("fixedTax").isBlank() ? null : new BigDecimal(record.get("fixedTax")),
            record.get("currency")
        );
    }
}
