package services;

import dataaccess.DataAccessException;
import dataaccess.GameDataAccessMemory;
import model.GameData;

public class GameService {
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
        boolean validAuth = AuthService.verifyAuthToken(gamesListRequest.authToken());

        if (validAuth) {}
        else {
            return null;
        }
    }

    public JoinGameResult joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
            // authenticate
            boolean validAuth = AuthService.verifyAuthToken(gamesListRequest.authToken());

            if (validAuth) {}
            else {
                return null;
            }
    }
}
