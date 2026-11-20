package org.acme.notes.feature.note;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/** Hidden check of the note page the agent built: the envelope, the 204 + HX-Trigger, the 400 form envelope. */
@QuarkusTest
class NoteUiCheckTest {

	@Test
	void pageIsAnEnvelope() {
		given().accept(ContentType.JSON)
			.when().get("/uiroute/NotePage")
			.then().statusCode(200).body("route", is("NotePage")).body("vm", notNullValue());
	}

	@Test
	void addingIs204WithAnEventAndShowsUpOnThePage() {
		given().contentType(ContentType.URLENC).formParam("text", "buy milk")
			.when().post("/note-actions/notes")
			.then().statusCode(204).header("HX-Trigger", not(emptyOrNullString()));
		given().accept(ContentType.JSON)
			.when().get("/uiroute/NotePage")
			.then().statusCode(200).body(containsString("buy milk"));
	}

	@Test
	void blankTextIs400WithTheFormEnvelope() {
		given().contentType(ContentType.URLENC).formParam("text", " ")
			.when().post("/note-actions/notes")
			.then().statusCode(400).body("route", notNullValue()).body("vm.errors.text", notNullValue());
	}
}
