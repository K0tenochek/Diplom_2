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
import static org.hamcrest.Matchers.notNullValue;

public class UserLoginTest {
    String token;
    ObjectMapper objectMapper;
    String email;
    String password;
    String name;

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        objectMapper = new ObjectMapper();

        Random random = new Random();
        email = "test" + random.nextInt(120000) + "@yandex.ru";
        name = "Tany";
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
    public void authorizationUser() {
        String jsonLogin = "{\"email\": \"" + email + "\", \"password\": \"qwerty\"}";
        given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(jsonLogin)
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .log().all()
                .body("accessToken", notNullValue());
    }

    @Test
    public void authorizationUserWithIncorrectLogin() throws Exception {
        String incorrectEmail = "Tany123";
        AuthUserRequest authUserRequest = new AuthUserRequest(incorrectEmail, password);
        String jsonLogin = objectMapper.writeValueAsString(authUserRequest);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(jsonLogin)
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("message", equalTo("email or password are incorrect"))
                .log().all();
    }

    @Test
    public void authorizationUserWithIncorrectPassword() throws Exception {
        String incorrectPassword = "1234";
        AuthUserRequest authUserRequest = new AuthUserRequest(name, incorrectPassword);
        String jsonLogin = objectMapper.writeValueAsString(authUserRequest);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(jsonLogin)
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("message", equalTo("email or password are incorrect"))
                .log().all();
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



