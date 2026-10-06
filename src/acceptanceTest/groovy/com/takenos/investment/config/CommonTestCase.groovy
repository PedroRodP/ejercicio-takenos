package com.takenos.investment.config

import com.takenos.investment.repository.PurchaseRepository
import com.takenos.investment.repository.SaleRepository
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach

@QuarkusTest
abstract class CommonTestCase {

    @Inject
    PurchaseRepository purchaseRepository

    @Inject
    SaleRepository saleRepository

    @AfterEach
    void cleanup() {
        purchaseRepository.deleteAll()
        saleRepository.deleteAll()
    }
}
