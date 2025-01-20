import constants.DataConstants;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import request.CreateOrderRequest;

import java.util.List;

import static constants.DataConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GetOrderFromSpecificUserTest extends BaseApiTest {
    @Before
    public void prepare() throws Exception {
        createTestUser();
    }

    @Test
    @Description("Получение заказа от неавторизованного пользователя")
    public void getOrderFromUserWithoutAuthorization() {
        callGetOrder(null)
                .then()
                .statusCode(UNAUTHORIZED)
                .body(DataConstants.MESSAGE, equalTo(UNAUTHORIZED_ERROR));
    }

    @Test
    @Description("Получение заказа от авторизованного пользователя")
    public void getOrderFromUserWithAuthorization() throws Exception {
        callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue());

       callGetOrder(token)
                .then()
                .statusCode(200)
                .body("orders.size()", equalTo(0));
    }

    @Test
    @Description("Создание и получение заказа от авторизованного пользователя")
    public void createAndGetOrderFromUserWithAuthorization() throws Exception {
        callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue());

        Response response = callGetIngredients();
        String ingredientId1 = response.path("data[0]._id");
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(List.of(ingredientId1));
        String body = objectMapper.writeValueAsString(createOrderRequest);
        callCreateOrder(body, token)
                .then()
                .statusCode(HTTP_OK)
                .body(DataConstants.ORDER_NUMBER, notNullValue());

        callGetOrder(token)
                .then()
                .statusCode(HTTP_OK)
                .body(ORDERS_SIZE, equalTo(1));
    }

}
