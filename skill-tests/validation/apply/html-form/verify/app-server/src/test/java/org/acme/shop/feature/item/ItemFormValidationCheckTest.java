package org.acme.shop.feature.item;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

/**
 * Copied in by the skill test after the run: an invalid POST is a 400 carrying the form's own envelope, with the
 * submitted values unchanged and an error per field, so the client re-renders the form with inline errors.
 */
@QuarkusTest
class ItemFormValidationCheckTest {

    @Inject
    ItemHandler itemHandler;

    @Test
    void valid_post_adds_the_item() {
        given().formParam("name", "check-valid").formParam("quantity", "2")
            .when().post("/vm/items")
            .then().statusCode(204);
        assertThat(itemHandler.items()).extracting(Item::name).contains("check-valid");
    }

    @Test
    void non_numeric_quantity_is_400_with_the_form_and_its_error() {
        given().formParam("name", "check-text").formParam("quantity", "abc")
            .when().post("/vm/items")
            .then().statusCode(400)
            .body("route", is("ItemForm"))
            .body("vm.name", is("check-text"))
            .body("vm.quantity", is("abc"))
            .body("vm.errors.quantity", not(emptyOrNullString()));
        assertThat(itemHandler.items()).extracting(Item::name).doesNotContain("check-text");
    }

    @Test
    void blank_name_and_zero_quantity_are_400_with_errors() {
        given().formParam("name", " ").formParam("quantity", "1")
            .when().post("/vm/items")
            .then().statusCode(400).body("vm.errors.name", not(emptyOrNullString()));
        given().formParam("name", "check-zero").formParam("quantity", "0")
            .when().post("/vm/items")
            .then().statusCode(400).body("vm.errors.quantity", not(emptyOrNullString()));
    }
}
