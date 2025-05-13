package com.creditsimulator.application.request;

import com.creditsimulator.shared.enums.TaxType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Credit simulation request data")
public record CreditSimulationRequest(
    @Schema(description = "Currency code (ISO 4217). Defaults to BRL if not provided", example = "USD")
    String currency,

    @NotNull
    @Schema(description = "Credit amount requested", example = "10000.00")
    BigDecimal creditAmount,

    @NotNull
    @Positive
    @Schema(description = "Payment term in months", example = "24")
    Integer termInMonths,

    @NotNull
    @Schema(description = "Customer's birth date", example = "1990-05-20")
    LocalDate birthDate,

    @NotNull
    @Schema(description = "Tax type to be used in simulation", allowableValues = {"FIXED", "AGE_BASED"})
    TaxType taxType,

    @Schema(description = "Fixed annual tax rate (required if taxType is FIXED)", example = "0.035")
    BigDecimal fixedTax
) {
}