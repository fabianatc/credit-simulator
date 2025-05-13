package com.creditsimulator.unit;

import com.creditsimulator.domain.model.tax.TaxPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TaxPolicyTest {

    private final TaxPolicy policy = new TaxPolicy();

    @Test
    void shouldReturn5PercentForAgeUpTo25() {
        BigDecimal rate = policy.getTaxForAge(18);
        assertEquals(new BigDecimal("0.05"), rate);
    }

    @Test
    void shouldReturn3PercentForAgeBetween26And40() {
        BigDecimal rate = policy.getTaxForAge(35);
        assertEquals(new BigDecimal("0.03"), rate);
    }

    @Test
    void shouldReturn2PercentForAgeBetween41And60() {
        BigDecimal rate = policy.getTaxForAge(50);
        assertEquals(new BigDecimal("0.02"), rate);
    }

    @Test
    void shouldReturn4PercentForAgeAbove60() {
        BigDecimal rate = policy.getTaxForAge(65);
        assertEquals(new BigDecimal("0.04"), rate);
    }

    @Test
    void shouldThrowExceptionForAgeBelowZero() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            policy.getTaxForAge(-5)
        );

        assertTrue(exception.getMessage().contains("No rate found for age"));
    }

    @Test
    void shouldThrowExceptionForAgeAboveMax() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            policy.getTaxForAge(201)
        );

        assertTrue(exception.getMessage().contains("No rate found for age"));
    }
}