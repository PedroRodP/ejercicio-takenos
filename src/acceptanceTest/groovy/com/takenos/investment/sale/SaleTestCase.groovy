package com.takenos.investment.sale

import com.takenos.investment.config.CommonTestCase
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.notNullValue

@QuarkusTest
class SaleTestCase extends CommonTestCase {

    @Test
    void "sell should consume purchases in FIFO order and return the realized profit"() {
        def oldestId = buy(100, 10, "payin:1")   // 10 units @ 10
        def newestId = buy(100, 20, "payin:2")   // 5 units @ 20

        given()
                .contentType(ContentType.JSON)
                .body([units: 12, price_per_unit: 25])
        .when()
                .post("/v1/users/user-1/sales")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("user_id", equalTo("user-1"))
                .body("units", equalTo(12))
                .body("price_per_unit", equalTo(25))
                .body("proceeds_usd", equalTo(300))
                .body("cost_usd", equalTo(140.0f))
                .body("realized_profit_usd", equalTo(160.0f))
                .body("consumed_purchases", hasSize(2))
                .body("consumed_purchases[0].purchase_id", equalTo(oldestId))
                .body("consumed_purchases[0].units", equalTo(10.0f))
                .body("consumed_purchases[1].purchase_id", equalTo(newestId))
                .body("consumed_purchases[1].units", equalTo(2.0f))
                .body("created", notNullValue())

        def purchases = purchaseRepository.findByUserId("user-1")
        assert purchases*.remainingUnits == [0, 3]
        assert saleRepository.findByUserId("user-1").size() == 1
    }

    @Test
    void "sell should return 422 when the user has not enough units"() {
        buy(100, 10, "payin:1")

        given()
                .contentType(ContentType.JSON)
                .body([units: 11, price_per_unit: 25])
        .when()
                .post("/v1/users/user-1/sales")
        .then()
                .statusCode(422)
                .body("title", equalTo("Insufficient units"))

        assert purchaseRepository.findByUserId("user-1")*.remainingUnits == [10]
        assert saleRepository.findByUserId("user-1").isEmpty()
    }

    private static String buy(int amountUsd, int pricePerUnit, String reference) {
        given()
                .contentType(ContentType.JSON)
                .body([amount_usd: amountUsd, price_per_unit: pricePerUnit, reference: reference])
        .when()
                .post("/v1/users/user-1/purchases")
        .then()
                .statusCode(201)
                .extract().path("id")
    }
}
