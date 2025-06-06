package dataaccess;

import model.AuthData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class AuthDataAccessMySql implements AuthDataAccessObject {

    @Override
    public AuthData addAuthToken(AuthData authData) throws DataAccessException {
        String sqlCommand = "INSERT INTO AuthData (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlCommand)) {

            // add the auth token to the first column, the user name to the second
            statement.setString(1, authData.authToken());
            statement.setString(2, authData.username());

            // DO IT!
            int rows = statement.executeUpdate();

            return authData;
        } catch (SQLException error) {
            throw new DataAccessException("Error adding Auth Token to MySQL database");
        }
    }

    @Override
    public AuthData deleteAuthToken(String authToken) throws DataAccessException {
        String sqlSelectCommand = "SELECT token, username FROM AuthData WHERE token = ?";
        String sqlDeleteCommand = "DELETE FROM AuthData WHERE token = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectStatement = conn.prepareStatement(sqlSelectCommand);
             PreparedStatement deleteStatement = conn.prepareStatement(sqlDeleteCommand)) {

            // add the auth token to the select statement and see if it comes up in the results
            selectStatement.setString(1, authToken);
            ResultSet results = selectStatement.executeQuery();

            if (results.next()) {
                // Okay so it exists, so it should be deleted
                AuthData authData = new AuthData(
                        results.getString("token"),
                        results.getString("username"));

                deleteStatement.setString(1, authToken);
                deleteStatement.executeUpdate();

                return authData;
            }

            // this means it wasn't found
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Issue deleting the auth token from the my sql database");
        }
    }

    @Override
    public AuthData[] getValidTokens() throws DataAccessException {
        String sqlCommand = "SELECT token, username FROM AuthData";
        List<AuthData> tokens = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlCommand)) {

            // run the query statement to get all the auth data
            ResultSet results = statement.executeQuery();

            // loop through the results and add them to the data structure
            while (results.next()) {
                AuthData authData = new AuthData(
                        results.getString("token"),
                        results.getString("username"));

                tokens.add(authData);
            }

            // return the array of found auth data
            return tokens.toArray(new AuthData[0]);

        } catch (SQLException e) {
            throw new RuntimeException("Issue getting all valid auth tokens from mySQL server");
        }
    }

    @Override
    public String getUserByAuthToken(String authToken) throws DataAccessException {
        String sqlCommand = "SELECT username FROM AuthData WHERE token = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlCommand)) {

            // Set the token parameter to search for
            statement.setString(1, authToken);
            ResultSet results = statement.executeQuery();

            // if there's any results, we can get the username from it
            if (results.next()) {
                return results.getString("username");
            } else {
                // no results, so there is no username with that auth token
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Issue getting the user for that auth token, check if it is a valid token");
        }
    }

    @Override
    public String getAuthTokenByUser(String username) throws DataAccessException {
        String sqlCommand = "SELECT token FROM AuthData WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlCommand)) {

            // Set the username parameter to search for
            statement.setString(1, username);

            ResultSet results = statement.executeQuery();

            // if there's any results, we can get the token from it
            if (results.next()) {
                return results.getString("token");
            }
            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Issue getting an auth token for that username from the sql database.");
        }
    }
}