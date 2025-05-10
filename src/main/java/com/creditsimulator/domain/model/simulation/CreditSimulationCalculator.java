package com.creditsimulator.domain.model.simulation;

import com.creditsimulator.domain.model.tax.TaxPolicy;
import com.creditsimulator.domain.model.tax.TaxStrategy;
import com.creditsimulator.domain.model.tax.TaxStrategyFactory;
import com.creditsimulator.shared.enums.TaxType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class CreditSimulationCalculator {
    private final TaxPolicy taxPolicy;

    public CreditSimulationCalculator() {
        this.taxPolicy = new TaxPolicy();
    }

    public CreditSimulation calculate(BigDecimal creditAmount, int termInMonths, LocalDate birthDate,
                                      TaxType taxType, BigDecimal fixedTax) {

        TaxStrategy strategy = TaxStrategyFactory.getStrategy(taxType);
        BigDecimal annualTax = strategy.getAnnualTax(birthDate, fixedTax);
        BigDecimal monthlyRate = annualTax.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal numerator = monthlyRate.multiply(creditAmount);
        BigDecimal denominator = BigDecimal.ONE.subtract(
            BigDecimal.ONE.add(monthlyRate).pow(-termInMonths, MathContext.DECIMAL128)
        );

        BigDecimal monthlyPayment = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = monthlyPayment.multiply(BigDecimal.valueOf(termInMonths));
        BigDecimal taxPaid = totalAmount.subtract(creditAmount);

        return new CreditSimulation(
            creditAmount, termInMonths, birthDate,
            totalAmount, monthlyPayment, taxPaid
        );
    }
}
