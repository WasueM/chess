package dataaccess;

import model.GameData;

public class GameDataAccessMySql implements GameDataAccessObject {
    @Override
    public GameData makeGame(GameData gameData) throws DataAccessException {
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public GameData updateGameWithNewData(GameData gameData) {
        return null;
    }

    @Override
    public GameData[] getActiveGames() throws DataAccessException {
        return new GameData[0];
    }

    @Override
    public int[] getGameIDs() throws DataAccessException {
        return new int[0];
    }
}
