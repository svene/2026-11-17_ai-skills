package org.acme.shop.feature.item;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;

/** Copied in by the skill test after the run: POST /items answers 201, or 400 with [{field, message}]. */
@QuarkusTest
class ItemValidationCheckTest {

    @Inject
    ItemHandler itemHandler;

    @Test
    void valid_item_is_created() {
        given().contentType("application/json").body("{\"name\": \"check-valid\", \"quantity\": 2}")
            .when().post("/items")
            .then().statusCode(201);
        assertThat(itemHandler.items()).extracting(Item::name).contains("check-valid");
    }

    @Test
    void quantity_not_positive_is_400_naming_the_field() {
        given().contentType("application/json").body("{\"name\": \"check-zero\", \"quantity\": 0}")
            .when().post("/items")
            .then().statusCode(400).body("field", hasItem("quantity"));
        assertThat(itemHandler.items()).extracting(Item::name).doesNotContain("check-zero");
    }

    @Test
    void blank_name_is_400_naming_the_field() {
        given().contentType("application/json").body("{\"name\": \" \", \"quantity\": 1}")
            .when().post("/items")
            .then().statusCode(400).body("field", hasItem("name"));
    }
}
