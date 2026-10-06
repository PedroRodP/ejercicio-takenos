package com.takenos.investment.position

import com.takenos.investment.config.CommonTestCase
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import org.junit.jupiter.api.Test

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo

@QuarkusTest
class PositionTestCase extends CommonTestCase {

    @Test
    void "position should return zeros for a user without operations"() {
        given()
        .when()
                .get("/v1/users/user-1/position")
        .then()
                .statusCode(200)
                .body("user_id", equalTo("user-1"))
                .body("units", equalTo(0))
                .body("cost_usd", equalTo(0))
                .body("realized_profit_usd", equalTo(0))
    }

    @Test
    void "position should reflect remaining units, their cost and the realized profit after FIFO sales"() {
        buy(100, 10, "payin:1")   // 10 units @ 10
        buy(100, 20, "payin:2")   // 5 units @ 20
        sell(12, 25)              // consumes 10 @ 10 + 2 @ 20 -> profit 300 - 140 = 160
        sell(1, 15)               // consumes 1 @ 20 -> profit 15 - 20 = -5

        given()
        .when()
                .get("/v1/users/user-1/position")
        .then()
                .statusCode(200)
                .body("user_id", equalTo("user-1"))
                .body("units", equalTo(2.0f))
                .body("cost_usd", equalTo(40.0f))
                .body("realized_profit_usd", equalTo(155.0f))
    }

    private static void buy(int amountUsd, int pricePerUnit, String reference) {
        given()
                .contentType(ContentType.JSON)
                .body([amount_usd: amountUsd, price_per_unit: pricePerUnit, reference: reference])
        .when()
                .post("/v1/users/user-1/purchases")
        .then()
                .statusCode(201)
    }

    private static void sell(int units, int pricePerUnit) {
        given()
                .contentType(ContentType.JSON)
                .body([units: units, price_per_unit: pricePerUnit])
        .when()
                .post("/v1/users/user-1/sales")
        .then()
                .statusCode(201)
    }
}
