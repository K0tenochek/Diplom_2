import constants.DataConstants;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Test;

import java.util.Random;

import static constants.DataConstants.*;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserTest extends BaseApiTest {
    @Test
    @Description("Создание уникального пользователя")
    public void createUniqueUser() throws Exception {
        Random random = new Random();
        String email = "test" + random.nextInt(120000) + "@yandex.ru";
        Response response = callCreateUser(name, email, password)
                .then()
                .log().all()
                .assertThat()
                .statusCode(HTTP_OK)
                .body(USER_EMAIL, equalTo(email))
                .extract().response();
        token = response.jsonPath().getString(ACCESS_TOKEN);

    }

    @Test
    @Description("Создание пользователя, который уже зарегистрирован")
    public void createTheSameUser() throws Exception {
        Random random = new Random();
        String email = "test" + random.nextInt(120000) + "@yandex.ru";
        Response response = callCreateUser(name, email, password)
                .then()
                .assertThat()
                .statusCode(HTTP_OK)
                .body("user.email", equalTo(email))
                .extract().response();
        token = response.jsonPath().getString(ACCESS_TOKEN);

       callCreateUser(name, email, password)
                .then()
                .assertThat()
                .statusCode(FORBIDDEN)
                .body(DataConstants.MESSAGE, equalTo(USER_ALREADY_EXISTS));
    }

    @Test
    @Description("Создание пользователя с незаполнением одного из обязательных полей")
    public void createUserWithMissingField() throws Exception {
        Random random = new Random();
        String email = "test" + random.nextInt(120000) + "@yandex.ru";
        callCreateUser("", email, password)
                .then()
                .assertThat()
                .statusCode(FORBIDDEN)
                .body(DataConstants.MESSAGE, equalTo(REQUIRED_FIELDS_ERROR));
    }
}
