package com.takenos.investment.service

import com.takenos.investment.model.Purchase
import com.takenos.investment.model.Sale
import com.takenos.investment.repository.PurchaseRepository
import com.takenos.investment.repository.SaleRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class PositionServiceTest extends Specification {

    PurchaseRepository purchaseRepository = Mock(PurchaseRepository)
    SaleRepository saleRepository = Mock(SaleRepository)

    @Subject
    PositionService positionService = new PositionService(purchaseRepository, saleRepository)

    def "getPosition should return zeros for a user without operations"() {
        given:
        purchaseRepository.findByUserId("user-1") >> []
        saleRepository.findByUserId("user-1") >> []

        when:
        def position = positionService.getPosition("user-1")

        then:
        position.userId() == "user-1"
        position.units() == 0
        position.costUsd() == 0
        position.realizedProfitUsd() == 0
    }

    def "getPosition should sum remaining units and their cost at each purchase price"() {
        given:
        def partiallySold = purchase("10", "10")
        partiallySold.consume(new BigDecimal("4"))
        def fullySold = purchase("5", "20")
        fullySold.consume(new BigDecimal("5"))
        def untouched = purchase("2", "30")
        purchaseRepository.findByUserId("user-1") >> [partiallySold, fullySold, untouched]
        saleRepository.findByUserId("user-1") >> []

        when:
        def position = positionService.getPosition("user-1")

        then:
        position.units() == 8
        position.costUsd() == 120
    }

    def "getPosition should sum the realized profit of every sale, including losses"() {
        given:
        purchaseRepository.findByUserId("user-1") >> []
        saleRepository.findByUserId("user-1") >> [sale("20"), sale("-5.5")]

        when:
        def position = positionService.getPosition("user-1")

        then:
        position.realizedProfitUsd() == 14.5
    }

    private static Purchase purchase(String units, String pricePerUnit) {
        new Purchase(UUID.randomUUID(), "user-1", new BigDecimal(units) * new BigDecimal(pricePerUnit),
                new BigDecimal(units), new BigDecimal(pricePerUnit), "payin:${UUID.randomUUID()}", Instant.now())
    }

    private static Sale sale(String realizedProfitUsd) {
        new Sale(UUID.randomUUID(), "user-1", BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN,
                new BigDecimal(realizedProfitUsd), [], Instant.now())
    }
}
