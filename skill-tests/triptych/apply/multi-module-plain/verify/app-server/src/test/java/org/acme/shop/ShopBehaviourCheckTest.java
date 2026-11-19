package org.acme.shop;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/** Copied in by the skill test after the migration: the API behaves exactly as before. */
@QuarkusTest
class ShopBehaviourCheckTest {

    @Test
    void addThenList() {
        given().contentType(ContentType.JSON)
            .body("{\"sku\":\"check-1\",\"name\":\"Ink\",\"priceCents\":90}")
            .when().post("/products")
            .then().statusCode(201).body("sku", org.hamcrest.Matchers.equalTo("check-1"));

        given().when().get("/products")
            .then().statusCode(200).body("name", hasItem("Ink"));
    }

    @Test
    void negativePriceIsRejected() {
        given().contentType(ContentType.JSON)
            .body("{\"sku\":\"check-2\",\"name\":\"Bad\",\"priceCents\":-5}")
            .when().post("/products")
            .then().statusCode(400);
    }
}
