package com.creditsimulator.domain.model.tax;

import java.math.BigDecimal;
import java.util.List;

public class TaxPolicy {
    private final List<TaxRange> ranges = List.of(
        new TaxRange(0, 25, new BigDecimal("0.05")),
        new TaxRange(26, 40, new BigDecimal("0.03")),
        new TaxRange(41, 60, new BigDecimal("0.02")),
        new TaxRange(61, 200, new BigDecimal("0.04"))
    );

    public BigDecimal getTaxForAge(int age) {
        return ranges.stream()
            .filter(r -> age >= r.minAge() && age <= r.maxAge())
            .map(TaxRange::annualRate)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No rate found for age " + age));
    }
}
