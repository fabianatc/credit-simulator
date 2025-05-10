package com.creditsimulator.domain.model.simulation;

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
}