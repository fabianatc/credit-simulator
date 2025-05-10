package com.creditsimulator.domain.port.outgoing;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.shared.enums.SimulationJobStatus;

import java.util.Optional;
import java.util.UUID;

public interface SimulationJobPersistencePort {
    void save(CreditSimulationJob job);

    Optional<CreditSimulationJob> findById(UUID id);

    void updateStatus(UUID jobId, SimulationJobStatus status);
}
