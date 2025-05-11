package com.creditsimulator.application.controller.batch;

import com.creditsimulator.application.response.BatchSimulationResponse;
import com.creditsimulator.application.service.BatchSimulationExportService;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreditSimulationBatchControllerImpl implements CreditSimulationBatchController {
    private final BatchSimulationUseCase uploadBatchSimulationUseCase;
    private final BatchSimulationExportService exportService;


    @Override
    public ResponseEntity<BatchSimulationResponse> uploadBatchCsv(MultipartFile file, String requesterName, String requesterEmail) throws IOException {
        UUID jobId = uploadBatchSimulationUseCase.process(file, requesterName, requesterEmail);

        return ResponseEntity.accepted().body(new BatchSimulationResponse(
            jobId,
            "Your batch is being processed. Results will be sent to your email when ready.",
            true,
            "/simulations/batch/" + jobId + "/csv"
        ));
    }

    @Override
    public void exportBatchAsCsv(UUID jobId, HttpServletResponse response) throws IOException {
        var completedJob = exportService.getIfCompleted(jobId);

        if (completedJob.isEmpty()) {
            if (!exportService.jobExists(jobId)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Job not found");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job not completed yet.");
            }
            return;
        }

        exportService.writeCsvResponse(jobId, response);
    }
}
