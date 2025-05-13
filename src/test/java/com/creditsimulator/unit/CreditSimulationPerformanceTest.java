package com.creditsimulator.unit;

import com.creditsimulator.domain.model.simulation.CreditSimulationCalculator;
import com.creditsimulator.shared.enums.TaxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.DoubleSummaryStatistics;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreditSimulationPerformanceTest {
    private CreditSimulationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CreditSimulationCalculator();
    }

    @Test
    void shouldCalculateWithinExpectedTime() {
        int runs = 10_000;

        calculator.calculate(
            BigDecimal.valueOf(10000),
            24,
            LocalDate.of(1990, 5, 20),
            TaxType.AGE_BASED,
            null
        );

        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();

            calculator.calculate(
                BigDecimal.valueOf(10000),
                24,
                LocalDate.of(1990, 5, 20),
                TaxType.AGE_BASED,
                null
            );

            long durationNs = System.nanoTime() - start;
            double durationMs = durationNs / 1_000_000.0;
            stats.accept(durationMs);
        }

        System.out.printf("""
            📊 Cálculo executado %d vezes
            🔹 Média:     %.3f ms
            🔹 Máximo:    %.3f ms
            🔹 Mínimo:    %.3f ms
            """, runs, stats.getAverage(), stats.getMax(), stats.getMin());

        // ✅ Limite de tempo médio por cálculo (ajustável)
        assertTrue(stats.getAverage() < 5, "Tempo médio superior a 5ms");
    }
}