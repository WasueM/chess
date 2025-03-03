package service;

import dataaccess.DataAccessException;
import dataaccess.AuthDataAccessMemory;
import dataaccess.GameDataAccessMemory;
import model.GameData;
import org.junit.jupiter.api.*;
import services.AuthService;
import services.GameService;
import services.RequestsRecords.CreateGameRequest;
import services.RequestsRecords.GamesListRequest;
import services.RequestsRecords.JoinGameRequest;
import services.ResultsRecords.CreateGameResult;
import services.ResultsRecords.GamesListResult;
import services.ResultsRecords.JoinGameResult;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameServiceTests {

    static AuthService authService;
    static GameService gameService;
    static String validAuthToken;

    @BeforeEach
    void setup() throws DataAccessException {
        authService = new AuthService(new AuthDataAccessMemory());
        gameService = new GameService(new GameDataAccessMemory(), authService);

        // get a valid token for use in the tests
        validAuthToken = authService.authenticateUser("testUser");
    }

    @Test
    @Order(1)
    @DisplayName("getGamesList - positive")
    void testGetGamesListPositive() throws DataAccessException {
        GamesListRequest request = new GamesListRequest(validAuthToken);
        GamesListResult gameList = gameService.getGamesList(request);

        assertNotNull(gameList);
    }

    @Test
    @Order(2)
    @DisplayName("getGamesList - negative")
    void testGetGamesListNegative() throws DataAccessException {
        GamesListRequest request = new GamesListRequest("1234");
        GamesListResult result = gameService.getGamesList(request);

        assertNull(result);
    }

    @Test
    @Order(3)
    @DisplayName("createGame - positive")
    void testCreateGamePositive() throws DataAccessException {
        CreateGameRequest request = new CreateGameRequest(validAuthToken, "testGame");
        CreateGameResult result = gameService.createGame(request);

        assertNotNull(result);
    }

    @Test
    @Order(4)
    @DisplayName("createGame - negative")
    void testCreateGameNegative() throws DataAccessException {
        CreateGameRequest request = new CreateGameRequest("1234", "testGame");
        CreateGameResult result = gameService.createGame(request);

        assertNull(result);
    }

    @Test
    @Order(5)
    @DisplayName("joinGame - positive")
    void testJoinGamePositive() throws DataAccessException {
        CreateGameRequest createRequest = new CreateGameRequest(validAuthToken, "testGame");
        CreateGameResult createResponse = gameService.createGame(createRequest);

        JoinGameRequest joinReq = new JoinGameRequest(validAuthToken, createResponse.gameID(), "WHITE");
        JoinGameResult joinRes = gameService.joinGame(joinReq);

        assertNotNull(joinRes);
    }

    @Test
    @Order(6)
    @DisplayName("joinGame - negative")
    void testJoinGameInvalidToken() throws DataAccessException {
        CreateGameRequest createRequest = new CreateGameRequest(validAuthToken, "testGame");
        CreateGameResult createResponse = gameService.createGame(createRequest);

        JoinGameRequest joinRequest = new JoinGameRequest("1234", createResponse.gameID(), "WHITE");
        JoinGameResult joinResponse = gameService.joinGame(joinRequest);

        assertNull(joinResponse);
    }
}
