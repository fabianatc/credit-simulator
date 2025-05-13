package com.creditsimulator.application.worker;

import com.creditsimulator.application.message.QueuedSimulationMessage;
import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
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
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncUploadWorker {

    private final SimulationJobPersistencePort jobPersistence;
    private final SimulationMessageQueuePort messageQueuePort;
    private final SimulationResultPersistencePort resultPersistence;
    private final ObjectMapper objectMapper;

    private static final int MAX_LINES = 10000;

    @Async("asyncExecutor")
    public void processCsvAsync(MultipartFile file, CreditSimulationJob job) {
        UUID jobId = job.id();
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVFormat myFormat = CSVFormat.DEFAULT.builder()
                .setIgnoreEmptyLines(true)
                .setIgnoreSurroundingSpaces(true)
                .setTrim(true)
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

            CSVParser parser = myFormat.parse(reader);
            headerValidation(parser);

            for (CSVRecord record : parser) {
                if (record.stream().allMatch(String::isBlank)) {
                    continue;
                }

                lineCount++;
                if (lineCount > MAX_LINES) {
                    log.warn("[UPLOAD][{}] Max lines exceeded ({} lines)", jobId, MAX_LINES);
                    break;
                }

                try {
                    var message = parseLineToMessage(record, jobId);
                    messageQueuePort.enqueue(message);

                } catch (Exception ex) {
                    log.error("[UPLOAD][{}] Error parsing line {}: {}", jobId, lineCount, ex.getMessage());

                    try {
                        String inputJson = objectMapper.writeValueAsString(record.toMap());
                        var result = new CreditSimulationResult(
                            UUID.randomUUID(),
                            jobId,
                            inputJson,
                            null,
                            SimulationResultStatus.ERROR,
                            ex.getMessage(),
                            LocalDateTime.now()
                        );
                        resultPersistence.save(result);
                    } catch (Exception e2) {
                        log.error("[UPLOAD][{}] Failed to persist error result: {}", jobId, e2.getMessage());
                    }
                }
            }

            var updatedJob = job.withStatus(SimulationJobStatus.PROCESSING)
                .withTotalSimulations(lineCount);
            jobPersistence.save(updatedJob);

            log.info("[UPLOAD][{}] Finished async upload. {} simulations enqueued", jobId, lineCount);

        } catch (Exception ex) {
            log.error("[UPLOAD][{}] Failed to process CSV file asynchronously: {}", jobId, ex.getMessage());
            jobPersistence.save(job.withStatus(SimulationJobStatus.ERROR));
        }
    }

    private void headerValidation(CSVParser parser) {
        // ✅ Header validation
        List<String> expectedHeaders = List.of("creditAmount", "termInMonths", "birthDate", "taxType", "fixedTax", "currency");
        List<String> actualHeaders = parser.getHeaderNames(); // ✅ Sem NPE
        if (!new HashSet<>(actualHeaders).containsAll(expectedHeaders)) {
            throw new IllegalArgumentException("The CSV file must include all required headers: " + expectedHeaders);
        }
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

