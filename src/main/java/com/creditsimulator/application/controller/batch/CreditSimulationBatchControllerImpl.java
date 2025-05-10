package com.creditsimulator.application.controller.batch;

import com.creditsimulator.application.response.BatchSimulationResponse;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CreditSimulationBatchControllerImpl implements CreditSimulationBatchController {
    private final BatchSimulationUseCase uploadBatchSimulationUseCase;

    @Override
    public ResponseEntity<BatchSimulationResponse> uploadBatchCsv(MultipartFile file, String requesterName, String requesterEmail) {
        UUID jobId = uploadBatchSimulationUseCase.process(file, requesterName, requesterEmail);
        return ResponseEntity.accepted().body(new BatchSimulationResponse(jobId));
    }
}
