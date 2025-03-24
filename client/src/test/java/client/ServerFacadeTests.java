package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        try {
            facade = new ServerFacade("http://localhost:" + port + "/");
        } catch (Exception error) {
            System.out.println("Couldn't connect to the server at \"http://localhost:" + port + "/\"");
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDB() throws Exception {
        facade.clearDatabase();
    }

    @Test
    @Order(1)
    @DisplayName("register - positive")
    public void testRegisterPositive() throws Exception {
        AuthData authData = facade.register("testUser", "1234", "testEmail");
        assertNotNull(authData.authToken());
    }

    @Test
    @Order(2)
    @DisplayName("register - negative")
    public void testRegisterNegative() throws Exception {
        facade.register("testUser", "1234", "testEmail");

        // it should fail because a user is already registered with that username
        assertThrows(Exception.class, () -> {
            facade.register("testUser", "1234", "testEmail");
        });
    }

    @Test
    @Order(3)
    @DisplayName("login - positive")
    public void testLoginPositive() throws Exception {
        facade.register("testUser", "1234", "testEmail");
        AuthData authData = facade.login("testUser", "1234");

        // should work just fine
        assertNotNull(authData.authToken());
    }

    @Test
    @Order(4)
    @DisplayName("login - negative")
    public void testLoginNegative() throws Exception {
        facade.register("testUser", "1234", "testEmail");

        // shouldn't work because that user isn't registered
        assertThrows(Exception.class, () -> {
            facade.login("testUser", "wrongPassword");
        });
    }

    @Test
    @Order(5)
    @DisplayName("log out - positive")
    public void testLogoutPositive() throws Exception {
        facade.register("testUser", "1234", "testEmail");

        // returns true on a successful logout
        boolean success = facade.logout();
        assertTrue(success);
    }

    @Test
    @Order(6)
    @DisplayName("create game - positive")
    public void testCreateGamePositive() throws Exception {
        facade.register("testUser", "1234", "testEmail");
        int gameID = facade.createGame("testGame");

        // if it works, a game ID will be returned
        assertTrue(gameID > 0);
    }

    @Test
    @Order(7)
    @DisplayName("create game - negative")
    public void testCreateGameNegative() throws Exception {
        // should fail for not being logged in
        assertThrows(Exception.class, () -> {
            facade.createGame("testGame");
        });
    }

    @Test
    @Order(8)
    @DisplayName("join game - positive")
    public void testJoinGamePositive() throws Exception {
        facade.register("testUser", "1234", "testEmail");
        int gameID = facade.createGame("testGame");
        int joinedID = facade.joinGame("WHITE", gameID);
        // this flow should work
        assertEquals(gameID, joinedID);
    }

    @Test
    @Order(9)
    @DisplayName("join game - negative")
    public void testJoinGameNegative() throws Exception {
        facade.register("testUser", "1234", "testEmail");

        // no game with that id, should fail
        assertThrows(Exception.class, () -> {
            facade.joinGame("BLACK", 999999);
        });
    }

    @Test
    @Order(10)
    @DisplayName("list games - positive")
    public void testListGamesPositive() throws Exception {
        facade.register("testUser", "1234", "testEmail");
        facade.createGame("gameOne");
        facade.createGame("gameTwo");

        GameData[] games = facade.listGames();
        // should have exactly 2
        assertEquals(2, games.length);
    }

    @Test
    @Order(11)
    @DisplayName("list games - negative")
    public void testListGamesNegative() throws Exception {
        // not logged in, should fail
        assertThrows(Exception.class, () -> {
            facade.listGames();
        });
    }

    @Test
    @Order(12)
    @DisplayName("clear database - positive")
    public void testClearDatabase() throws Exception {
        facade.register("testUser", "1234", "testEmail");
        facade.createGame("gameToClear");

        facade.clearDatabase();

        facade.register("anotherUser", "5678", "anotherEmail");
        GameData[] games = facade.listGames();

        // there should be no games after this
        assertEquals(0, games.length);
    }
}
