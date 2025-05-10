package com.creditsimulator.application.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Result of a credit simulation")
public record CreditSimulationResponse(
    @Schema(description = "Total amount to be paid", example = "11273.50")
    BigDecimal totalAmount,

    @Schema(description = "Monthly installment value", example = "469.73")
    BigDecimal monthlyPayment,

    @Schema(description = "Total fee paid", example = "1273.50")
    BigDecimal feePaid,

    @Schema(description = "Currency used in the response", example = "USD")
    String currency
) {
}