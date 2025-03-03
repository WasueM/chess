package services.requests;

public record CreateGameRequest(
        String authToken,
        String gameName
) {}
