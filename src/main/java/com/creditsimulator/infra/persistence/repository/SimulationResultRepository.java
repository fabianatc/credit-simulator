package com.creditsimulator.infra.persistence.repository;

import com.creditsimulator.infra.persistence.entity.SimulationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SimulationResultRepository extends JpaRepository<SimulationResultEntity, UUID> {
    List<SimulationResultEntity> findAllByJob_Id(UUID jobId);
}
