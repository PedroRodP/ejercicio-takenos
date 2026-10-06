package com.takenos.investment.repository

import com.takenos.investment.model.Purchase
import spock.lang.Specification
import spock.lang.Subject

import java.time.Instant

class PurchaseRepositoryTest extends Specification {

    @Subject
    PurchaseRepository purchaseRepository = new PurchaseRepository()

    def "findByUserId should return purchases of the user in insertion order"() {
        given:
        def first = purchase("user-1", "payin:1")
        def second = purchase("user-1", "payin:2")
        def other = purchase("user-2", "payin:3")

        when:
        purchaseRepository.save(first)
        purchaseRepository.save(other)
        purchaseRepository.save(second)

        then:
        purchaseRepository.findByUserId("user-1") == [first, second]
        purchaseRepository.findByUserId("user-2") == [other]
    }

    def "findByUserId should return an empty list for an unknown user"() {
        expect:
        purchaseRepository.findByUserId("unknown").isEmpty()
    }

    def "findByUserId should return an unmodifiable copy"() {
        given:
        purchaseRepository.save(purchase("user-1", "payin:1"))

        when:
        purchaseRepository.findByUserId("user-1").add(purchase("user-1", "payin:2"))

        then:
        thrown(UnsupportedOperationException)
        purchaseRepository.findByUserId("user-1").size() == 1
    }

    def "deleteAll should remove every purchase"() {
        given:
        purchaseRepository.save(purchase("user-1", "payin:1"))

        when:
        purchaseRepository.deleteAll()

        then:
        purchaseRepository.findByUserId("user-1").isEmpty()
    }

    private static Purchase purchase(String userId, String reference) {
        new Purchase(UUID.randomUUID(), userId, new BigDecimal("100"), new BigDecimal("10"),
                new BigDecimal("10"), reference, Instant.now())
    }
}
