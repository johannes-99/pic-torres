package com.johannesmager.pictorres;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Smoke test of the packaged jar (failsafe), with Dev Services providing Postgres and Keycloak. */
@QuarkusIntegrationTest
@Tag("NFR-004")
class PackagedApplicationIT {

  @Test
  @DisplayName("NFR-004/AC-3 the packaged application starts and reports ready")
  void packagedApplicationIsReady() {
    given().when().get("/q/health/ready").then().statusCode(200).body("status", is("UP"));
  }
}
