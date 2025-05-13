package com.creditsimulator.application.service;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchSimulationExportService {

    private final SimulationJobPersistencePort jobPersistence;
    private final SimulationResultPersistencePort resultPersistence;

    public Optional<CreditSimulationJob> getIfCompleted(UUID jobId) {
        return jobPersistence.findById(jobId)
            .filter(job -> job.status() == SimulationJobStatus.DONE);
    }

    public boolean jobExists(UUID jobId) {
        return jobPersistence.findById(jobId).isPresent();
    }

    public File writeCsvResponse(UUID jobId) {
        List<CreditSimulationResult> results = findByJobId(jobId);

        String fileName = "Results-" + jobId + ".csv";

        try {
            FileWriter fileWriter = new FileWriter(fileName);
            final CSVFormat csvFormat = CSVFormat.DEFAULT;
            final CSVPrinter printer = new CSVPrinter(fileWriter, csvFormat);

            printer.printRecord("id,input,output,status,errorMessage,createdAt");

            for (var r : results) {
                printer.printRecord("%s,\"%s\",\"%s\",%s,\"%s\",%s%n",
                    r.id(),
                    escape(r.input()),
                    escape(r.output()),
                    r.status().name(),
                    escape(r.errorMessage()),
                    r.createdAt());
            }
            fileWriter.flush();
            fileWriter.close();
            printer.close();
        } catch (Exception e) {
            log.error("Error creating file {}", e.getMessage(), e);
            return null;
        }

        return new File(fileName);
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    public List<CreditSimulationResult> findByJobId(UUID jobId) {
        return resultPersistence.findByJobId(jobId);
    }
}
