package com.creditsimulator.domain.model.tax;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TaxStrategy {
    BigDecimal getAnnualTax(LocalDate birthDate, BigDecimal fixedTax);
}
