package server;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import services.AuthService;
import services.GameService;
import services.Handlers;
import services.UserService;
import services.requests.MoveRequest;
import spark.*;
import websocket.messages.ServerMessage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Server {

    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    public WSServer websocketServer;

    private record PlayerSessionInfo(String username, int gameID, boolean isPlayer, ChessGame.TeamColor color) {}


    private Handlers handlers;

    public Server() {
        this.handlers = new Handlers(userService, gameService, authService);
        this.wakeupDatabase();
        this.websocketServer = new WSServer();
    }

    public void wakeupDatabase() {

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

        // Start the websocket server
        this.websocketServer.start(desiredPort);

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

            System.out.println("Ensuring tables exist...");

            // Ensure the database exists
            DatabaseManager.createDatabase();

            // Create AuthData table
            statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS AuthData (
                token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
            )
        """);

            // Create UserData table
            statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS UserData (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
        """);

            // Create GameData table
            statement.executeUpdate("""
            CREATE TABLE IF NOT EXISTS GameData (
                game_id INT AUTO_INCREMENT PRIMARY KEY,
                white_username VARCHAR(255),
                black_username VARCHAR(255),
                game_name VARCHAR(255) NOT NULL,
                game_json TEXT NOT NULL
            )
        """);

            System.out.println("Database tables ensured successfully.");

        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Error configuring database: " + e.getMessage(), e);
        }
    }
}
