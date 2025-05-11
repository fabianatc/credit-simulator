package com.creditsimulator.domain.model.simulation;

import com.creditsimulator.shared.enums.SimulationJobStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditSimulationJob(
    UUID id,
    SimulationJobStatus status,
    LocalDateTime createdAt,
    LocalDateTime finishedAt,
    int totalSimulations,
    int successCount,
    int errorCount,
    String requesterName,
    String requesterEmail
) {

    public CreditSimulationJob withSuccessCount(int newSuccessCount) {
        return new CreditSimulationJob(
            id, status, createdAt, finishedAt,
            totalSimulations, newSuccessCount, errorCount,
            requesterName, requesterEmail
        );
    }

    public CreditSimulationJob withTotalSimulations(int newTotalSimulations) {
        return new CreditSimulationJob(
            id, status, createdAt, finishedAt,
            newTotalSimulations, successCount, errorCount,
            requesterName, requesterEmail
        );
    }

    public CreditSimulationJob withErrorCount(int newErrorCount) {
        return new CreditSimulationJob(
            id, status, createdAt, finishedAt,
            totalSimulations, successCount, newErrorCount,
            requesterName, requesterEmail
        );
    }

    public CreditSimulationJob withStatus(SimulationJobStatus newStatus) {
        return new CreditSimulationJob(
            id, newStatus, createdAt, finishedAt,
            totalSimulations, successCount, errorCount,
            requesterName, requesterEmail
        );
    }

    public CreditSimulationJob withFinishedAt(LocalDateTime newFinishedAt) {
        return new CreditSimulationJob(
            id, status, createdAt, newFinishedAt,
            totalSimulations, successCount, errorCount,
            requesterName, requesterEmail
        );
    }

    public CreditSimulationJob withUpdatedStats(int newSuccessCount, int newErrorCount, SimulationJobStatus newStatus) {
        return new CreditSimulationJob(
            id, newStatus, createdAt, finishedAt,
            totalSimulations, newSuccessCount, newErrorCount,
            requesterName, requesterEmail
        );
    }

    public CreditSimulationJob withStatsAndFinished(int newSuccessCount, int newErrorCount, SimulationJobStatus newStatus, LocalDateTime newFinishedAt) {
        return new CreditSimulationJob(
            id, newStatus, createdAt, newFinishedAt,
            totalSimulations, newSuccessCount, newErrorCount,
            requesterName, requesterEmail
        );
    }
}
