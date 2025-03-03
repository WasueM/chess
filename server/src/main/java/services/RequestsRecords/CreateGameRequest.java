package services.RequestsRecords;

public record CreateGameRequest(
        String authToken,
        String gameName
) {}
