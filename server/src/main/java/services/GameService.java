package services;

import chess.ChessGame;
import dataaccess.*;

import model.GameData;
import services.requests.CreateGameRequest;
import services.requests.GamesListRequest;
import services.requests.JoinGameRequest;
import services.results.CreateGameResult;
import services.results.GamesListResult;
import services.results.JoinGameResult;

import java.util.Random;

public class GameService {

    private GameDataAccessObject gameDataAccess;
    private AuthService authService;

    public GameService(GameDataAccessObject gameDataAccess, AuthService authService) {
        this.gameDataAccess = gameDataAccess;
        this.authService = authService;
    }

    public GamesListResult getGamesList(GamesListRequest gamesListRequest) throws DataAccessException {
        // authenticate
        boolean validAuth = authService.verifyAuthToken(gamesListRequest.authToken());

        if (validAuth) {
            // get the games list
            GameData[] gamesList = gameDataAccess.getActiveGames();

            return new GamesListResult(gamesList);
        } else {
            return null;
        }
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
        // authenticate
        boolean validAuth = authService.verifyAuthToken(createGameRequest.authToken());

        if (validAuth) {
            // random id
            Random random = new Random();
            int randomGameID = random.nextInt(99999);

            // get player data for name
            String username = authService.getUserByAuthToken(createGameRequest.authToken());

            GameData newGame = new GameData(randomGameID, null, null, createGameRequest.gameName(), new ChessGame());
            gameDataAccess.makeGame(newGame);

            return new CreateGameResult(randomGameID);
        }
        else {
            return null;
        }
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
            // authenticate
            boolean validAuth = authService.verifyAuthToken(joinGameRequest.authToken());

            if (validAuth) {
                // get the game from the id
                GameData[] allGames = gameDataAccess.getActiveGames();

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
                ChessGame chessGame = gameToJoin.game();
                String currentWhiteUser = gameToJoin.whiteUsername();
                String currentBlackUser = gameToJoin.blackUserName();

                // get the username of the person whose joining
                String joinerName = authService.getUserByAuthToken(joinGameRequest.authToken());

                System.out.println(gameName + " " + joinerName + " " + joinGameRequest.playerColor());

                // if the joining player wants to be black, make the game that way
                GameData modifiedGame = null;
                if ((joinGameRequest.playerColor().equals("BLACK") && currentBlackUser != null) ||
                        (joinGameRequest.playerColor().equals("WHITE") && currentWhiteUser != null)) {
                    throw new DataAccessException("Failed to join the game because that color is already taken");
                } else if (joinGameRequest.playerColor().equals("WHITE")) {
                    modifiedGame = new GameData(joinGameRequest.gameID(), joinerName, currentBlackUser, gameName, chessGame);
                } else if (joinGameRequest.playerColor().equals("BLACK")) {
                    modifiedGame = new GameData(joinGameRequest.gameID(), currentWhiteUser, joinerName, gameName, chessGame);
                }

                System.out.println(modifiedGame.toString());
                System.out.println("^^^^^^");

                if (modifiedGame == null) {
                    throw new DataAccessException("Failed to join the game");
                }

                for (GameData item : gameDataAccess.getActiveGames()) {
                    System.out.println(item);
                }

                gameDataAccess.updateGameWithNewData(modifiedGame);

                System.out.println("------");
                for (GameData item : gameDataAccess.getActiveGames()) {
                    System.out.println(item);
                }
                System.out.println("000000");

                return new JoinGameResult(gameToJoin.gameID());
            }
            else {
                return null;
            }
    }
}
