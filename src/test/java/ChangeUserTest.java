import constants.DataConstants;
import io.qameta.allure.Description;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.hamcrest.Matchers.equalTo;

public class ChangeUserTest extends BaseApiTest {

    public static final String YOU_SHOULD_BE_AUTHORISED = "You should be authorised";

    @Before
    public void prepare() throws Exception {
        createTestUser();
    }

    @Test
    @Description("Изменение email с авторизацией")
    public void loginAndChangeEmail() throws Exception {
        String currentToken = callLogin(email, password)
                .then()
                .assertThat()
                .statusCode(DataConstants.HTTP_OK)
                .extract().path("accessToken");
        Random random = new Random();
        String newEmail = "t" + random.nextInt(120000) + "@yandex.ru";
        callChangeEmail(newEmail, currentToken)
                .then()
                .assertThat()
                .statusCode(DataConstants.HTTP_OK)
                .body("user.email", equalTo(newEmail));
    }

    @Test
    @Description("Изменение имени с авторизацией")
    public void loginAndChangeName() throws Exception {
        String currentToken = callLogin(email, password)
                .then()
                .assertThat()
                .statusCode(DataConstants.HTTP_OK)
                .extract().path(DataConstants.ACCESS_TOKEN);
        String newName = "tanyusha";
        callChangeName(newName, currentToken)
                .then()
                .assertThat()
                .statusCode(DataConstants.HTTP_OK)
                .body("user.name", equalTo(newName));
    }

    @Test
    @Description("Изменение email без авторизаци")
    public void changeDataEmailWithoutAuthorization() {
        String newEmail = "Slava@yandex.ru";

        callChangeEmail(newEmail, null)
                .then()
                .assertThat()
                .statusCode(DataConstants.UNAUTHORIZED)
                .body(DataConstants.MESSAGE, equalTo(YOU_SHOULD_BE_AUTHORISED));
    }

}
