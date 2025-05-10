package com.creditsimulator.application.controller.batch;

import com.creditsimulator.application.response.BatchSimulationResponse;
import com.creditsimulator.domain.port.incoming.BatchSimulationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Batch Upload", description = "Endpoint for uploading credit simulations in CSV format")
@RequestMapping("/simulations/batch")
public interface CreditSimulationBatchController {
    @Operation(
        summary = "Upload a CSV file with multiple credit simulations",
        description = """
            Upload a `.csv` file containing multiple credit simulation requests.
            The system processes the file asynchronously and returns a job ID for status tracking.
            """,
        requestBody = @RequestBody(
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        ),
        responses = {
            @ApiResponse(
                responseCode = "202",
                description = "Upload accepted. Returns the job ID.",
                content = @Content(schema = @Schema(implementation = BatchSimulationUseCase.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
        }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<BatchSimulationResponse> uploadBatchCsv(
        @Parameter(description = "CSV file with credit simulations", required = true)
        @RequestParam("file") MultipartFile file,

        @Parameter(description = "Requester's full name", example = "Fabiana Costa", required = true)
        @RequestParam("requesterName") String requesterName,

        @Parameter(description = "Requester's email address", example = "fabiana@email.com", required = true)
        @RequestParam("requesterEmail") String requesterEmail
    );
}
