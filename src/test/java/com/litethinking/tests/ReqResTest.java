package com.litethinking.tests;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEmptyString.emptyString;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReqResTest {

    private final String ENDPOINT_USERS = "https://reqres.in/api/users";
    private final Path schemasPath = Paths.get("src", "test", "resources", "schemas");
    private final String schemasPathString = schemasPath.toFile().getAbsolutePath();

    private String userCreatedId = "0" ;

    @BeforeAll
    public static void setup() {
        System.out.println("------Start Tests -----");
    }

    @Test
    @Order(1)
    public void validatePageTwoUsers() {

        File schema = new File(schemasPathString + "/users_get.json");

        given().queryParam("page", "2")
                .when()
                .get(ENDPOINT_USERS)
                .then()
                .body(matchesJsonSchema(schema))
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Order(2)
    public void validateUserTwelve() {
        File schema = new File(schemasPathString + "/user_get.json");

        given().when()
                .pathParam("userId","12")
                .get(ENDPOINT_USERS+"/{userId}")
                .then()
                .body(matchesJsonSchema(schema))
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Order(3)
    public void validateCreateUser() {
        File schema = new File(schemasPathString + "/users_post.json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Test");
        requestBody.put("job", "leader");

        Response response = given()
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .when()
                .post(ENDPOINT_USERS)
                .andReturn();

        response.then()
                .body(matchesJsonSchema(schema))
                .statusCode(HttpStatus.SC_CREATED);

        userCreatedId = response.then().extract().path("id");
    }

    @Test
    @Order(4)
    public void validateModifyUser() {
        File schema = new File(schemasPathString + "/users_put.json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "Test Modify");
        requestBody.put("job", "leader Modify");

        Response response = given()
                .header("Content-Type", "application/json")
                .pathParam("userId",userCreatedId)
                .body(requestBody.toString())
                .when()
                .put(ENDPOINT_USERS+"/{userId}")
                .andReturn();

        response.then()
                .body(matchesJsonSchema(schema))
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Order(5)
    public void validateDeleteUser() {

        given()
                .pathParam("userId",userCreatedId)
                .when()
                .delete(ENDPOINT_USERS+"/{userId}")
                .then()
                .body(is(emptyString()))
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("------Close Tests -----");
    }
}
