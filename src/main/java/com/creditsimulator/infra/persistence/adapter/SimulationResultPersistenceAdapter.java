package com.creditsimulator.infra.persistence.adapter;

import com.creditsimulator.domain.model.simulation.CreditSimulationResult;
import com.creditsimulator.domain.port.outgoing.SimulationResultPersistencePort;
import com.creditsimulator.infra.persistence.repository.SimulationResultRepository;
import com.creditsimulator.shared.mapper.SimulationResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SimulationResultPersistenceAdapter implements SimulationResultPersistencePort {
    private final SimulationResultRepository repository;

    @Override
    public void save(CreditSimulationResult result) {
        repository.save(SimulationResultMapper.toEntity(result));
    }

    @Override
    public List<CreditSimulationResult> findByJobId(UUID jobId) {
        return repository.findAllByJob_Id(jobId).stream()
            .map(SimulationResultMapper::toModel)
            .collect(Collectors.toList());
    }
}