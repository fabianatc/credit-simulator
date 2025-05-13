package com.creditsimulator.unit;

import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.model.simulation.CreditSimulationCalculator;
import com.creditsimulator.shared.enums.TaxType;
import com.creditsimulator.shared.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CreditSimulationCalculatorTest {

    private CreditSimulationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CreditSimulationCalculator();
    }

    @Test
    void shouldCalculateWithFixedTax() {
        BigDecimal creditAmount = new BigDecimal("10000");
        int term = 24;
        LocalDate birthDate = LocalDate.of(1985, 7, 11);
        BigDecimal fixedTax = new BigDecimal("0.035");

        CreditSimulation result = calculator.calculate(creditAmount, term, birthDate, TaxType.FIXED, fixedTax);

        assertNotNull(result);
        assertEquals(creditAmount, result.creditAmount());
        assertEquals(term, result.termInMonths());
        assertEquals(birthDate, result.birthDate());
        assertTrue(result.totalAmount().compareTo(creditAmount) > 0);
        assertTrue(result.feePaid().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(result.totalAmount(), result.monthlyPayment().multiply(BigDecimal.valueOf(term)));
    }

    @Test
    void shouldCalculateWithAgeBasedTax_Under25() {
        BigDecimal creditAmount = new BigDecimal("5000");
        int term = 12;
        LocalDate birthDate = LocalDate.now().minusYears(22); // idade 22
        CreditSimulation result = calculator.calculate(creditAmount, term, birthDate, TaxType.AGE_BASED, null);

        assertNotNull(result);
        assertTrue(result.totalAmount().compareTo(creditAmount) > 0);
    }

    @Test
    void shouldCalculateWithAgeBasedTax_Over40() {
        BigDecimal creditAmount = new BigDecimal("8000");
        int term = 36;
        LocalDate birthDate = LocalDate.now().minusYears(45); // idade 45
        CreditSimulation result = calculator.calculate(creditAmount, term, birthDate, TaxType.AGE_BASED, null);

        assertNotNull(result);
        assertTrue(result.totalAmount().compareTo(creditAmount) > 0);
        assertEquals(creditAmount, result.creditAmount());
        assertEquals(term, result.termInMonths());
    }

    @Test
    void shouldThrowExceptionForNullFixedTaxWhenRequired() {
        BigDecimal creditAmount = new BigDecimal("7000");
        int term = 18;
        LocalDate birthDate = LocalDate.of(1990, 1, 1);

        Exception exception = assertThrows(BusinessValidationException.class, () ->
            calculator.calculate(creditAmount, term, birthDate, TaxType.FIXED, null)
        );

        assertEquals("Fixed tax must be provided when tax type is FIXED", exception.getMessage());
    }
}
