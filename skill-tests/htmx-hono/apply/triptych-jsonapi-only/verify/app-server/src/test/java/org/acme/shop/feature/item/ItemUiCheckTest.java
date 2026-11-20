package org.acme.shop.feature.item;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

/** Hidden check of the item page the agent built: the envelope, the 204 + HX-Trigger, the 400 form envelopes. */
@QuarkusTest
class ItemUiCheckTest {

	@Test
	void pageIsAnEnvelope() {
		given().accept(ContentType.JSON)
			.when().get("/uiroute/ItemPage")
			.then().statusCode(200).body("route", is("ItemPage")).body("vm", notNullValue());
	}

	@Test
	void addingIs204WithAnEventAndShowsUpOnThePage() {
		given().contentType(ContentType.URLENC).formParam("name", "sprocket").formParam("quantity", "7")
			.when().post("/item-actions/items")
			.then().statusCode(204).header("HX-Trigger", not(emptyOrNullString()));
		given().accept(ContentType.JSON)
			.when().get("/uiroute/ItemPage")
			.then().statusCode(200).body(containsString("sprocket"));
	}

	@Test
	void blankNameIs400WithTheFormEnvelope() {
		given().contentType(ContentType.URLENC).formParam("name", " ").formParam("quantity", "2")
			.when().post("/item-actions/items")
			.then().statusCode(400).body("route", notNullValue()).body("vm.errors.name", notNullValue());
	}

	@Test
	void zeroQuantityIs400WithTheFormEnvelope() {
		given().contentType(ContentType.URLENC).formParam("name", "bolt").formParam("quantity", "0")
			.when().post("/item-actions/items")
			.then().statusCode(400).body("route", notNullValue()).body("vm.errors.quantity", notNullValue());
	}
}
