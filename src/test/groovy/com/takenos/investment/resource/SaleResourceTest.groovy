package com.takenos.investment.resource

import com.takenos.investment.dto.SaleTO
import com.takenos.investment.dto.SellRequestTO
import com.takenos.investment.model.ConsumedPurchase
import com.takenos.investment.model.Sale
import com.takenos.investment.service.SaleService
import jakarta.ws.rs.core.Response
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class SaleResourceTest extends Specification {

    SaleService saleService = Mock(SaleService)

    @Subject
    SaleResource saleResource = new SaleResource(saleService)

    def "sell should delegate to the service and return 201 with the sale"() {
        given:
        def request = new SellRequestTO(new BigDecimal("4"), new BigDecimal("15"))
        def consumed = new ConsumedPurchase(UUID.randomUUID(), new BigDecimal("4"), new BigDecimal("10"), new BigDecimal("40"))
        def sale = new Sale(UUID.randomUUID(), "user-1", new BigDecimal("4"), new BigDecimal("15"),
                new BigDecimal("60"), new BigDecimal("40"), new BigDecimal("20"), [consumed], Instant.now())

        when:
        def response = saleResource.sell("user-1", request)

        then:
        1 * saleService.sell("user-1", request) >> sale
        response.status == Response.Status.CREATED.statusCode
        response.entity == SaleTO.from(sale)
    }
}
