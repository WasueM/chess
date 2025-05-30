package services;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.*;

import model.GameData;
import services.requests.CreateGameRequest;
import services.requests.GamesListRequest;
import services.requests.JoinGameRequest;
import services.requests.MoveRequest;
import services.results.CreateGameResult;
import services.results.GamesListResult;
import services.results.JoinGameResult;
import services.results.MoveResult;

import java.util.Random;

public class GameService {

    private final GameDataAccessObject gameDataAccess;
    private final AuthService authService;

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
                String currentBlackUser = gameToJoin.blackUsername();

                // get the username of the person whose joining
                String joinerName = authService.getUserByAuthToken(joinGameRequest.authToken());

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

                if (modifiedGame == null) {
                    throw new DataAccessException("Failed to join the game");
                }

                gameDataAccess.updateGameWithNewData(modifiedGame);

                return new JoinGameResult(gameToJoin.gameID());
            }
            else {
                return null;
            }
    }

    public JoinGameResult leaveGame(JoinGameRequest joinGameRequest) throws DataAccessException {
        // authenticate
        boolean validAuth = authService.verifyAuthToken(joinGameRequest.authToken());

        if (validAuth) {
            // get the game from the id
            GameData[] allGames = gameDataAccess.getActiveGames();

            // see if the game ID exists
            GameData gameToLeave = null;
            for (GameData game : allGames) {
                if (game.gameID() == joinGameRequest.gameID()) {
                    gameToLeave = game;
                }
            }

            if (gameToLeave == null) {
                throw new DataAccessException("Game ID doesn't exist");
            }

            // get the needed data out of the game
            String gameName = gameToLeave.gameName();
            ChessGame chessGame = gameToLeave.game();
            String currentWhiteUser = gameToLeave.whiteUsername();
            String currentBlackUser = gameToLeave.blackUsername();

            // get the username of the person whose joining
            String leaverName = authService.getUserByAuthToken(joinGameRequest.authToken());

            // if the leaving player is black, make black user null, same for white
            GameData modifiedGame = null;
            if (joinGameRequest.playerColor().equals("WHITE")) {
                modifiedGame = new GameData(joinGameRequest.gameID(), null, currentBlackUser, gameName, chessGame);
            } else if (joinGameRequest.playerColor().equals("BLACK")) {
                modifiedGame = new GameData(joinGameRequest.gameID(), currentWhiteUser, null, gameName, chessGame);
            }

            if (modifiedGame == null) {
                throw new DataAccessException("Failed to leave the game");
            }

            gameDataAccess.updateGameWithNewData(modifiedGame);

            return new JoinGameResult(gameToLeave.gameID());
        }
        else {
            return null;
        }
    }

    public MoveResult handleMove(MoveRequest moveRequest) throws DataAccessException, InvalidMoveException {

        boolean validAuth = authService.verifyAuthToken(moveRequest.authToken());

        if (validAuth) {

            GameData[] allGames = gameDataAccess.getActiveGames();

            GameData gameToModify = null;
            for (GameData game : allGames) {
                if (game.gameID() == moveRequest.gameID()) {
                    gameToModify = game;
                }
            }

            if (gameToModify == null) {
                throw new DataAccessException("Game ID doesn't exist");
            }

            // get the needed information
            String gameName = gameToModify.gameName();
            ChessGame chessGame = gameToModify.game();
            String currentWhiteUser = gameToModify.whiteUsername();
            String currentBlackUser = gameToModify.blackUsername();

            // modify the game
            chessGame.makeMove(moveRequest.move());

            // make a modified game with the modified game
            GameData modifiedGameData = new GameData(moveRequest.gameID(), currentWhiteUser, currentBlackUser, gameName, chessGame);

            if (modifiedGameData == null) {
                throw new DataAccessException("Failed to handle the chess move");
            }

            gameDataAccess.updateGameWithNewData(modifiedGameData);

            return new MoveResult(moveRequest.gameID(), modifiedGameData);

        } else {
            return null;
        }

    }
}
