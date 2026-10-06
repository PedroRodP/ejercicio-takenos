package com.takenos.investment.service

import com.takenos.investment.dto.SellRequestTO
import com.takenos.investment.exception.InsufficientUnitsException
import com.takenos.investment.model.Purchase
import com.takenos.investment.model.Sale
import com.takenos.investment.repository.PurchaseRepository
import com.takenos.investment.repository.SaleRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SaleServiceTest extends Specification {

    static final Instant NOW = Instant.parse("2026-10-06T12:00:00Z")

    PurchaseRepository purchaseRepository = Mock(PurchaseRepository)
    SaleRepository saleRepository = Mock(SaleRepository)

    @Subject
    SaleService saleService = new SaleService(purchaseRepository, saleRepository, Clock.fixed(NOW, ZoneOffset.UTC))

    def "sell should consume part of a single purchase and compute the realized profit"() {
        given:
        def purchase = purchase("10", "10")
        purchaseRepository.findByUserId("user-1") >> [purchase]

        when:
        def sale = saleService.sell("user-1", new SellRequestTO(new BigDecimal("4"), new BigDecimal("15")))

        then:
        1 * saleRepository.save(_ as Sale) >> { Sale saved -> saved }
        sale.id != null
        sale.userId == "user-1"
        sale.units == 4
        sale.pricePerUnit == 15
        sale.proceedsUsd == 60
        sale.costUsd == 40
        sale.realizedProfitUsd == 20
        sale.created == NOW
        sale.consumedPurchases.size() == 1
        sale.consumedPurchases[0].purchaseId == purchase.id
        sale.consumedPurchases[0].units == 4
        sale.consumedPurchases[0].costUsd == 40
        purchase.remainingUnits == 6
    }

    def "sell should consume purchases in FIFO order across several lots"() {
        given:
        def oldest = purchase("10", "10")
        def middle = purchase("5", "20")
        def newest = purchase("5", "30")
        purchaseRepository.findByUserId("user-1") >> [oldest, middle, newest]

        when:
        def sale = saleService.sell("user-1", new SellRequestTO(new BigDecimal("12"), new BigDecimal("25")))

        then:
        1 * saleRepository.save(_ as Sale) >> { Sale saved -> saved }
        sale.consumedPurchases*.purchaseId == [oldest.id, middle.id]
        sale.consumedPurchases*.units == [10, 2]
        sale.consumedPurchases*.costUsd == [100, 40]
        sale.proceedsUsd == 300
        sale.costUsd == 140
        sale.realizedProfitUsd == 160
        oldest.remainingUnits == 0
        middle.remainingUnits == 3
        newest.remainingUnits == 5
    }

    def "sell should skip purchases already fully consumed by previous sales"() {
        given:
        def consumed = purchase("10", "10")
        consumed.consume(new BigDecimal("10"))
        def open = purchase("10", "20")
        purchaseRepository.findByUserId("user-1") >> [consumed, open]

        when:
        def sale = saleService.sell("user-1", new SellRequestTO(new BigDecimal("5"), new BigDecimal("12")))

        then:
        1 * saleRepository.save(_ as Sale) >> { Sale saved -> saved }
        sale.consumedPurchases*.purchaseId == [open.id]
        sale.costUsd == 100
        sale.realizedProfitUsd == -40
    }

    def "sell should fail without consuming anything when the user has not enough units"() {
        given:
        def purchase = purchase("10", "10")
        purchaseRepository.findByUserId("user-1") >> [purchase]

        when:
        saleService.sell("user-1", new SellRequestTO(new BigDecimal("11"), new BigDecimal("15")))

        then:
        thrown(InsufficientUnitsException)
        0 * saleRepository.save(_)
        purchase.remainingUnits == 10
    }

    private static Purchase purchase(String units, String pricePerUnit) {
        def amount = new BigDecimal(units) * new BigDecimal(pricePerUnit)
        new Purchase(UUID.randomUUID(), "user-1", amount, new BigDecimal(units), new BigDecimal(pricePerUnit),
                "payin:${UUID.randomUUID()}", Instant.now())
    }
}
