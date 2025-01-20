import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import request.CreateOrderRequest;

import java.util.List;

import static constants.DataConstants.*;
import static org.hamcrest.Matchers.*;

public class CreateOrderTest extends BaseApiTest {
    @Before
    public void prepare() throws Exception {
        createTestUser();
    }

    @Test
    @Description("Создание заказа с авторизацией")
    public void createOrderWithAuthorization() throws Exception {
        token = callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue())
                .extract().path(ACCESS_TOKEN);

        Response response = callGetIngredients();
        String ingredientId1 = response.path("data[0]._id");
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(List.of(ingredientId1));
        String orderBody = objectMapper.writeValueAsString(createOrderRequest);
        callCreateOrder(orderBody, token)
                .then()
                .statusCode(HTTP_OK)
                .body(ORDER_NUMBER, notNullValue());
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void createOrderWithoutAuthorization() throws Exception {
        Response response = callGetIngredients();
        String ingredientId1 = response.path("data[0]._id");
        String ingredientId2 = response.path("data[1]._id");
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(List.of(ingredientId1, ingredientId2));
        String orderBody = objectMapper.writeValueAsString(createOrderRequest);
        callCreateOrder(orderBody, null)
                .then()
                .statusCode(HTTP_OK)
                .body(ORDER_NUMBER, notNullValue());
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() throws Exception {
        token = callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue())
                .extract().path(ACCESS_TOKEN);

        CreateOrderRequest createOrderRequest = new CreateOrderRequest(List.of());
        String orderBody = objectMapper.writeValueAsString(createOrderRequest);
        callCreateOrder(orderBody, token)
                .then()
                .statusCode(BAD_REQUEST)
                .body(MESSAGE, equalTo(INGREDIENT_IDS_MUST_BE_PROVIDED));
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidHash() throws Exception {
        String orderBody = "{\"ingredients\": [\"" + INVALID_INGREDIENT_ID + "\"]}";

        token = callLogin(email, password)
                .then()
                .statusCode(HTTP_OK)
                .body(ACCESS_TOKEN, notNullValue())
                .extract().path(ACCESS_TOKEN);


        callCreateOrder(orderBody, token)
                .then()
                .statusCode(INTERNAL_SERVER_ERROR_CODE)
                .body(containsString(INTERNAL_SERVER_ERROR));
    }
}
