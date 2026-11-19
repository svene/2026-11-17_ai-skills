package @PACKAGE@.feature.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/** Drives the example slice end to end: HTTP -> ExampleReceiver (inbound-http-jsonapi) -> ExampleHandler (core). */
@QuarkusTest
class ExampleReceiverTest {

    @Test
    void greets() {
        given().queryParam("name", "triptych")
            .when().get("/api/example")
            .then().statusCode(200).body("message", is("Hello, triptych"));
    }
}
