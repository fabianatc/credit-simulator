package com.creditsimulator.application.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response after CSV batch request")
public record BatchSimulationResponse(
    @Schema(description = "Identifier of the created job", example = "1d191431-bc33-4c26-98cd-bbdb6a8a6f12")
    UUID jobId,

    @Schema(description = "User guidance message", example = "Your batch is being processed. Results will be sent by email.")
    String message,

    @Schema(description = "Whether results will be sent by email", example = "true")
    boolean emailNotification,

    @Schema(description = "API endpoint to query the results manually", example = "/simulations/batch/{jobId}/csv")
    String resultUrl
) {
}