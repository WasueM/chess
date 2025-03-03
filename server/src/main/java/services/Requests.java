package services;

record LoginRequest(
        String username,
        String password){
}

record RegisterRequest(
        String username,
        String password,
        String email) {
}

record LogoutRequest(
        String authToken
) {}

record GamesListRequest(
        String authToken
) {}

record CreateGameRequest(
        String authToken,
        String gameName
) {}

record JoinGameRequest(
        String authToken,
        int gameID,
        String playerColor
) {}
