package com.takenos.investment.model

import spock.lang.Specification

import java.time.Instant

class PurchaseTest extends Specification {

    def "a new purchase should have all its units remaining"() {
        expect:
        purchase("10").remainingUnits == 10
        purchase("10").hasRemainingUnits()
    }

    def "consume should take #requested units out of 10 and leave #remaining"() {
        given:
        def purchase = purchase("10")

        when:
        def consumed = purchase.consume(new BigDecimal(requested))

        then:
        consumed == new BigDecimal(expectedConsumed)
        purchase.remainingUnits == new BigDecimal(remaining)
        purchase.hasRemainingUnits() == (remaining != "0")

        where:
        requested || expectedConsumed | remaining
        "4"       || "4"              | "6"
        "10"      || "10"             | "0"
        "15"      || "10"             | "0"
    }

    private static Purchase purchase(String units) {
        new Purchase(UUID.randomUUID(), "user-1", new BigDecimal("100"), new BigDecimal(units),
                new BigDecimal("10"), "payin:1", Instant.now())
    }
}
