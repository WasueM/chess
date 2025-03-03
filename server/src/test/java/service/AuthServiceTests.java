package service;

import dataaccess.AuthDataAccessMemory;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import services.AuthService;
import services.RequestsRecords.LogoutRequest;
import services.ResultsRecords.LogoutResult;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTests {

    static AuthService authService;

    @BeforeEach
    public void setup() {
        authService = new AuthService(new AuthDataAccessMemory());
    }

    @Test
    @Order(1)
    @DisplayName("authenticate - positive")
    void testAuthenticateUserPositive() throws DataAccessException {
        String username = "testUser";

        // this should return a valid auth token
        String authToken = authService.authenticateUser(username);

        assertNotNull(authToken);
    }

    @Test
    @Order(2)
    @DisplayName("authenticate - negative")
    void testAuthenticateUserNegative() throws DataAccessException {
        String username = "";

        // should fail since there's no real username
        assertThrows(DataAccessException.class, () -> {
            authService.authenticateUser(username);
        });
    }

    @Test
    @Order(3)
    @DisplayName("check token - positive")
    void testCheckAuth() throws DataAccessException {
        String username = "testUser";

        // this should return a valid auth token
        String authToken = authService.authenticateUser(username);

        boolean result = authService.verifyAuthToken(authToken);

        assertTrue(result);
    }

    @Test
    @Order(4)
    @DisplayName("check token - negative")
    void testCheckAuthNegative() throws DataAccessException {

        boolean result = authService.verifyAuthToken("1234");

        assertFalse(result);
    }

    @Test
    @Order(5)
    @DisplayName("check logout - negative")
    void testLogoutNegative() throws DataAccessException {

       LogoutResult result = authService.logout(new LogoutRequest("1234"));

       assertNull(result);
    }

    @Test
    @Order(6)
    @DisplayName("check logout - positive")
    void testLogoutPositive() throws DataAccessException {
        String username = "testUser";

        String authToken = authService.authenticateUser(username);

        LogoutResult result = authService.logout(new LogoutRequest(authToken));

        assertNotNull(result);
    }

    @Test
    @Order(7)
    @DisplayName("get user from token - positive")
    void testGetUserByAuthTokenPositive() throws DataAccessException {
        String username = "testUser";

        String authToken = authService.authenticateUser(username);

        String result = authService.getUserByAuthToken(authToken);

        assertNotNull(result);
        assertEquals(username, result);
    }

    @Test
    @Order(8)
    @DisplayName("get user from token - negative")
    void testGetUserByAuthTokenNegative() throws DataAccessException {
        String result = authService.getUserByAuthToken("1234");

        assertNull(result);
    }
}
