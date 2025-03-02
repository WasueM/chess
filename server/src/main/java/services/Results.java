package services;

import model.GameData;

record LoginResult(String username, String authToken) {}

record RegisterResult(String username, String authToken) { }

record LogoutResult(String username) {}

record GamesListResult(
        GameData[] games
) {}

record CreateGameResult(
        int gameID
) {}

record JoinGameResult(
        int gameID
) {}