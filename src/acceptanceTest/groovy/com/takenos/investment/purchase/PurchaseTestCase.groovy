package com.takenos.investment.purchase

import com.takenos.investment.config.CommonTestCase
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.notNullValue

@QuarkusTest
class PurchaseTestCase extends CommonTestCase {

    @Test
    void "buy should register the purchase and return it"() {
        given()
                .contentType(ContentType.JSON)
                .body([amount_usd: 100, price_per_unit: 25, reference: "payin:abc123"])
        .when()
                .post("/v1/users/user-1/purchases")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("user_id", equalTo("user-1"))
                .body("amount_usd", equalTo(100))
                .body("units", equalTo(4.0f))
                .body("remaining_units", equalTo(4.0f))
                .body("price_per_unit", equalTo(25))
                .body("reference", equalTo("payin:abc123"))
                .body("created", notNullValue())

        def purchases = purchaseRepository.findByUserId("user-1")
        assert purchases.size() == 1
        assert purchases[0].units == new BigDecimal("4.00000000")
    }

    @Test
    void "buy should register the purchase again when the reference is repeated"() {
        2.times {
            given()
                    .contentType(ContentType.JSON)
                    .body([amount_usd: 100, price_per_unit: 25, reference: "payin:abc123"])
            .when()
                    .post("/v1/users/user-1/purchases")
            .then()
                    .statusCode(201)
        }

        assert purchaseRepository.findByUserId("user-1").size() == 2
    }

    @Test
    void "buy should return 400 when the request is invalid"() {
        given()
                .contentType(ContentType.JSON)
                .body([amount_usd: 0, price_per_unit: -1, reference: ""])
        .when()
                .post("/v1/users/user-1/purchases")
        .then()
                .statusCode(400)
                .body("title", equalTo("Invalid request"))
                .body("status", equalTo(400))
                .body("sub_errors", hasSize(3))
                .body("sub_errors.title", containsInAnyOrder(
                        "buy.request.amountUsd", "buy.request.pricePerUnit", "buy.request.reference"))

        assert purchaseRepository.findByUserId("user-1").isEmpty()
    }

    @Test
    void "buy should return 400 when the body is missing"() {
        given()
                .contentType(ContentType.JSON)
        .when()
                .post("/v1/users/user-1/purchases")
        .then()
                .statusCode(400)
    }
}
