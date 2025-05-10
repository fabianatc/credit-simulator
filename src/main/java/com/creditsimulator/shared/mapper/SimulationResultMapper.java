package com.creditsimulator.shared.mapper;

import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.infra.persistence.entity.SimulationJobEntity;
import com.creditsimulator.infra.persistence.entity.SimulationResultEntity;
import com.creditsimulator.shared.enums.SimulationResultStatus;

public class SimulationResultMapper {
    public static CreditSimulationResult toModel(SimulationResultEntity entity) {
        return new CreditSimulationResult(
            entity.getId(),
            entity.getJob().getId(),
            entity.getInput(),
            entity.getOutput(),
            SimulationResultStatus.valueOf(entity.getStatus().name()),
            entity.getErrorMessage(),
            entity.getCreatedAt()
        );
    }

    public static SimulationResultEntity toEntity(CreditSimulationResult model) {
        return SimulationResultEntity.builder()
            .id(model.id())
            .job(SimulationJobEntity.builder().id(model.jobId()).build())
            .input(model.input())
            .output(model.output())
            .status(SimulationResultStatus.valueOf(model.status().name()))
            .errorMessage(model.errorMessage())
            .createdAt(model.createdAt())
            .build();
    }
}
