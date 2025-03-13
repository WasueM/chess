package server;

import dataaccess.*;
import services.AuthService;
import services.GameService;
import services.Handlers;
import services.UserService;
import spark.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Server {

    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    private Handlers handlers;

    public Server() {
        this.handlers = new Handlers(userService, gameService, authService);
        this.wakeupDatabase();
    }

    public void wakeupDatabase() {
        // umcomment to create the memory version of the database
//        AuthDataAccessMemory authDataAccessMemory = new AuthDataAccessMemory();
//        GameDataAccessMemory gameDataAccessMemory = new GameDataAccessMemory();
//        UserDataAccessMemory userDataAccessMemory = new UserDataAccessMemory();

        // uncomment to create the SQL version of the database
        AuthDataAccessMySql authDataAccessMemory = new AuthDataAccessMySql();
        GameDataAccessMySql gameDataAccessMemory = new GameDataAccessMySql();
        UserDataAccessMySql userDataAccessMemory = new UserDataAccessMySql();

        // create the services based on the version of the database we want
        this.authService = new AuthService(authDataAccessMemory);
        this.gameService = new GameService(gameDataAccessMemory, authService);
        this.userService = new UserService(userDataAccessMemory, authService);

        this.handlers.reset(userService, gameService, authService);
    }

    public void resetDatabase() {
        // umcomment to create the memory version of the database
//        AuthDataAccessMemory authDataAccessMemory = new AuthDataAccessMemory();
//        GameDataAccessMemory gameDataAccessMemory = new GameDataAccessMemory();
//        UserDataAccessMemory userDataAccessMemory = new UserDataAccessMemory();

        configureDatabase();

        // uncomment to create the SQL version of the database
        AuthDataAccessMySql authDataAccessMemory = new AuthDataAccessMySql();
        GameDataAccessMySql gameDataAccessMemory = new GameDataAccessMySql();
        UserDataAccessMySql userDataAccessMemory = new UserDataAccessMySql();

        // delete everything from the SQL tables to get them ready to go
        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {

            statement.executeUpdate("DELETE FROM AuthData");
            statement.executeUpdate("DELETE FROM GameData");
            statement.executeUpdate("DELETE FROM UserData");

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error resetting my sql database");
        }

        // create the services based on the version of the database we want
        this.authService = new AuthService(authDataAccessMemory);
        this.gameService = new GameService(gameDataAccessMemory, authService);
        this.userService = new UserService(userDataAccessMemory, authService);

        this.handlers.reset(userService, gameService, authService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        createEndpoints();

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void createEndpoints() {
        Spark.delete("/db", (request, response) -> {
            this.resetDatabase();
            response.type("application/json");
            response.status(200);
            return "{\"message\":\"Database reset!\"}";
        });
        Spark.post("/user", (request, response) -> {
            this.handlers.handleRegister(request, response);
            return response.body();
        });
        Spark.post("/session", (request, response) -> {
            this.handlers.handleLogin(request, response);
            return response.body();
        });
        Spark.delete("/session", (request, response) -> {
            this.handlers.handleLogout(request, response);
            return response.body();
        });
        Spark.get("/game", (request, response) -> {
            this.handlers.handleGetGamesList(request, response);
            return response.body();
        });
        Spark.post("/game", (request, response) -> {
            this.handlers.handleCreateGame(request, response);
            return response.body();
        });
        Spark.put("/game", (request, response) -> {
            this.handlers.handleJoinGame(request, response);
            return response.body();
        });
    }

    private void configureDatabase() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {

            // Ensure the database exists
            DatabaseManager.createDatabase();

            // Create tables if they do not exist
            String createAuthTable = "CREATE TABLE IF NOT EXISTS AuthData (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "token VARCHAR(255) NOT NULL UNIQUE, " +
                    "userId INT NOT NULL, " +
                    "FOREIGN KEY (userId) REFERENCES UserData(id) ON DELETE CASCADE" +
                    ");";

            String createUserTable = "CREATE TABLE IF NOT EXISTS UserData (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "passwordHash VARCHAR(255) NOT NULL" +
                    ");";

            String createGameTable = "CREATE TABLE IF NOT EXISTS GameData (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "gameState TEXT NOT NULL, " +
                    "createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";

            // Execute table creation statements
            statement.executeUpdate(createUserTable);
            statement.executeUpdate(createAuthTable);
            statement.executeUpdate(createGameTable);

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error configuring database: " + e.getMessage(), e);
        }
    }
}
