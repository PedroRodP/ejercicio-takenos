package com.takenos.investment.dto;

import com.takenos.investment.model.Purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseTO(
        UUID id,
        String userId,
        BigDecimal amountUsd,
        BigDecimal units,
        BigDecimal remainingUnits,
        BigDecimal pricePerUnit,
        String reference,
        Instant created) {

    public static PurchaseTO from(Purchase purchase) {
        return new PurchaseTO(
                purchase.getId(),
                purchase.getUserId(),
                purchase.getAmountUsd(),
                purchase.getUnits(),
                purchase.getRemainingUnits(),
                purchase.getPricePerUnit(),
                purchase.getReference(),
                purchase.getCreated());
    }
}
