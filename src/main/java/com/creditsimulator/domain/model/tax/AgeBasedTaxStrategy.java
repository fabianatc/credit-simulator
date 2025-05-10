package com.creditsimulator.domain.model.tax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

public class AgeBasedTaxStrategy implements TaxStrategy {

    private final TaxPolicy policy = new TaxPolicy();

    @Override
    public BigDecimal getAnnualTax(LocalDate birthDate, BigDecimal fixedTax) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return policy.getTaxForAge(age);
    }
}