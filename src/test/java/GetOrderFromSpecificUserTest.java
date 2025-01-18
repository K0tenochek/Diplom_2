import constants.DataConstants;
import org.junit.Before;
import org.junit.Test;

import static constants.DataConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GetOrderFromSpecificUserTest extends BaseApiTest {



    @Before
    public void prepare() throws Exception {
        createTestUser();
    }

    @Test
    public void getOrderFromUserWithoutAuthorization() {
        callGetOrder(null)
                .then()
                .statusCode(UNAUTHORIZED)
                .body(DataConstants.MESSAGE, equalTo(UNAUTHORIZED_ERROR));
    }

    @Test
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
    public void createAndGetOrderFromUserWithAuthorization() throws Exception {
        callLogin(email, password)
                .then()
                .statusCode(200)
                .body(ACCESS_TOKEN, notNullValue());

        String orderBody = "{\"ingredients\": [\"" + DataConstants.INGREDIENT_ID_1 + "\"]}";
        callCreateOrder(orderBody, token)
                .then()
                .statusCode(HTTP_OK)
                .body(DataConstants.ORDER_NUMBER, notNullValue());

        callGetOrder(token)
                .then()
                .statusCode(HTTP_OK)
                .body(ORDERS_SIZE, equalTo(1));
    }

}
