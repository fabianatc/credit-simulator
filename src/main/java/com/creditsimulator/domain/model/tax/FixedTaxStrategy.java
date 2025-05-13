package com.creditsimulator.domain.model.tax;

import com.creditsimulator.shared.exception.BusinessValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FixedTaxStrategy implements TaxStrategy {

    @Override
    public BigDecimal getAnnualTax(LocalDate birthDate, BigDecimal fixedTax) {
        if (fixedTax == null)
            throw new BusinessValidationException("Fixed tax must be provided when tax type is FIXED");
        return fixedTax;
    }
}