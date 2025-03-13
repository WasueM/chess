package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import passoff.chess.TestUtilities;

import static org.junit.jupiter.api.Assertions.*;

public class GameSQLTests {
    GameDataAccessMySql gameDataAccessSQL;

    @BeforeEach
    public void setup() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var statement = conn.createStatement()) {
            statement.executeUpdate("DELETE FROM GameData");
        } catch (Exception e) {
            throw new DataAccessException("Test failed because the table wouldn't be reset right");
        }

        gameDataAccessSQL = new GameDataAccessMySql();
    }

    @Test
    @Order(1)
    @DisplayName("make game - positive")
    public void testMakeGamePositive() throws DataAccessException {
        // create a default ChessGame
        var chessGame = new ChessGame();

        // make a gameData and fill it up
        var gameData = new GameData(
                1,
                "whiteUser",
                "blackUser",
                "testGame",
                chessGame
        );

        // send it to the database
        gameDataAccessSQL.makeGame(gameData);

        // see if we can get it and test the basic parameters
        var returnedGame = gameDataAccessSQL.getGame(1);
        assertEquals("whiteUser", returnedGame.whiteUsername());
        assertEquals("testGame", returnedGame.gameName());

        // confirm we got the chessGame, and that the number of pieces are there still
        assertNotNull(returnedGame.game());
        assertEquals(32, returnedGame.game().chessBoard.numPieces);
    }

    @Test
    @Order(2)
    @DisplayName("make game - negative")
    public void testMakeGameNegative() throws DataAccessException {
        // add the first game
        var firstGame = new GameData(
                1,
                "whiteUser",
                "blackUser",
                "testGame1",
                new ChessGame()
        );
        gameDataAccessSQL.makeGame(firstGame);

        // now try and add the same ID again
        var duplicateGame = new GameData(
                1,
                "whiteUser2",
                "blackUser2",
                "testGame2",
                new ChessGame()
        );

        // this should fail when we try and add the duplicate
        assertThrows(RuntimeException.class, () -> {
            gameDataAccessSQL.makeGame(duplicateGame);
        });
    }

    @Test
    @Order(3)
    @DisplayName("get game - positive")
    public void testGetGamePositive() throws DataAccessException {
        // add a game
        var newGame = new GameData(
                1,
                "whiteUser",
                "blackUser",
                "testGame",
                new ChessGame()
        );
        gameDataAccessSQL.makeGame(newGame);

        // get it and test the values. Basically the same as test 1 but without the chess game aspect
        var returnedGame = gameDataAccessSQL.getGame(1);
        assertNotNull(returnedGame);
        assertEquals("testGame", returnedGame.gameName());
        assertEquals("whiteUser", returnedGame.whiteUsername());
    }

    @Test
    @Order(4)
    @DisplayName("get game - negative")
    public void testGetGameNegative() throws DataAccessException {
        // try getting a game when there are none, so it should fail and return null
        var returnedGame = gameDataAccessSQL.getGame(1);
        assertNull(returnedGame);
    }

    @Test
    @Order(5)
    @DisplayName("update game with new data - positive")
    public void testUpdateGamePositive() throws DataAccessException {
        // add a game
        var originalGame = new GameData(
                10,
                "whiteUser",
                "blackUser",
                "testGame",
                new ChessGame()
        );
        gameDataAccessSQL.makeGame(originalGame);

        // create a ChessGame that has a different turn
        var updatedChessGame = new ChessGame();
        updatedChessGame.setTeamTurn(ChessGame.TeamColor.BLACK);

        // update
        var updatedGame = new GameData(
                10,
                "newWhiteUser",
                "newBlackUser",
                "testGame",
                updatedChessGame
        );
        gameDataAccessSQL.updateGameWithNewData(updatedGame);

        // see if the changes stuck around
        var returnedGame = gameDataAccessSQL.getGame(10);
        assertNotNull(returnedGame);
        assertEquals("newWhiteUser", returnedGame.whiteUsername());

        // see if the chess game is now the new one, with the right team turn
        assertEquals(ChessGame.TeamColor.BLACK, returnedGame.game().getTeamTurn());
    }

    @Test
    @Order(6)
    @DisplayName("update game with new data - negative")
    public void testUpdateGameNegative() throws DataAccessException {
        // not adding any games, so it should fail to update
        var chessGame = new ChessGame();
        var gameData = new GameData(
                1,
                "whiteUser",
                "blackUser",
                "testGame",
                chessGame
        );

        // this should update no rows since there's nothing to update
        gameDataAccessSQL.updateGameWithNewData(gameData);

        // so trying to get the game we just "updated" should just return null
        var returnedGame = gameDataAccessSQL.getGame(1);
        assertNull(returnedGame);
    }

    @Test
    @Order(7)
    @DisplayName("get active games - positive")
    public void testGetActiveGamesPositive() throws DataAccessException {
        // add a two games
        gameDataAccessSQL.makeGame(new GameData(20, "whiteUser1", "blackUser1", "testGame1", new ChessGame()));
        gameDataAccessSQL.makeGame(new GameData(21, "whiteUser2", "blackUser2", "testGame2", new ChessGame()));

        // see if it gets two games back from the list
        var activeGames = gameDataAccessSQL.getActiveGames();
        assertEquals(2, activeGames.length);
    }

    @Test
    @Order(8)
    @DisplayName("get game IDs - positive")
    public void testGetGameIDsPositive() throws DataAccessException {
        gameDataAccessSQL.makeGame(new GameData(1, "whiteUser1", "blackUser1", "testGame1", new ChessGame()));
        gameDataAccessSQL.makeGame(new GameData(2, "whiteUser2", "blackUser2", "testGame2", new ChessGame()));

        int[] ids = gameDataAccessSQL.getGameIDs();
        // expecting 2 distinct IDs
        assertTrue(ids.length == 2);
    }

    @Test
    @Order(9)
    @DisplayName("super update game test - doing a couple moves and seeing if it saves")
    public void testUpdateGameWithMoves() throws DataAccessException {
        // add a new game
        var originalGame = new GameData(
                17,
                "theWhiteUser",
                "theBlackUser",
                "theTestGame",
                new ChessGame()
        );
        gameDataAccessSQL.makeGame(originalGame);

        // create a ChessGame that has a different turn, some different moves made too. We'll test it with some "in check" tests
        var updatedChessGame = new ChessGame();
        updatedChessGame.setTeamTurn(ChessGame.TeamColor.BLACK);
        updatedChessGame.setBoard(TestUtilities.loadBoard("""
                | | | | | | | |k|
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | |K| | | |r| | |
                | | | | | | | | |
                | | | | | | | | |
                """));

        // update it
        var theUpdatedGame = new GameData(
                17,
                "aNewWhiteUser",
                "aNewBlackUser",
                "aTestGame",
                updatedChessGame
        );
        gameDataAccessSQL.updateGameWithNewData(theUpdatedGame);

        // see if the changes stuck around
        var theReturnedGame = gameDataAccessSQL.getGame(17);
        assertNotNull(theReturnedGame);

        // did the username stay changed?
        assertEquals("aNewWhiteUser", theReturnedGame.whiteUsername());

        // now see if it's in check as it should be
        Assertions.assertTrue(theReturnedGame.game().isInCheck(ChessGame.TeamColor.WHITE),
                "White is in check but isInCheck returned false.");
    }
}