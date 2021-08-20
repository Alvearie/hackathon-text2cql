package org.alvearie;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class CQLGeneratorTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/health")
          .then()
             .statusCode(200)
             .body(is("Service is up and running"));
    }

}