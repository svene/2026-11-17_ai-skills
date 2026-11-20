package org.acme.shop.feature.item;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class ItemUiTest {

    @Test
    void shows_the_empty_form() {
        given().accept(ContentType.JSON).when().get("/uiroute/ItemForm")
            .then().statusCode(200).body("route", is("ItemForm")).body("vm.name", is(""));
    }

    @Test
    void adds() {
        given().formParam("name", "bolt").formParam("quantity", "5")
            .when().post("/item/items")
            .then().statusCode(204).header("HX-Trigger", "ItemAdded");
    }
}
