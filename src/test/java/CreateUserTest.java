import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import request.RegistrationRequest;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserTest {
    String token;
    ObjectMapper objectMapper = new ObjectMapper();
    String password = "qwerty";
    String name = "Tany";
    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }


    @Test
    public void createUniqueUser() throws Exception {
        Random random = new Random();
        String email = "test" + random.nextInt(120000) + "@yandex.ru";
        RegistrationRequest registrationRequest = new RegistrationRequest(name, email, password);

        String json = objectMapper.writeValueAsString(registrationRequest);

        Response response = given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/auth/register")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("user.email", equalTo(email))
                .extract().response();
        token = response.jsonPath().getString("accessToken");

    }

    @Test
    public void createTheSameUser() throws Exception {
        Random random = new Random();
        String email = "test" + random.nextInt(120000) + "@yandex.ru";
        RegistrationRequest registrationRequest = new RegistrationRequest(name, email, password);
        String json = objectMapper.writeValueAsString(registrationRequest);

        Response response = given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/auth/register")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("user.email", equalTo(email))
                .extract().response();
        token = response.jsonPath().getString("accessToken");

        given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/auth/register")
                .then().log().all()
                .assertThat()
                .statusCode(403)
                .body("message", equalTo("User already exists"));
    }

    @Test
    public void createUserWithMissingField() throws Exception {
        Random random = new Random();
        String email = "test" + random.nextInt(120000) + "@yandex.ru";
        RegistrationRequest registrationRequest = new RegistrationRequest("", email, password);
        String json = objectMapper.writeValueAsString(registrationRequest);

        given()
                .header("Content-type", "application/json")
                .body(json)
                .post("/api/auth/register")
                .then().log().all()
                .assertThat()
                .statusCode(403)
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void deleteUser() {
        if (token != null) {
            given()
                    .header("Authorization", token)
                    .delete("/api/auth/user")
                    .then().log().all();
        }
    }
}
