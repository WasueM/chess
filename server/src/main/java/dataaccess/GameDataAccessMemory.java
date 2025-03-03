package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.List;

public final class GameDataAccessMemory implements GameDataAccessObject {
    final private List<GameData> games = new ArrayList<GameData>();

    @Override
    public GameData makeGame(GameData gameData) throws DataAccessException {
        try {
            games.add(gameData);
            return gameData;
        } catch (Exception e) {
            throw new DataAccessException("Couldn't add the game for some reason");
        }
    }

    @Override
    public GameData deleteGame(int gameID) throws DataAccessException {
        try {
            for (GameData game : games) {
                if (game.gameID() == gameID) {
                    games.remove(game);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error deleting game");
        }
        return null;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Game not found");
    }

    @Override
    public GameData updateGameWithNewData(GameData gameData) {
        GameData gameToUpdate = null;
        for (GameData game : games) {
            if (game.gameID() == gameData.gameID()) {
                gameToUpdate = game;
            }
        }
        games.remove(gameToUpdate);
        games.add(gameData);
        return gameData;
    }

    @Override
    public GameData[] getActiveGames() throws DataAccessException {
        return games.toArray(new GameData[0]);
    }

    @Override
    public int[] getGameIDs() throws DataAccessException {
        int[] ids = new int[games.size()];
        int counter = 0;
        for (GameData game : games) {
            ids[counter] = game.gameID();
            counter++;
        }
        return ids;
    }
}
