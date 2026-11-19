package org.acme.shop;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductResourceTest {

    @Test
    void addedProductIsListed() {
        given().contentType(ContentType.JSON)
            .body("{\"sku\":\"p-1\",\"name\":\"Pen\",\"priceCents\":150}")
            .when().post("/products")
            .then().statusCode(201);

        given().when().get("/products")
            .then().statusCode(200)
            .body("sku", hasItem("p-1"));
    }

    @Test
    void negativePriceIsRejected() {
        given().contentType(ContentType.JSON)
            .body("{\"sku\":\"p-2\",\"name\":\"Bad\",\"priceCents\":-1}")
            .when().post("/products")
            .then().statusCode(400);
    }
}
