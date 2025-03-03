package services.results;

import model.GameData;

public record GamesListResult(
        GameData[] games
) {}
