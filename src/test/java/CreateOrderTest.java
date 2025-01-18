import org.junit.Before;
import org.junit.Test;

import static constants.DataConstants.*;
import static org.hamcrest.Matchers.*;

public class CreateOrderTest extends BaseApiTest {
    @Before
    public void prepare() throws Exception {
        createTestUser();
    }

    @Test
    public void createOrderWithAuthorization() throws Exception {
        token = callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue())
                .extract().path(ACCESS_TOKEN);

        String orderBody = "{\"ingredients\": [\"" + INGREDIENT_ID_1 + "\"]}";
        callCreateOrder(orderBody, token)
                .then()
                .statusCode(HTTP_OK)
                .body(ORDER_NUMBER, notNullValue());
    }

    @Test
    public void createOrderWithoutAuthorization() {
        String orderBody = "{\"ingredients\": [\"" + INGREDIENT_ID_1 + "\", \"" + INGREDIENT_ID_2 + "\"]}";

        callCreateOrder(orderBody, null)
                .then()
                .statusCode(HTTP_OK)
                .body(ORDER_NUMBER, notNullValue());
    }

    @Test
    public void createOrderWithoutIngredients() throws Exception {
        token = callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue())
                .extract().path(ACCESS_TOKEN);

        String orderBody = "{\"ingredients\": []}";

        callCreateOrder(orderBody, token)
                .then()
                .statusCode(BAD_REQUEST)
                .body(MESSAGE, equalTo(INGREDIENT_IDS_MUST_BE_PROVIDED));
    }

    @Test
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
