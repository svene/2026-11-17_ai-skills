package org.acme.shop.feature.item;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ItemReceiverTest {

    @Test
    void adds() {
        given().contentType("application/json").body("{\"name\": \"bolt\", \"quantity\": 5}")
            .when().post("/items")
            .then().statusCode(201).body("message", is("5 x bolt"));
    }
}
