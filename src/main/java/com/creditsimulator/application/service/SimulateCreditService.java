package com.creditsimulator.application.service;

import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.model.simulation.CreditSimulationCalculator;
import com.creditsimulator.domain.port.incoming.SimulateCreditUseCase;
import com.creditsimulator.domain.port.outgoing.CurrencyConversionClient;
import com.creditsimulator.shared.enums.TaxType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class SimulateCreditService implements SimulateCreditUseCase {

    private final CreditSimulationCalculator calculator;
    private final CurrencyConversionClient currencyClient;

    @Override
    public CreditSimulation simulate(BigDecimal creditAmount, int termInMonths, LocalDate birthDate,
                                     TaxType taxType, BigDecimal fixedTax, String currency) {

        var simulation = calculator.calculate(creditAmount, termInMonths, birthDate, taxType, fixedTax);

        String currencyBRL = "BRL";
        String targetCurrency = (currency == null || currency.isBlank()) ? currencyBRL : currency.toUpperCase();

        if (!"BRL".equals(targetCurrency)) {
            return new CreditSimulation(
                currencyClient.convert(simulation.creditAmount(), currencyBRL, targetCurrency),
                simulation.termInMonths(),
                simulation.birthDate(),
                currencyClient.convert(simulation.totalAmount(), currencyBRL, targetCurrency),
                currencyClient.convert(simulation.monthlyPayment(), currencyBRL, targetCurrency),
                currencyClient.convert(simulation.feePaid(), currencyBRL, targetCurrency)
            );
        }

        return simulation;
    }
}