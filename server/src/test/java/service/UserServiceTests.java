package service;

import dataaccess.DataAccessException;
import dataaccess.AuthDataAccessMemory;
import dataaccess.UserDataAccessMemory;
import org.junit.jupiter.api.*;
import services.AuthService;
import services.UserService;
import services.RequestsRecords.LoginRequest;
import services.RequestsRecords.RegisterRequest;
import services.ResultsRecords.LoginResult;
import services.ResultsRecords.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTests {

    static AuthService authService;
    static UserService userService;

    @BeforeEach
    void setup() {
        authService = new AuthService(new AuthDataAccessMemory());
        userService = new UserService(new UserDataAccessMemory(), authService);
    }

    @Test
    @Order(1)
    @DisplayName("register - positive")
    void testRegisterPositive() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("newUser", "newPassword", "testemail@gmail.com");

        RegisterResult result = userService.register(request);

        assertNotNull(result);

        assertNotNull(result.authToken());
    }

    @Test
    @Order(2)
    @DisplayName("register - negative")
    void testRegisterNegative() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("newUser", "newPassword", "testemail@gmail.com");
        userService.register(request);

        RegisterResult result2 = userService.register(request);

        assertNull(result2);
    }

    @Test
    @Order(3)
    @DisplayName("login - positive")
    void testLoginPositive() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("newUser", "newPassword", "testemail@gmail.com");
        userService.register(request);

        LoginRequest loginRequest = new LoginRequest("newUser", "newPassword");

        LoginResult loginResult = userService.login(loginRequest);

        assertNotNull(loginResult);
    }

    @Test
    @Order(4)
    @DisplayName("login - negative")
    void testLoginNegative() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("fakeUser", "newPassword");

        LoginResult loginResult = userService.login(loginRequest);

        assertNull(loginResult);
    }

    @Test
    @Order(5)
    @DisplayName("verifyCredentials - positive")
    void testVerifyCredentialsPositive() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("newUser", "newPassword", "testemail@gmail.com");

        userService.register(request);

        boolean isValid = userService.verifyCredentials("newUser", "newPassword");

        assertTrue(isValid);
    }

    @Test
    @Order(6)
    @DisplayName("verifyCredentials - negative")
    void testVerifyCredentialsNegative() throws DataAccessException {
        boolean isValid = userService.verifyCredentials("newUser", "newPassword");

        assertFalse(isValid);
    }
}
