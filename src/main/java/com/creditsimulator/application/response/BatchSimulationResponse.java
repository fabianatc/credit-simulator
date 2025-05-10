package com.creditsimulator.application.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response after CSV batch request")
public record BatchSimulationResponse(
    @Schema(description = "Batch simulation job identifier", example = "d56fa0a2-d542-4e12-9a73-19e5e3340f3d")
    UUID jobId
) {
}