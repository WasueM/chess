package services;

import com.google.gson.Gson;
import model.GameData;
import services.requests.*;
import services.results.*;
import spark.*;

public class Handlers {

    private UserService userService;
    private GameService gameService;
    private AuthService authService;

    private static final Gson GSON = new Gson();

    public Handlers(UserService userService, GameService gameService, AuthService authService) {
        this.userService = userService;
        this.gameService = gameService;
        this.authService = authService;
    }

    public void reset(UserService userService, GameService gameService, AuthService authService) {
        this.userService = userService;
        this.gameService = gameService;
        this.authService = authService;
    }

    public Response handleRegister(Request request, Response response) {
        response.type("application/json");

        RegisterRequest registerRequest = GSON.fromJson(request.body(), RegisterRequest.class);

        if ((registerRequest.username() == null) || (registerRequest.password() == null) || (registerRequest.email() == null)) {
            response.status(400);
            response.body("{\"message\":\"Error: Missing a parameter\"}");
            return response;
        }

        try {
            RegisterResult result = userService.register(registerRequest);

            if (result != null) {
                response.status(200);
                response.body(GSON.toJson(result));
            } else {
                response.status(403);
                response.body("{\"message\":\"Error: Problem registering you, perhaps a duplicate username\"}");
            }
        } catch (Exception error) {
            response.body("{\"message\":\"Error: Problem registering you\"}");
            response.status(400);
        }

        return response;
    }

    public Response handleLogin(Request request, Response response) {
        response.type("application/json");

        LoginRequest loginRequest = GSON.fromJson(request.body(), LoginRequest.class);

        try {
            LoginResult result = userService.login(loginRequest);

            if (result != null) {
                response.status(200);
                response.body(GSON.toJson(result));
            } else {
                response.status(401);
                response.body("{\"message\":\"Error: Invalid username or password\"}");
            }
        } catch (Exception error) {
            response.body("{\"message\":\"Error: Problem logging you in\"}");
            response.status(401);
        }

        return response;
    }

    public Response handleLogout(Request request, Response response) {
        response.type("application/json");

        LogoutRequest logoutRequest = new LogoutRequest(request.headers("Authorization"));

        try {
            LogoutResult result = authService.logout(logoutRequest);

            if (result != null) {
                response.status(200);
                response.body(GSON.toJson(result));
            } else {
                response.status(401);
                response.body("{\"message\":\"Error: Problem logging you out\"}");
            }
        } catch (Exception error) {
            response.status(401);
            response.body("{\"message\":\"Error: Problem logging you out\"}");
        }

        return response;
    }

    public Response handleGetGamesList(Request request, Response response) {
        response.type("application/json");

        GamesListRequest gamesListRequest = new GamesListRequest(request.headers("Authorization"));

        try {
            GamesListResult result = gameService.getGamesList(gamesListRequest);

            if (result != null) {
                response.status(200);
                for (GameData r : result.games()) {
                    System.out.println(r.toString());
                }
                response.body(GSON.toJson(result));
            } else {
                response.status(401);
                response.body("{\"message\":\"Error: Problem getting active games\"}");
            }
        } catch (Exception error) {
            response.body("{\"message\":\"Error: Problem getting active games\"}");
            response.status(401);
        }

        return response;
    }

    public Response handleCreateGame(Request request, Response response) {
        response.type("application/json");

        CreateGameRequest createGameRequest = GSON.fromJson(request.body(), CreateGameRequest.class);
        createGameRequest = new CreateGameRequest(request.headers("Authorization"), createGameRequest.gameName());

        try {
            CreateGameResult result = gameService.createGame(createGameRequest);

            if (result != null) {
                response.status(200);
                response.body(GSON.toJson(result));
            } else {
                response.status(401);
                response.body("{\"message\":\"Error: Your auth Token was likely invalid\"}");
            }

        } catch (Exception error) {
            response.body("Problem creating the game");
            response.status(500);
        }

        return response;
    }

    public Response handleJoinGame(Request request, Response response) {
        response.type("application/json");

        JoinGameRequest joinGameRequest = GSON.fromJson(request.body(), JoinGameRequest.class);
        joinGameRequest = new JoinGameRequest(request.headers("Authorization"), joinGameRequest.gameID(), joinGameRequest.playerColor());

        if (joinGameRequest.playerColor() == null) {
            response.body("{\"message\":\"Error: Please enter a valid team color, 'BLACK' or 'WHITE'\"}");
            response.status(400);
            return response;
        }

        if ((!joinGameRequest.playerColor().equals("BLACK")) && (!joinGameRequest.playerColor().equals("WHITE"))) {
            response.body("{\"message\":\"Error: Please enter a valid team color, 'BLACK' or 'WHITE'\"}");
            response.status(400);
            return response;
        }

        try {
            JoinGameResult result = gameService.joinGame(joinGameRequest);

            if (result != null) {
                response.status(200);
                response.body(GSON.toJson(result));
            } else {
                response.status(401);
                response.body("{\"message\":\"Error: Problem joining the game. Check your auth token\"}");
            }
        } catch (Exception error) {
            if (error.getMessage() == "Failed to join the game because that color is already taken") {
                response.body("{\"message\":\"Error: Failed to join the game because that color is already taken\"}");
                response.status(403);
            } else {
                response.body("{\"message\":\"Error: Problem joining the game, is your game id right?\"}");
                response.status(400);
            }
        }

        return response;
    }
}