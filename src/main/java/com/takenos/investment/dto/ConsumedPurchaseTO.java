package com.takenos.investment.dto;

import com.takenos.investment.model.ConsumedPurchase;

import java.math.BigDecimal;
import java.util.UUID;

public record ConsumedPurchaseTO(
        UUID purchaseId,
        BigDecimal units,
        BigDecimal pricePerUnit,
        BigDecimal costUsd) {

    public static ConsumedPurchaseTO from(ConsumedPurchase consumedPurchase) {
        return new ConsumedPurchaseTO(
                consumedPurchase.purchaseId(),
                consumedPurchase.units(),
                consumedPurchase.pricePerUnit(),
                consumedPurchase.costUsd());
    }
}
