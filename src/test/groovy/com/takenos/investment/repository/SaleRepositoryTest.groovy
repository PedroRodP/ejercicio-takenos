package com.takenos.investment.repository

import com.takenos.investment.model.Sale
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class SaleRepositoryTest extends Specification {

    @Subject
    SaleRepository saleRepository = new SaleRepository()

    def "findByUserId should return sales of the user in insertion order"() {
        given:
        def first = sale("user-1")
        def second = sale("user-1")
        def other = sale("user-2")

        when:
        saleRepository.save(first)
        saleRepository.save(other)
        saleRepository.save(second)

        then:
        saleRepository.findByUserId("user-1") == [first, second]
        saleRepository.findByUserId("user-2") == [other]
        saleRepository.findByUserId("unknown").isEmpty()
    }

    def "deleteAll should remove every sale"() {
        given:
        saleRepository.save(sale("user-1"))

        when:
        saleRepository.deleteAll()

        then:
        saleRepository.findByUserId("user-1").isEmpty()
    }

    private static Sale sale(String userId) {
        new Sale(UUID.randomUUID(), userId, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ONE,
                new BigDecimal("9"), [], Instant.now())
    }
}
