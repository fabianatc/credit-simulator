package com.creditsimulator.domain.model.tax;

import com.creditsimulator.shared.enums.TaxType;

public class TaxStrategyFactory {
    public static TaxStrategy getStrategy(TaxType taxType) {
        return switch (taxType) {
            case AGE_BASED -> new AgeBasedTaxStrategy();
            case FIXED -> new FixedTaxStrategy();
        };
    }
}
