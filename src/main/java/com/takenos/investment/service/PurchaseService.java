package com.takenos.investment.service;

import com.takenos.investment.dto.BuyRequestTO;
import com.takenos.investment.model.Purchase;
import com.takenos.investment.repository.PurchaseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class PurchaseService {

    static final int UNITS_SCALE = 8;

    private final PurchaseRepository purchaseRepository;
    private final Clock clock;

    @Inject
    public PurchaseService(PurchaseRepository purchaseRepository) {
        this(purchaseRepository, Clock.systemUTC());
    }

    PurchaseService(PurchaseRepository purchaseRepository, Clock clock) {
        this.purchaseRepository = purchaseRepository;
        this.clock = clock;
    }

    public Purchase buy(String userId, BuyRequestTO request) {
        BigDecimal units = request.amountUsd().divide(request.pricePerUnit(), UNITS_SCALE, RoundingMode.DOWN);
        Purchase purchase = new Purchase(
                UUID.randomUUID(),
                userId,
                request.amountUsd(),
                units,
                request.pricePerUnit(),
                request.reference(),
                Instant.now(clock));
        log.info("User {} bought {} units at {} (reference {})", userId, units, request.pricePerUnit(), request.reference());
        return purchaseRepository.save(purchase);
    }
}
