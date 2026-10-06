package com.takenos.investment.repository;

import com.takenos.investment.model.Sale;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory storage of sales, grouped by user and kept in insertion (chronological) order.
 */
@ApplicationScoped
public class SaleRepository {

    private final Map<String, List<Sale>> salesByUser = new ConcurrentHashMap<>();

    public Sale save(Sale sale) {
        salesByUser.computeIfAbsent(sale.userId(), userId -> new CopyOnWriteArrayList<>()).add(sale);
        return sale;
    }

    public List<Sale> findByUserId(String userId) {
        return List.copyOf(salesByUser.getOrDefault(userId, List.of()));
    }

    public void deleteAll() {
        salesByUser.clear();
    }
}
