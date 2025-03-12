package dataaccess;

import model.GameData;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class GameDataAccessMySql implements GameDataAccessObject {
    @Override
    public GameData makeGame(GameData gameData) throws DataAccessException {
        String SQLcommand = "INSERT INTO GameData (game_id, white_username, black_username, game_name, game_json) VALUES (?, ?, ?, ?, ?)";

        // get gson ready to go
        Gson gson = new Gson();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQLcommand, Statement.RETURN_GENERATED_KEYS)) {

            // set the basic parameters
            statement.setInt(1, gameData.gameID());
            statement.setString(2, gameData.whiteUsername());
            statement.setString(3, gameData.blackUsername());
            statement.setString(4, gameData.gameName());

            // turn the ChessGame into JSON so it can be uploaded
            String JSONchessGame = gson.toJson(gameData.game());
            statement.setString(4, JSONchessGame);

            // Run the command
            statement.executeUpdate();

            // If we got to this point, return the gameData because it's a success
            return gameData;

        } catch (SQLException e) {
            throw new RuntimeException("Error making the game with SQL database");
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String SQLcommand = "SELECT * FROM GameData WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQLcommand)) {} catch (SQLException e)
        {
            throw new RuntimeException("Error getting the game from SQL database, is the id correct?");
        }

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
