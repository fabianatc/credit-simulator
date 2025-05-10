package com.creditsimulator.domain.model.tax;

import java.math.BigDecimal;

public record TaxRange(
    int minAge,
    int maxAge,
    BigDecimal annualRate) {
}