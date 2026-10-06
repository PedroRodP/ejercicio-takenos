package com.takenos.investment.resource

import com.takenos.investment.dto.BuyRequestTO
import com.takenos.investment.dto.PurchaseTO
import com.takenos.investment.model.Purchase
import com.takenos.investment.service.PurchaseService
import jakarta.ws.rs.core.Response
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class PurchaseResourceTest extends Specification {

    PurchaseService purchaseService = Mock(PurchaseService)

    @Subject
    PurchaseResource purchaseResource = new PurchaseResource(purchaseService)

    def "buy should delegate to the service and return 201 with the purchase"() {
        given:
        def request = new BuyRequestTO(new BigDecimal("100"), new BigDecimal("25"), "payin:abc123")
        def purchase = new Purchase(UUID.randomUUID(), "user-1", new BigDecimal("100"),
                new BigDecimal("4.00000000"), new BigDecimal("25"), "payin:abc123", Instant.now())

        when:
        def response = purchaseResource.buy("user-1", request)

        then:
        1 * purchaseService.buy("user-1", request) >> purchase
        response.status == Response.Status.CREATED.statusCode
        response.entity == PurchaseTO.from(purchase)
    }
}
