package com.creditsimulator.domain.port.outgoing;

import com.creditsimulator.domain.model.simulation.CreditSimulationResult;

import java.util.List;
import java.util.UUID;

public interface SimulationResultPersistencePort {

    void save(CreditSimulationResult result);

    List<CreditSimulationResult> findByJobId(UUID jobId);
}
