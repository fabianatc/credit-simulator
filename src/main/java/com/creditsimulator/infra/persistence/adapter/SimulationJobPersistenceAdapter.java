package com.creditsimulator.infra.persistence.adapter;

import com.creditsimulator.domain.model.simulation.CreditSimulationJob;
import com.creditsimulator.domain.port.outgoing.SimulationJobPersistencePort;
import com.creditsimulator.infra.persistence.repository.SimulationJobRepository;
import com.creditsimulator.shared.enums.SimulationJobStatus;
import com.creditsimulator.shared.mapper.SimulationJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SimulationJobPersistenceAdapter implements SimulationJobPersistencePort {

    private final SimulationJobRepository repository;

    @Override
    public void save(CreditSimulationJob job) {
        repository.save(SimulationJobMapper.toEntity(job));
    }

    @Override
    public Optional<CreditSimulationJob> findById(UUID id) {
        return repository.findById(id).map(SimulationJobMapper::toModel);
    }

    @Override
    public void updateStatus(UUID jobId, SimulationJobStatus status) {
        repository.findById(jobId).ifPresent(job -> {
            job.setStatus(SimulationJobStatus.valueOf(status.name()));
            repository.save(job);
        });
    }
}

