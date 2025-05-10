package com.creditsimulator.domain.model.simulation;

import com.creditsimulator.shared.enums.SimulationResultStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditSimulationResult(
    UUID id,
    UUID jobId,
    String input,
    String output,
    SimulationResultStatus status,
    String errorMessage,
    LocalDateTime createdAt
) {
}
