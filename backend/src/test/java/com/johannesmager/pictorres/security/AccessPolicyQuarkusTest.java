package com.johannesmager.pictorres.security;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Access rules from application.properties, verified against the real Dev Services Keycloak. */
@QuarkusTest
@Tag("NFR-002")
class AccessPolicyQuarkusTest {

  @Test
  @DisplayName("NFR-002/AC-4 browser navigation without a session is redirected to Keycloak")
  void redirectsAnonymousBrowserToLogin() {
    given()
        .redirects()
        .follow(false)
        .when()
        .get("/api/anything")
        .then()
        .statusCode(302)
        .header("Location", containsString("/realms/pic-torres/protocol/openid-connect/auth"));
  }

  @Test
  @DisplayName("NFR-002/AC-7 a signed-in user without the member role is denied")
  @TestSecurity(user = "carol")
  void deniesUserWithoutMemberRole() {
    given().when().get("/api/anything").then().statusCode(403);
  }

  @Test
  @DisplayName("NFR-002/AC-7 a signed-in member passes the access policy")
  @TestSecurity(
      user = "alice",
      roles = {"member"})
  void allowsMember() {
    // No endpoint exists yet, so passing the policy shows up as 404 instead of 401/403.
    given().when().get("/api/anything").then().statusCode(404);
  }

  @Test
  @DisplayName("NFR-006/AC-5 liveness and readiness are public for container health checks")
  void healthIsPublic() {
    given().when().get("/q/health/live").then().statusCode(200).body("status", is("UP"));
    given().when().get("/q/health/ready").then().statusCode(200).body("status", is("UP"));
  }
}
