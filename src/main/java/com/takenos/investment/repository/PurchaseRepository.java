package com.takenos.investment.repository;

import com.takenos.investment.model.Purchase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory storage of purchases, grouped by user and kept in insertion (chronological) order.
 */
@ApplicationScoped
public class PurchaseRepository {

    private final Map<String, List<Purchase>> purchasesByUser = new ConcurrentHashMap<>();

    public Purchase save(Purchase purchase) {
        purchasesByUser.computeIfAbsent(purchase.getUserId(), userId -> new CopyOnWriteArrayList<>()).add(purchase);
        return purchase;
    }

    public List<Purchase> findByUserId(String userId) {
        return List.copyOf(purchasesByUser.getOrDefault(userId, List.of()));
    }

    public void deleteAll() {
        purchasesByUser.clear();
    }
}
