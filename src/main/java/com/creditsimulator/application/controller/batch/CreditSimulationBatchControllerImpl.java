package com.creditsimulator.application.controller.batch;

import com.creditsimulator.application.response.BatchSimulationResponse;
import com.creditsimulator.application.service.BatchSimulationExportService;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/simulations/batch")
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
    public ResponseEntity<ByteArrayResource> exportBatchAsCsv(UUID jobId) {
        var completedJob = exportService.getIfCompleted(jobId);

        if (completedJob.isEmpty()) {
            if (!exportService.jobExists(jobId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }

        File file = exportService.writeCsvResponse(jobId);
        try {
            byte[] byteArray = FileUtils.readFileToByteArray(file);
            ByteArrayResource byteArrayResource = new ByteArrayResource(byteArray);
            Files.delete(file.toPath());
            return ResponseEntity.ok()
                .contentLength(byteArray.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(byteArrayResource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
