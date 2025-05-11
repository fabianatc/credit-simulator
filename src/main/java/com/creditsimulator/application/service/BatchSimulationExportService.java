package com.creditsimulator.application.service;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
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

    public void writeCsvResponse(UUID jobId, HttpServletResponse response) throws IOException {
        List<CreditSimulationResult> results = resultPersistence.findByJobId(jobId);

        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"resultados-" + jobId + ".csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("id,input,output,status,errorMessage,createdAt");

            for (var r : results) {
                writer.printf("%s,\"%s\",\"%s\",%s,\"%s\",%s%n",
                    r.id(),
                    escape(r.input()),
                    escape(r.output()),
                    r.status().name(),
                    escape(r.errorMessage()),
                    r.createdAt());
            }
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
