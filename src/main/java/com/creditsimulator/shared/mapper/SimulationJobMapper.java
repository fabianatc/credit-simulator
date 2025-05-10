package com.creditsimulator.shared.mapper;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.infra.persistence.entity.SimulationJobEntity;
import com.creditsimulator.shared.enums.SimulationJobStatus;

public class SimulationJobMapper {
    public static CreditSimulationJob toModel(SimulationJobEntity entity) {
        return new CreditSimulationJob(
            entity.getId(),
            SimulationJobStatus.valueOf(entity.getStatus().name()),
            entity.getCreatedAt(),
            entity.getFinishedAt(),
            entity.getTotalSimulations(),
            entity.getSuccessCount(),
            entity.getErrorCount(),
            entity.getRequesterName(),
            entity.getRequesterEmail()
        );
    }

    public static SimulationJobEntity toEntity(CreditSimulationJob model) {
        return SimulationJobEntity.builder()
            .id(model.id())
            .status(SimulationJobStatus.valueOf(model.status().name()))
            .createdAt(model.createdAt())
            .finishedAt(model.finishedAt())
            .totalSimulations(model.totalSimulations())
            .successCount(model.successCount())
            .errorCount(model.errorCount())
            .requesterName(model.requesterName())
            .requesterEmail(model.requesterEmail())
            .build();
    }
}
