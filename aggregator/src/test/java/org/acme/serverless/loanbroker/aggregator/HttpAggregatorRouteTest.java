package org.acme.serverless.loanbroker.aggregator;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class HttpAggregatorRouteTest {

    @Test
    void verifyOneMessageAggregated() {

        RestAssured.given()
                .header("Content-Type", "application/json")
                .header("ce-kogitoprocinstanceid", "123")
                .body("{\"rate\":4.655600086643112,\"bankId\":\"BankPremium\"}")
                .when()
                .post("/test")
                .then()
                .statusCode(200);
    }

}
