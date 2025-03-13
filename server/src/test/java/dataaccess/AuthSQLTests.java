package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class AuthSQLTests {
    AuthDataAccessMySql authDataAccessSQL;

    @BeforeEach
    public void setup() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM AuthData");
        } catch (Exception e) {
            throw new DataAccessException("Test failed because the table wouldn't be rest right");
        }

        authDataAccessSQL = new AuthDataAccessMySql();
    }


    @Test
    @Order(1)
    @DisplayName("add auth token - positive")
    public void testAddAuthTokenPositive() throws DataAccessException {
        // add it
        AuthData auth = new AuthData("testToken", "testUser");
        authDataAccessSQL.addAuthToken(auth);

        // check if it was added right
        var tokens = authDataAccessSQL.getValidTokens();
        assertEquals(1, tokens.length);
    }

    @Test
    @Order(2)
    @DisplayName("add auth token - negative")
    public void testAddAuthTokenNegative() throws DataAccessException {
        // Add the token
        AuthData auth = new AuthData("testToken", "testUser");
        authDataAccessSQL.addAuthToken(auth);

        // Add it again (which should fail)
        AuthData duplicate = new AuthData("testToken", "otherUserThatSomehowHasSameTokenAsFirst");

        // This should throw at data access exception
        assertThrows(DataAccessException.class, () -> {
            authDataAccessSQL.addAuthToken(duplicate);
        });
    }

    @Test
    @Order(3)
    @DisplayName("delete auth token - positive")
    public void testDeleteAuthTokenPositive() throws DataAccessException {
        // Add a token
        AuthData auth = new AuthData("testToken", "testUser");
        authDataAccessSQL.addAuthToken(auth);

        // delete it
        authDataAccessSQL.deleteAuthToken("testToken");

        // make sure its gone
        var authTokens = authDataAccessSQL.getValidTokens();
        assertEquals(0, authTokens.length);
    }

    @Test
    @Order(4)
    @DisplayName("delete auth token - negative)")
    public void testDeleteAuthTokenNegative() throws DataAccessException {
        AuthData deleted = authDataAccessSQL.deleteAuthToken("testToken");
        // Should return null if there's no token with that string to delete
        assertNull(deleted);
    }

    @Test
    @Order(5)
    @DisplayName("get valid tokens - positive")
    public void testGetValidTokensPositive() throws DataAccessException {
        authDataAccessSQL.addAuthToken(new AuthData("testToken1", "testUser1"));
        authDataAccessSQL.addAuthToken(new AuthData("testToken2", "testUser2"));

        // make sure there are two valid tokens after that
        AuthData[] tokens = authDataAccessSQL.getValidTokens();
        assertEquals(2, tokens.length);
    }

    @Test
    @Order(6)
    @DisplayName("get user by token - positive")
    public void testGetUserByAuthTokenPositive() throws DataAccessException {
        // first add the token
        authDataAccessSQL.addAuthToken(new AuthData("testToken", "testUser"));

        // try and get the username, given the token, and it should match
        String username = authDataAccessSQL.getUserByAuthToken("testToken");
        assertEquals("testUser", username);
    }

    @Test
    @Order(7)
    @DisplayName("get user by token - negative")
    public void testGetUserByAuthTokenNegative() throws DataAccessException {
        // Don't add any tokens, and try and get a username out of it, which should just return null
        String username = authDataAccessSQL.getUserByAuthToken("testToken");
        assertNull(username);
    }
}
