package dataaccess;

import model.UserData;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDataAccessMySql implements UserDataAccessObject {
    @Override
    public UserData getUserByUsername(String username) throws DataAccessException {
        String sqlCommand = "SELECT username, password, email FROM UserData WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statememnt = conn.prepareStatement(sqlCommand)) {

            // search the database for a user with that username
            statememnt.setString(1, username);
            ResultSet results = statememnt.executeQuery();

            if (results.next()) {
                // so there was a result, return it
                return new UserData(
                        results.getString("username"),
                        results.getString("password"),
                        results.getString("email")
                );
            } else {
                // no user with that name, so return null
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error getting user from SQL database, do they exist?");
        }
    }

    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        String sqlCommand = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";

        // turn the password into something we can send over the air safely
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sqlCommand)) {

            // put in the parameters for the new user
            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());

            // make them!
            int rows = statement.executeUpdate();

            // return the user, because we're done
            return user;

        } catch (SQLException e) {
            throw new DataAccessException("Error making a new user with that name and password, are they taken?");
        }
    }
}
