package com.takenos.investment.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A buy lot. Sales consume its units in FIFO order, so {@code remainingUnits} decreases over time.
 */
@Getter
public class Purchase {

    private final UUID id;
    private final String userId;
    private final BigDecimal amountUsd;
    private final BigDecimal units;
    private final BigDecimal pricePerUnit;
    private final String reference;
    private final Instant created;
    private BigDecimal remainingUnits;

    public Purchase(UUID id, String userId, BigDecimal amountUsd, BigDecimal units, BigDecimal pricePerUnit,
                    String reference, Instant created) {
        this.id = id;
        this.userId = userId;
        this.amountUsd = amountUsd;
        this.units = units;
        this.pricePerUnit = pricePerUnit;
        this.reference = reference;
        this.created = created;
        this.remainingUnits = units;
    }

    public boolean hasRemainingUnits() {
        return remainingUnits.signum() > 0;
    }

    /**
     * Consumes up to {@code requestedUnits} from this lot and returns how many were actually consumed.
     */
    public BigDecimal consume(BigDecimal requestedUnits) {
        BigDecimal consumed = requestedUnits.min(remainingUnits);
        remainingUnits = remainingUnits.subtract(consumed);
        return consumed;
    }
}
