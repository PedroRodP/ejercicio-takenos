package com.takenos.investment.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Portion of a purchase consumed by a sale, with the cost basis of those units.
 */
public record ConsumedPurchase(
        UUID purchaseId,
        BigDecimal units,
        BigDecimal pricePerUnit,
        BigDecimal costUsd) {
}
