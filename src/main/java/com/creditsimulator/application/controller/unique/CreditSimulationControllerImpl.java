package com.creditsimulator.application.controller.unique;

import com.creditsimulator.application.request.CreditSimulationRequest;
import com.creditsimulator.application.response.CreditSimulationResponse;
import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.port.incoming.SimulateCreditUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
public class CreditSimulationControllerImpl implements CreditSimulationController {

    private final SimulateCreditUseCase simulateCreditUseCase;

    @Override
    public ResponseEntity<CreditSimulationResponse> simulate(CreditSimulationRequest request) {
        log.info("Received simulation request: {}", request);
        CreditSimulation simulation = simulateCreditUseCase.simulate(
            request.creditAmount(),
            request.termInMonths(),
            request.birthDate(),
            request.taxType(),
            request.fixedTax(),
            request.currency()
        );

        log.info("Simulation result: {}", simulation);
        return ResponseEntity.ok(new CreditSimulationResponse(
            simulation.totalAmount(),
            simulation.monthlyPayment(),
            simulation.feePaid(),
            request.currency() == null ? "BRL" : request.currency().toUpperCase()
        ));
    }
}
