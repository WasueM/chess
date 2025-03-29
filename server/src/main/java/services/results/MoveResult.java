package services.results;

import model.GameData;

public record MoveResult(
        int gameID,
        GameData game
) {}
