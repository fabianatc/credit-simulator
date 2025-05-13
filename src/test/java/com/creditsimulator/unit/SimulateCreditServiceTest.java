package com.creditsimulator.unit;

import com.creditsimulator.application.service.SimulateCreditService;
import com.creditsimulator.domain.model.simulation.CreditSimulation;
import com.creditsimulator.domain.model.simulation.CreditSimulationCalculator;
import com.creditsimulator.domain.port.outgoing.CurrencyConversionClient;
import com.creditsimulator.shared.enums.TaxType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulateCreditServiceTest {

    @Mock
    private CreditSimulationCalculator calculator;

    @Mock
    private CurrencyConversionClient currencyClient;

    @InjectMocks
    private SimulateCreditService service;

    private final BigDecimal creditAmount = new BigDecimal("10000.00");
    private final int termInMonths = 24;
    private final LocalDate birthDate = LocalDate.of(1990, 5, 20);
    private final TaxType taxType = TaxType.AGE_BASED;
    private final BigDecimal fixedTax = null;

    @Test
    void shouldReturnSimulationInBRLWhenCurrencyIsNull() {
        CreditSimulation simulationBRL = new CreditSimulation(
            creditAmount,
            termInMonths,
            birthDate,
            new BigDecimal("12254.70"),
            new BigDecimal("93.11"),
            new BigDecimal("254.70")
        );

        when(calculator.calculate(creditAmount, termInMonths, birthDate, taxType, fixedTax))
            .thenReturn(simulationBRL);

        CreditSimulation result = service.simulate(creditAmount, termInMonths, birthDate, taxType, fixedTax, null);

        assertThat(result).isEqualTo(simulationBRL);
        verify(currencyClient, never()).convert(any(), any(), any());
    }

    @Test
    void shouldConvertValuesWhenCurrencyIsDifferentThanBRL() {
        CreditSimulation baseSim = new CreditSimulation(
            creditAmount,
            termInMonths,
            birthDate,
            new BigDecimal("12254.70"),
            new BigDecimal("93.11"),
            new BigDecimal("254.70")
        );

        when(calculator.calculate(creditAmount, termInMonths, birthDate, taxType, fixedTax))
            .thenReturn(baseSim);

        when(currencyClient.convert(any(), eq("BRL"), eq("USD"))).thenAnswer(invocation -> {
            BigDecimal value = invocation.getArgument(0);
            return value.multiply(new BigDecimal("0.2"));
        });

        CreditSimulation result = service.simulate(creditAmount, termInMonths, birthDate, taxType, fixedTax, "USD");

        assertThat(result.creditAmount()).isEqualByComparingTo("2000.0000");
        assertThat(result.totalAmount()).isEqualByComparingTo("2450.9400");
        assertThat(result.monthlyPayment()).isEqualByComparingTo("18.6220");
        assertThat(result.feePaid()).isEqualByComparingTo("50.9400");
        assertThat(result.termInMonths()).isEqualTo(termInMonths);
        assertThat(result.birthDate()).isEqualTo(birthDate);
    }

    @Test
    void shouldTreatLowercaseCurrencyCode() {
        when(calculator.calculate(any(), anyInt(), any(), any(), any()))
            .thenReturn(mock(CreditSimulation.class));

        service.simulate(creditAmount, termInMonths, birthDate, taxType, fixedTax, "usd");

        verify(currencyClient, atLeastOnce()).convert(any(), eq("BRL"), eq("USD"));
    }

    @Test
    void shouldReturnBRLSimulationWhenCurrencyIsBlank() {
        when(calculator.calculate(any(), anyInt(), any(), any(), any()))
            .thenReturn(mock(CreditSimulation.class));

        service.simulate(creditAmount, termInMonths, birthDate, taxType, fixedTax, "  ");

        verify(currencyClient, never()).convert(any(), any(), any());
    }
}
