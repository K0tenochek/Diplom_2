import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import request.RegistrationRequest;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetOrderFromSpecificUserTest {
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
    public void getOrderFromUserWithoutAuthorization() {
        given()
                .header("Content-type", "application/json")
                .log().all()
                .get("/api/orders")
                .then()
                .statusCode(401)
                .log().all()
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    public void getOrderFromUserWithAuthorization() {
        String jsonLogin = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}";
        given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .body(jsonLogin)
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .log().all()
                .body("accessToken", notNullValue());

        String ingredientId = "61c0c5a71d1f82001bdaaa70";
        String orderBody = "{\"ingredients\": [\"" + ingredientId + "\"]}";
        given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .log().all()
                .body(orderBody)
                .post("/api/orders")
                .then()
                .statusCode(200)
                .log().all()
                .body("order.number", notNullValue());

        given()
                .header("Content-type", "application/json")
                .header("Authorization", token)
                .log().all()
                .get("/api/orders")
                .then()
                .statusCode(200)
                .log().all()
                .body("orders.size()", greaterThan(0));
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
