package com.takenos.investment.service

import com.takenos.investment.dto.BuyRequestTO
import com.takenos.investment.model.Purchase
import com.takenos.investment.repository.PurchaseRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class PurchaseServiceTest extends Specification {

    static final Instant NOW = Instant.parse("2026-10-06T12:00:00Z")

    PurchaseRepository purchaseRepository = Mock(PurchaseRepository)

    @Subject
    PurchaseService purchaseService = new PurchaseService(purchaseRepository, Clock.fixed(NOW, ZoneOffset.UTC))

    def "buy should compute units from amount and price and save the purchase"() {
        given:
        def request = new BuyRequestTO(new BigDecimal("100"), new BigDecimal("25"), "payin:abc123")

        when:
        def purchase = purchaseService.buy("user-1", request)

        then:
        1 * purchaseRepository.save(_ as Purchase) >> { Purchase saved -> saved }
        purchase.id != null
        purchase.userId == "user-1"
        purchase.amountUsd == new BigDecimal("100")
        purchase.units == new BigDecimal("4.00000000")
        purchase.remainingUnits == new BigDecimal("4.00000000")
        purchase.pricePerUnit == new BigDecimal("25")
        purchase.reference == "payin:abc123"
        purchase.created == NOW
    }

    def "buy should round units down to 8 decimals (#amount USD at #price)"() {
        given:
        def request = new BuyRequestTO(new BigDecimal(amount), new BigDecimal(price), "transfer:9f2")

        when:
        def purchase = purchaseService.buy("user-1", request)

        then:
        1 * purchaseRepository.save(_ as Purchase) >> { Purchase saved -> saved }
        purchase.units == new BigDecimal(expectedUnits)

        where:
        amount   | price    || expectedUnits
        "100"    | "3"      || "33.33333333"
        "10"     | "6"      || "1.66666666"
        "0.01"   | "100000" || "0.00000010"
        "250.50" | "0.5"    || "501.00000000"
    }

    def "buy should register a new purchase each time even if the reference repeats"() {
        given:
        def request = new BuyRequestTO(new BigDecimal("100"), new BigDecimal("10"), "payin:abc123")

        when:
        purchaseService.buy("user-1", request)
        purchaseService.buy("user-1", request)

        then:
        2 * purchaseRepository.save(_ as Purchase) >> { Purchase saved -> saved }
    }
}
