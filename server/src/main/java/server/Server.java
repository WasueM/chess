package server;

import dataaccess.AuthDataAccessMemory;
import dataaccess.GameDataAccessMemory;
import dataaccess.UserDataAccessMemory;
import services.AuthService;
import services.GameService;
import services.Handlers;
import services.UserService;
import spark.*;

public class Server {

    private AuthService authService;
    private UserService userService;
    private GameService gameService;

    private Handlers handlers;

    public Server() {
        this.resetDatabase();

        this.handlers = new Handlers(userService, gameService, authService);
    }

    public void resetDatabase() {
        // create the memory version of the database
        AuthDataAccessMemory authDataAccessMemory = new AuthDataAccessMemory();
        GameDataAccessMemory gameDataAccessMemory = new GameDataAccessMemory();
        UserDataAccessMemory userDataAccessMemory = new UserDataAccessMemory();

        // create the services based on the version of the database we want
        this.authService = new AuthService(authDataAccessMemory);
        this.gameService = new GameService(gameDataAccessMemory, authService);
        this.userService = new UserService(userDataAccessMemory, authService);
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
}
