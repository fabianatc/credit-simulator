package com.creditsimulator.application.message;

import com.creditsimulator.shared.enums.TaxType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record QueuedSimulationMessage(
    UUID jobId,
    BigDecimal creditAmount,
    Integer termInMonths,
    LocalDate birthDate,
    TaxType taxType,
    BigDecimal fixedTax,
    String currency) implements Serializable {
}
