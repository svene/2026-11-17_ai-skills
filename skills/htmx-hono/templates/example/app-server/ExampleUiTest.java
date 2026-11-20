package @PACKAGE@.feature.example;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

/** The {route, vm} envelopes of the htmx example: what the Hono templates in the browser receive. */
@QuarkusTest
class ExampleUiTest {

	@Test
	void pageIsAnEnvelope() {
		given().accept(ContentType.JSON)
			.when().get("/uiroute/ExamplePage")
			.then().statusCode(200)
			.body("route", equalTo("ExamplePage"))
			.body("vm.form.name", equalTo(""));
	}

	@Test
	void unknownRouteIs404() {
		given().when().get("/uiroute/NoSuchRoute").then().statusCode(404);
	}

	@Test
	void blankNameIs400WithTheFormEnvelope() {
		given().contentType(ContentType.URLENC).formParam("name", " ")
			.when().post("/example/guests")
			.then().statusCode(400)
			.body("route", equalTo("ExampleSignForm"))
			.body("vm.errors.name", equalTo("must not be blank"));
	}

	@Test
	void signingIs204WithTheEvent() {
		given().contentType(ContentType.URLENC).formParam("name", "Ada")
			.when().post("/example/guests")
			.then().statusCode(204)
			.header("HX-Trigger", "ExampleGuestSigned");
		given().when().get("/uiroute/ExampleGuestList")
			.then().statusCode(200)
			.body("vm.names", hasItem("Ada"));
	}
}
