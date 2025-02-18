package dataaccess;

import model.GameData;

import javax.xml.crypto.Data;

public interface GameDataAccessObject {
    GameData makeGame(GameData gameData) throws DataAccessException;
    GameData deleteGame(int gameID) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    GameData updateGameWithNewData(GameData gameData);
    GameData[] getActiveGames() throws DataAccessException;
    int[] getGameIDs() throws DataAccessException;
}
