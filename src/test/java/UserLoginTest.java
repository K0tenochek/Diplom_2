import org.junit.Before;
import org.junit.Test;

import static constants.DataConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserLoginTest extends BaseApiTest {
    @Before
    public void prepare() throws Exception {
        createTestUser();
    }

    @Test
    public void authorizationUser() throws Exception {
        callLogin(email, password)
                .then()
                .statusCode(HTTP_OK)
                .body(ACCESS_TOKEN, notNullValue());
    }

    @Test
    public void authorizationUserWithIncorrectLogin() throws Exception{
        callLogin(INCORRECT_EMAIL, password)
                .then()
                .statusCode(UNAUTHORIZED)
                .body(MESSAGE, equalTo(EMAIL_OR_PASSWORD_ARE_INCORRECT));
    }

    @Test
    public void authorizationUserWithIncorrectPassword() throws Exception {
        callLogin(email, INCORRECT_PASSWORD)
                .then()
                .statusCode(UNAUTHORIZED)
                .body(MESSAGE, equalTo(EMAIL_OR_PASSWORD_ARE_INCORRECT));
    }
}



