package com.creditsimulator.infra.persistence.repository;

import com.creditsimulator.infra.persistence.entity.SimulationJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SimulationJobRepository extends JpaRepository<SimulationJobEntity, UUID> {
}
