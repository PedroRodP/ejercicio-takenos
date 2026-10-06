package com.takenos.investment.dto;

import com.takenos.investment.model.Sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleTO(
        UUID id,
        String userId,
        BigDecimal units,
        BigDecimal pricePerUnit,
        BigDecimal proceedsUsd,
        BigDecimal costUsd,
        BigDecimal realizedProfitUsd,
        List<ConsumedPurchaseTO> consumedPurchases,
        Instant created) {

    public static SaleTO from(Sale sale) {
        return new SaleTO(
                sale.id(),
                sale.userId(),
                sale.units(),
                sale.pricePerUnit(),
                sale.proceedsUsd(),
                sale.costUsd(),
                sale.realizedProfitUsd(),
                sale.consumedPurchases().stream().map(ConsumedPurchaseTO::from).toList(),
                sale.created());
    }
}
