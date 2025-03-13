package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    static void createDatabase() throws DataAccessException {
        // Try and make the database if it doesn't exist
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            String createDB = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            statement.executeUpdate(createDB);

        } catch (SQLException e) {
            throw new DataAccessException("Couldn't create the MySQL database");
        }

        // make the tables if they don't already exist
        try (Connection conn = DriverManager.getConnection(CONNECTION_URL + "/" + DATABASE_NAME, USER, PASSWORD);
             Statement statement = conn.createStatement()) {

            // make auth data table
            String createAuthTable = "CREATE TABLE IF NOT EXISTS AuthData ("
                    + " token VARCHAR(255) PRIMARY KEY, "
                    + " username VARCHAR(255) NOT NULL "
                    + ")";
            statement.executeUpdate(createAuthTable);

            // make user data table
            String createUserTable = "CREATE TABLE IF NOT EXISTS UserData ("
                    + " username VARCHAR(255) PRIMARY KEY, "
                    + " password VARCHAR(255) NOT NULL, "
                    + " email VARCHAR(255) NOT NULL "
                    + ")";
            statement.executeUpdate(createUserTable);

            // make game data table
            String createGameTable = "CREATE TABLE IF NOT EXISTS GameData ("
                    + " game_id INT AUTO_INCREMENT PRIMARY KEY, "
                    + " white_username VARCHAR(255), "
                    + " black_username VARCHAR(255), "
                    + " game_name VARCHAR(255) NOT NULL, "
                    + " game_json TEXT NOT NULL "
                    + ")";
            statement.executeUpdate(createGameTable);

        } catch (SQLException e) {
            throw new DataAccessException("Error adding new tables to new MySQL database");
        }
    }


    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL + "/" + DATABASE_NAME, USER, PASSWORD);

            // always make sure the database and tables exist first
            createDatabase();

            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
