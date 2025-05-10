package com.creditsimulator.domain.port.incoming;

import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.shared.enums.TaxType;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SimulateCreditUseCase {
    CreditSimulation simulate(
        BigDecimal creditAmount,
        int termInMonths,
        LocalDate birthDate,
        TaxType taxType,
        BigDecimal fixedTax,
        String currency);
}
