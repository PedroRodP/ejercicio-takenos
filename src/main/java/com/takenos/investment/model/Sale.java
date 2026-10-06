package com.takenos.investment.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Sale(
        UUID id,
        String userId,
        BigDecimal units,
        BigDecimal pricePerUnit,
        BigDecimal proceedsUsd,
        BigDecimal costUsd,
        BigDecimal realizedProfitUsd,
        List<ConsumedPurchase> consumedPurchases,
        Instant created) {
}
