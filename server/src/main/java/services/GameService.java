package services;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Random;

public class GameService {

    private final GameDataAccessObject gameDataAccess;

    public GameService(GameDataAccessObject gameDataAccess) {
        this.gameDataAccess = gameDataAccess;
    }

    public GamesListResult getGamesList(GamesListRequest gamesListRequest) throws DataAccessException {
        // authenticate
        boolean validAuth = AuthService.verifyAuthToken(gamesListRequest.authToken());

        if (validAuth) {
            // get the games list
            GameData[] gamesList = GameDataAccessMemory.getActiveGames();

            return new GamesListResult(gamesList);
        } else {
            return null;
        }
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
        // authenticate
        boolean validAuth = AuthService.verifyAuthToken(createGameRequest.authToken());

        if (validAuth) {
            // random id
            Random random = new Random();
            int randomGameID = random.nextInt(99999);

            // get player data for name
            String username = AuthDataAccessMemory.getUserByAuthToken(createGameRequest.authToken());

            GameData newGame = new GameData(randomGameID, username, "", createGameRequest.gameName(), new ChessGame());
            GameDataAccessMemory.makeGame(newGame);

            return new CreateGameResult(randomGameID);
        }
        else {
            return null;
        }
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
            // authenticate
            boolean validAuth = AuthService.verifyAuthToken(joinGameRequest.authToken());

            if (validAuth) {
                // get the game from the id
                GameData[] allGames = GameDataAccessMemory.getActiveGames();

                // see if the game ID exists
                GameData gameToJoin = null;
                for (GameData game : allGames) {
                    if (game.gameID() == joinGameRequest.gameID()) {
                        gameToJoin = game;
                    }
                }

                if (gameToJoin == null) {
                    throw new DataAccessException("Game ID doesn't exist");
                }

                // get the needed data out of the game
                String gameName = gameToJoin.gameName();
                String hostName = gameToJoin.whiteUsername();
                ChessGame chessGame = gameToJoin.game();

                // get the username of the person whose joining
                String joinerName = AuthDataAccessMemory.getUserByAuthToken(joinGameRequest.authToken());

                // if the joining player wants to be black, make the game that way
                GameData modifiedGame = null;
                if (joinGameRequest.playerColor() == "BLACK") {
                    modifiedGame = new GameData(joinGameRequest.gameID(), hostName, joinerName, gameName, chessGame);
                } else if (joinGameRequest.playerColor() == "WHITE") {
                    modifiedGame = new GameData(joinGameRequest.gameID(), joinerName, hostName, gameName, chessGame);
                }

                if (modifiedGame == null) {
                    throw new DataAccessException("Failed to join the game");
                }

                GameDataAccessMemory.updateGameWithNewData(modifiedGame);

                return new JoinGameResult(gameToJoin.gameID());
            }
            else {
                return null;
            }
    }
}
