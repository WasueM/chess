package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserSQLTests {

    UserDataAccessMySql userDataAccessSQL;

    @BeforeEach
    public void setup() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM UserData");
        } catch (Exception e) {
            throw new DataAccessException("Test failed because the table wouldn't be reset right");
        }

        userDataAccessSQL = new UserDataAccessMySql();
    }

    @Test
    @Order(1)
    @DisplayName("Add user and get by username - positive")
    public void testAddUserPositive() throws DataAccessException {
        // add the user
        var user = new UserData("testUser", "1234", "testEmail");
        userDataAccessSQL.addUser(user);

        // try and get it to see if it worked
        var returnedUser = userDataAccessSQL.getUserByUsername("testUser");
        assertEquals("testUser", returnedUser.username());
    }

    @Test
    @Order(2)
    @DisplayName("Add user - negative")
    public void testAddUserNegative() throws DataAccessException {
        // Add one user
        var user = new UserData("testUser", "1234", "testEmail");
        userDataAccessSQL.addUser(user);

        // Try and add them again with the same username
        var duplicateUser = new UserData("testUser", "1234", "testEmail");
        assertThrows(DataAccessException.class, () -> {
            userDataAccessSQL.addUser(duplicateUser);
        });
    }

    @Test
    @Order(3)
    @DisplayName("Get user by username - negative")
    public void testGetUserByUsernameNegative() throws DataAccessException {
        // Don't add a user, just try and get them anyways, should fail
        var returnedUser = userDataAccessSQL.getUserByUsername("testUser");
        assertNull(returnedUser);
    }
}
