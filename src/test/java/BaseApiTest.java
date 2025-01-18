import io.qameta.allure.Step;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.After;
import org.junit.Before;
import request.AuthUserRequest;
import request.RegistrationRequest;

import java.util.Random;

import static constants.DataConstants.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BaseApiTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    protected String token;
    protected String email;
    protected String password = "qwerty";
    protected String name = "Tany";


    @Before
    public void setUp() {
        RestAssured.baseURI = LINK_FOR_MAIN_PAGE;
        objectMapper = new ObjectMapper();
        Random random = new Random();
        email = "test" + random.nextInt(120000) + "@yandex.ru";

    }

    protected void createTestUser() throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest(name, email, password);
        String jsonRegistration = objectMapper.writeValueAsString(registrationRequest);

        Response response = given()
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(jsonRegistration)
                .post(API_AUTH_REGISTER)
                .then()
                .assertThat()
                .statusCode(HTTP_OK)
                .body(USER_EMAIL, equalTo(email))
                .extract().response();
        token = response.jsonPath().getString(ACCESS_TOKEN);
    }


    @Step("Получения списка заказов")
    protected Response callGetOrder(String token) {
        RequestSpecification builder = given()
                .header(CONTENT_TYPE, APPLICATION_JSON);
        if (token != null) {
            builder = builder.header(AUTHORIZATION, token);
        }
        return builder.get(API_ORDERS);

    }

    @Step("Авторизация пользователя")
    protected Response callLogin(String email, String password) throws Exception {
        AuthUserRequest authUserRequest = new AuthUserRequest(email, password);
        return given()
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(objectMapper.writeValueAsBytes(authUserRequest))
                .post(API_AUTH_LOGIN);
    }

    @Step("Изменение поля email")
    protected Response callChangeEmail(String email, String token) {
        String jsonChange = String.format("{\"email\": \"%s\"}", email);
        RequestSpecification builder = given()
                .header(CONTENT_TYPE, APPLICATION_JSON);
        if (token != null) {
            builder = builder.header(AUTHORIZATION, token);
        }
        return builder
                .body(jsonChange)
                .patch(API_AUTH_USER);
    }

    @Step("Изменение поля name")
    protected Response callChangeName(String newName, String token) {
        String jsonChange = String.format("{\"name\": \"%s\"}", newName);
        return given()
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(AUTHORIZATION, token)
                .body(jsonChange)
                .patch(API_AUTH_USER);
    }


    @Step("Создание заказа")
    protected Response callCreateOrder(String orderBody, String token) {
        RequestSpecification builder = given()
                .header(CONTENT_TYPE, APPLICATION_JSON);
        if (token != null) {
            builder = builder.header(AUTHORIZATION, token);

        }
        return builder
                .body(orderBody)
                .post(API_ORDERS);
    }

    @Step("Создание пользователя")
    protected Response callCreateUser(String name, String email, String password) throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest(name, email, password);
        String json = objectMapper.writeValueAsString(registrationRequest);
        return given()
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(json)
                .post(API_AUTH_REGISTER);
    }

    @After
    public void deleteUser() {
        if (token != null) {
            given()
                    .header(AUTHORIZATION, token)
                    .delete(API_AUTH_USER)
                    .then();
        }
    }
}
