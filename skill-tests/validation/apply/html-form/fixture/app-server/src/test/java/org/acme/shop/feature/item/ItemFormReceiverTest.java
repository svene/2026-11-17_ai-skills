package org.acme.shop.feature.item;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ItemFormReceiverTest {

    @Test
    void shows_the_empty_form() {
        given().when().get("/vm/items")
            .then().statusCode(200).body("route", is("ItemForm")).body("vm.name", is(""));
    }

    @Test
    void adds() {
        given().formParam("name", "bolt").formParam("quantity", "5")
            .when().post("/vm/items")
            .then().statusCode(204).header("HX-Trigger", "itemAdded");
    }
}
