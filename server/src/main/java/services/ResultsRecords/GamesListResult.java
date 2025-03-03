package services.ResultsRecords;

import model.GameData;

public record GamesListResult(
        GameData[] games
) {}
