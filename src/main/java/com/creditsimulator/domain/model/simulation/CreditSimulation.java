package com.creditsimulator.domain.model.simulation;

import com.creditsimulator.domain.port.outgoing.CurrencyConversionClient;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditSimulation(
    BigDecimal creditAmount,
    int termInMonths,
    LocalDate birthDate,
    BigDecimal totalAmount,
    BigDecimal monthlyPayment,
    BigDecimal feePaid
) {
    public CreditSimulation convertTo(String currency, CurrencyConversionClient converter) {
        return new CreditSimulation(
            converter.convert(creditAmount, "BRL", currency),
            termInMonths,
            birthDate,
            converter.convert(totalAmount, "BRL", currency),
            converter.convert(monthlyPayment, "BRL", currency),
            converter.convert(feePaid, "BRL", currency)
        );
    }
}