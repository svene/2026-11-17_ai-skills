package org.acme.shop.feature.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;

/** Hidden check: the clear action answers like every action, 204 plus an HX-Trigger event. */
@QuarkusTest
class ExampleClearCheckTest {

	@Test
	void clearIs204WithAnEvent() {
		given().when().delete("/example/guests")
			.then().statusCode(204).header("HX-Trigger", not(emptyOrNullString()));
	}
}
