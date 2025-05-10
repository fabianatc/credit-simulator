package com.creditsimulator.application.service;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.model.simulation.CreditSimulationCalculator;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.outgoing.CurrencyConversionClient;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.enums.SimulationResultStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchSimulationProcessor {

    private final CreditSimulationCalculator calculator;
    private final CurrencyConversionClient currencyClient;
    private final SimulationResultPersistencePort resultPersistence;
    private final SimulationJobPersistencePort jobPersistence;
    private final EmailNotificationService emailService;
    private final ObjectMapper objectMapper;

    public void process(QueuedSimulationMessage message) {
        UUID resultId = UUID.randomUUID();

        try {
            CreditSimulation simulation = calculator.calculate(
                message.creditAmount(),
                message.termInMonths(),
                message.birthDate(),
                message.taxType(),
                message.fixedTax()
            );

            if (message.currency() != null && !"BRL".equalsIgnoreCase(message.currency())) {
                simulation = simulation.convertTo(message.currency(), currencyClient);
            }

            String inputJson = objectMapper.writeValueAsString(message);
            String outputJson = objectMapper.writeValueAsString(simulation);

            var result = new CreditSimulationResult(
                resultId,
                message.jobId(),
                inputJson,
                outputJson,
                SimulationResultStatus.OK,
                null,
                LocalDateTime.now()
            );

            resultPersistence.save(result);
            log.info("[PROCESSOR] Saved success result for job {}", message.jobId());

            updateJobAndCheckFinalization(message.jobId(), true);

        } catch (Exception ex) {
            log.error("[PROCESSOR] Failed to process simulation for job {}: {}", message.jobId(), ex.getMessage());

            try {
                String inputJson = objectMapper.writeValueAsString(message);
                var result = new CreditSimulationResult(
                    resultId,
                    message.jobId(),
                    inputJson,
                    null,
                    SimulationResultStatus.ERROR,
                    ex.getMessage(),
                    LocalDateTime.now()
                );
                resultPersistence.save(result);
                updateJobAndCheckFinalization(message.jobId(), false);
            } catch (Exception e2) {
                log.error("[PROCESSOR] Failed to save failed result: {}", e2.getMessage());
            }
        }
    }

    private void updateJobAndCheckFinalization(UUID jobId, boolean success) {
        jobPersistence.findById(jobId).ifPresent(job -> {
            int updatedSuccess = job.successCount() + (success ? 1 : 0);
            int updatedError = job.errorCount() + (success ? 0 : 1);
            var updatedJob = job.withUpdatedStats(updatedSuccess, updatedError, job.status());
            jobPersistence.save(updatedJob);

            // Check for finalization
            if (updatedSuccess + updatedError == job.totalSimulations()) {
                var finalized = updatedJob.withStatus(SimulationJobStatus.DONE)
                    .withFinishedAt(LocalDateTime.now());
                jobPersistence.save(finalized);

                var results = resultPersistence.findByJobId(jobId);
                emailService.sendJobResults(finalized, results);
                log.info("[PROCESSOR] Job {} finalized. E-mail sent to {}", jobId, job.requesterEmail());
            }
        });
    }
}