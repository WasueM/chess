package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.GsonBuilder;
import model.GameData;
import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDataAccessMySql implements GameDataAccessObject {

    // set up a GSON to use for all the chessGame uploading/downloading
    //private final Gson gson = new Gson();
    private final Gson gson = new GsonBuilder()
    .registerTypeAdapter(ChessBoard.class, new ChessBoardJSONAdapter())
    .create();


    @Override
    public GameData makeGame(GameData gameData) throws DataAccessException {
        String SQLcommand = "INSERT INTO GameData (game_id, white_username, black_username, game_name, game_json) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQLcommand, Statement.RETURN_GENERATED_KEYS)) {

            // set the basic parameters
            statement.setInt(1, gameData.gameID());
            statement.setString(2, gameData.whiteUsername());
            statement.setString(3, gameData.blackUsername());
            statement.setString(4, gameData.gameName());

            // turn the ChessGame into JSON so it can be uploaded
            String JSONchessGame = gson.toJson(gameData.game());
            statement.setString(5, JSONchessGame);

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
             PreparedStatement statement = conn.prepareStatement(SQLcommand)) {

            statement.setInt(1, gameID);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                String whiteUsername = results.getString("white_username");
                String blackUsername = results.getString("black_username");
                String gameName = results.getString("game_name");

                ChessGame game = gson.fromJson(results.getString("game_json"), ChessGame.class);

                // it worked! So, return the new Game Data
                return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
            } else {
                // we didn't find anything, so return null
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting the game from SQL database, is the id correct?");
        }
    }

    @Override
    public GameData updateGameWithNewData(GameData gameData) {
        String SQLcommand = "UPDATE GameData SET white_username = ?, black_username = ?, game_name = ?, game_json = ? WHERE game_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQLcommand)) {

            // set the basic parameters
            statement.setInt(1, gameData.gameID());
            statement.setString(2, gameData.whiteUsername());
            statement.setString(3, gameData.blackUsername());
            statement.setString(4, gameData.gameName());

            // turn the ChessGame into JSON so it can be uploaded
            String JSONchessGame = gson.toJson(gameData.game());
            statement.setString(4, JSONchessGame);

            // full send
            statement.executeUpdate();

            // if we got this far, it worked, return the gamedata
            return gameData;
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error updating game in SQL database!");
        }
    }

    @Override
    public GameData[] getActiveGames() throws DataAccessException {
        String SQLcommand = "SELECT * FROM GameData";
        List<GameData> games = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQLcommand);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                int gameID = results.getInt("game_id");
                String whiteUsername = results.getString("white_username");
                String blackUsername = results.getString("black_username");
                String gameName = results.getString("game_name");

                // use GSON to make the game json into a game object
                ChessGame game = gson.fromJson(results.getString("game_json"), ChessGame.class);

                // add it to our list of games that we've found
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
            }

            // return the games we found
            return games.toArray(new GameData[0]);
        } catch (SQLException e) {
            throw new DataAccessException("Error getting the active games from the SQL server");
        }
    }

    @Override
    public int[] getGameIDs() throws DataAccessException {
        String SQLcommand = "SELECT game_id FROM GameData";
        List<Integer> idList = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(SQLcommand);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                // that means there's a result, so we grab it and add it
                idList.add(results.getInt("game_id"));
            }

            // return all the ids we found
            return idList.stream().mapToInt(i -> i).toArray();
        } catch (SQLException e) {
            throw new DataAccessException("Error getting the active game ids");
        }
    }
}
