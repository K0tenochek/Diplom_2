import io.qameta.allure.internal.shadowed.jackson.core.JsonProcessingException;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import request.AuthUserRequest;
import request.RegistrationRequest;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ChangeUserTest {
    String token;
    String email;
    String password;
    ObjectMapper objectMapper;

    @Before
    public void setUp() throws JsonProcessingException {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        objectMapper = new ObjectMapper();
        Random random = new Random();
        email = "test" + random.nextInt(120000) + "@yandex.ru";
        String name = "Tany";
        password = "qwerty";
        RegistrationRequest registrationRequest = new RegistrationRequest(name, email, password);
        String jsonRegistration = objectMapper.writeValueAsString(registrationRequest);

        Response response = given()
                .header("Content-type", "application/json")
                .body(jsonRegistration)
                .post("/api/auth/register")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("user.email", equalTo(email))
                .extract().response();
        token = response.jsonPath().getString("accessToken");

    }

    @Test
    public void loginAndChangeEmail() throws Exception {
        AuthUserRequest authUserRequest = new AuthUserRequest(email, password);
        String json = objectMapper.writeValueAsString(authUserRequest);
        String currentToken = given()
                .header("Content-type", "application/json")
                .header("Authorization",  token)
                .body(json)
                .post("/api/auth/login")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .extract().path("accessToken");
        Random random = new Random();
        String newEmail = "t" + random.nextInt(120000) +  "@yandex.ru";
        String jsonChange = String.format("{\"email\": \"%s\"}", newEmail);

        given()
                .header("Content-type", "application/json")
                .header("Authorization",  currentToken)
                .body(jsonChange)
                .patch("/api/auth/user")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("user.email", equalTo(newEmail));
    }

    @Test
    public void loginAndChangeName() throws Exception {
        AuthUserRequest authUserRequest = new AuthUserRequest(email, password);
        String json = objectMapper.writeValueAsString(authUserRequest);
        String currentToken = given()
                .header("Content-type", "application/json")
                .header("Authorization",  token)
                .body(json)
                .post("/api/auth/login")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .extract().path("accessToken");
        String name = "tanyusha";
        String jsonChange = String.format("{\"name\": \"%s\"}", name);

        given()
                .header("Content-type", "application/json")
                .header("Authorization",  currentToken)
                .body(jsonChange)
                .patch("/api/auth/user")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("user.name", equalTo(name));
    }

    @Test
    public void changeDataUserWithoutAuthorization() {
        String newName = "Slava";
        String jsonChange = "{\"name\": \"" + newName + "\"}";

        given()
                .header("Content-type", "application/json")
                .body(jsonChange)
                .patch("/api/auth/user")
                .then().log().all()
                .assertThat()
                .statusCode(401)
                .body("message", equalTo("You should be authorised"));
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
