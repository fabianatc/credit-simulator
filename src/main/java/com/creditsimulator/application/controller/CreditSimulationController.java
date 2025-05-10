package com.creditsimulator.application.controller;

import com.creditsimulator.application.request.CreditSimulationRequest;
import com.creditsimulator.application.response.CreditSimulationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Simulation Controller", description = "Endpoints for credit simulation")
@RequestMapping("/simulations")
public interface CreditSimulationController {
    @Operation(
        summary = "Simulate a credit request",
        description = "Simulates a credit based on the amount, customer's birth date and payment term.",
        requestBody = @RequestBody(
            description = "Credit simulation input",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreditSimulationRequest.class)
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Simulation result",
                content = @Content(
                    schema = @Schema(implementation = CreditSimulationResponse.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PostMapping
    ResponseEntity<CreditSimulationResponse> simulate(@Valid @RequestBody CreditSimulationRequest request);
}
