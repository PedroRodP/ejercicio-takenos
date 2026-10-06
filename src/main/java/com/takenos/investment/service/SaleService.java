package com.takenos.investment.service;

import com.takenos.investment.dto.SellRequestTO;
import com.takenos.investment.exception.InsufficientUnitsException;
import com.takenos.investment.model.ConsumedPurchase;
import com.takenos.investment.model.Purchase;
import com.takenos.investment.model.Sale;
import com.takenos.investment.repository.PurchaseRepository;
import com.takenos.investment.repository.SaleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class SaleService {

    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;
    private final Clock clock;

    @Inject
    public SaleService(PurchaseRepository purchaseRepository, SaleRepository saleRepository) {
        this(purchaseRepository, saleRepository, Clock.systemUTC());
    }

    SaleService(PurchaseRepository purchaseRepository, SaleRepository saleRepository, Clock clock) {
        this.purchaseRepository = purchaseRepository;
        this.saleRepository = saleRepository;
        this.clock = clock;
    }

    /**
     * Sells units consuming the user's purchases in FIFO order (oldest first). The realized profit is the
     * difference between the proceeds of the sale and the cost basis of the consumed units.
     * Synchronized so that two concurrent sales cannot consume the same units.
     */
    public synchronized Sale sell(String userId, SellRequestTO request) {
        List<Purchase> openPurchases = purchaseRepository.findByUserId(userId).stream()
                .filter(Purchase::hasRemainingUnits)
                .toList();
        BigDecimal available = openPurchases.stream()
                .map(Purchase::getRemainingUnits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (available.compareTo(request.units()) < 0) {
            throw new InsufficientUnitsException(userId, request.units(), available);
        }

        List<ConsumedPurchase> consumedPurchases = new ArrayList<>();
        BigDecimal pendingUnits = request.units();
        for (Purchase purchase : openPurchases) {
            if (pendingUnits.signum() == 0) {
                break;
            }
            BigDecimal consumedUnits = purchase.consume(pendingUnits);
            BigDecimal costUsd = consumedUnits.multiply(purchase.getPricePerUnit());
            consumedPurchases.add(new ConsumedPurchase(purchase.getId(), consumedUnits, purchase.getPricePerUnit(), costUsd));
            pendingUnits = pendingUnits.subtract(consumedUnits);
        }

        BigDecimal proceedsUsd = request.units().multiply(request.pricePerUnit());
        BigDecimal costUsd = consumedPurchases.stream()
                .map(ConsumedPurchase::costUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Sale sale = new Sale(
                UUID.randomUUID(),
                userId,
                request.units(),
                request.pricePerUnit(),
                proceedsUsd,
                costUsd,
                proceedsUsd.subtract(costUsd),
                List.copyOf(consumedPurchases),
                Instant.now(clock));
        log.info("User {} sold {} units at {} with realized profit {}", userId, sale.units(), sale.pricePerUnit(),
                sale.realizedProfitUsd());
        return saleRepository.save(sale);
    }
}
