package com.creditsimulator.infra.client;

import com.creditsimulator.domain.port.outgoing.CurrencyConversionClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MockCurrencyConversionClient implements CurrencyConversionClient {

    @Override
    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) return amount;

        /* Simulation of fictitious rates
         * TODO: Should be retrieved from an external service
         */
        BigDecimal rate = switch (toCurrency.toUpperCase()) {
            case "USD" -> new BigDecimal("0.20");
            case "EUR" -> new BigDecimal("0.18");
            case "BTC" -> new BigDecimal("0.000003");
            default -> BigDecimal.ONE; // fallback
        };

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
