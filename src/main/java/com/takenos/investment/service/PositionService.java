package com.takenos.investment.service;

import com.takenos.investment.model.Position;
import com.takenos.investment.model.Purchase;
import com.takenos.investment.model.Sale;
import com.takenos.investment.repository.PurchaseRepository;
import com.takenos.investment.repository.SaleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;

@ApplicationScoped
public class PositionService {

    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;

    @Inject
    public PositionService(PurchaseRepository purchaseRepository, SaleRepository saleRepository) {
        this.purchaseRepository = purchaseRepository;
        this.saleRepository = saleRepository;
    }

    /**
     * Units and cost come from what is left of each purchase after FIFO sales; the realized profit is the sum of
     * the profit registered by every sale.
     */
    public Position getPosition(String userId) {
        List<Purchase> purchases = purchaseRepository.findByUserId(userId);
        BigDecimal units = purchases.stream()
                .map(Purchase::getRemainingUnits)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costUsd = purchases.stream()
                .map(purchase -> purchase.getRemainingUnits().multiply(purchase.getPricePerUnit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realizedProfitUsd = saleRepository.findByUserId(userId).stream()
                .map(Sale::realizedProfitUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Position(userId, units, costUsd, realizedProfitUsd);
    }
}
