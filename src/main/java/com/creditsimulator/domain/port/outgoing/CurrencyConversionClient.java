package com.creditsimulator.domain.port.outgoing;

import java.math.BigDecimal;

public interface CurrencyConversionClient {
    BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency);
}
