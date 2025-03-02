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
        String username
) {}

record GamesListRequest(
        String authToken
) {}

record CreateGameRequest(
        String authToken
) {}

record JoinGameRequest(
        String authToken,
        int gameID
) {}
