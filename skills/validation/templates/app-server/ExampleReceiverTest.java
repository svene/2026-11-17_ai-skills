package @PACKAGE@.feature.example;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/** Drives the example slice end to end: HTTP -> ExampleReceiver (inbound-http-jsonapi) -> ExampleHandler (core). */
@QuarkusTest
class ExampleReceiverTest {

    @Inject
    ExampleHandler exampleHandler;

    @Test
    void greets() {
        given().queryParam("name", "triptych")
            .when().get("/api/example")
            .then().statusCode(200).body("message", is("Hello, triptych"));
    }

    @Test
    void accepts_validInput() {
        given().contentType("application/json").body("{\"name\": \"accepted-widget\", \"count\": 3}")
            .when().post("/api/example")
            .then().statusCode(201).body("message", is("3 x accepted-widget"));
        assertThat(exampleHandler.accepted()).contains(new Example("accepted-widget", 3));
    }

    @Test
    void accepts_countAsText() {
        given().contentType("application/json").body("{\"name\": \"text-widget\", \"count\": \"4\"}")
            .when().post("/api/example")
            .then().statusCode(201);
    }

    @Test
    void rejects_everyViolation_withFieldAndMessage() {
        given().contentType("application/json").body("{\"name\": \" \", \"count\": \"abc\"}")
            .when().post("/api/example")
            .then().statusCode(400)
            .body("field", contains("count", "name"))
            .body("message", contains("must be a number", "must not be blank"));
    }

    @Test
    void rejects_countNotPositive() {
        given().contentType("application/json").body("{\"name\": \"widget\", \"count\": 0}")
            .when().post("/api/example")
            .then().statusCode(400)
            .body("field", contains("count"))
            .body("message", contains("must be greater than 0"));
    }

    @Test
    void rejects_nonNumericCount() {
        given().contentType("application/json").body("{\"name\": \"rejected-widget\", \"count\": \"abc\"}")
            .when().post("/api/example")
            .then().statusCode(400)
            .body("field", contains("count"))
            .body("message", contains("must be a number"));
        assertThat(exampleHandler.accepted()).extracting(Example::name).doesNotContain("rejected-widget");
    }

    @Test
    void rejects_missingFields() {
        given().contentType("application/json").body("{}")
            .when().post("/api/example")
            .then().statusCode(400)
            .body("field", contains("count", "name"));
    }
}
