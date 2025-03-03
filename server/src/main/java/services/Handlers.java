package services;

import com.google.gson.Gson;
import spark.*;

public class Handlers {

    private UserService userService;
    private GameService gameService;
    private AuthService authService;

    private static final Gson gson = new Gson();

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

        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

        if ((registerRequest.username() == null) || (registerRequest.password() == null) || (registerRequest.email() == null)) {
            response.status(400);
            response.body("{\"message\":\"Error: Missing a parameter\"}");
            return response;
        }

        try {
            RegisterResult result = userService.register(registerRequest);

            if (result != null) {
                response.status(200);
                response.body(gson.toJson(result));
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

        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

        System.out.println(request.body().toString());

        try {
            LoginResult result = userService.login(loginRequest);

            if (result != null) {
                response.status(200);
                response.body(gson.toJson(result));
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
            System.out.println(request.body());
            System.out.println(logoutRequest.authToken());

            LogoutResult result = authService.logout(logoutRequest);

            System.out.println(result.toString());

            if (result != null) {
                response.status(200);
                response.body(gson.toJson(result));
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

        //System.out.println(gamesListRequest.toString());

        try {
            GamesListResult result = gameService.getGamesList(gamesListRequest);

            System.out.println(result.toString());

            if (result != null) {
                response.status(200);
                response.body(gson.toJson(result));
            } else {
                response.status(500);
                response.body("{\"message\":\"Error: Problem getting active games\"}");
            }
        } catch (Exception error) {
            response.body("{\"message\":\"Error: Problem getting active games\"}");
            response.status(500);
        }

        return response;
    }

    public Response handleCreateGame(Request request, Response response) {
        response.type("application/json");

        CreateGameRequest createGameRequest = gson.fromJson(request.body(), CreateGameRequest.class);
        createGameRequest = new CreateGameRequest(request.headers("Authorization"), createGameRequest.gameName());

        System.out.println(createGameRequest.toString());

        try {
            CreateGameResult result = gameService.createGame(createGameRequest);
            System.out.println("HEY");

            if (result != null) {
                response.status(200);
                response.body(gson.toJson(result));
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

        System.out.println(request.body().toString());

        JoinGameRequest joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);
        joinGameRequest = new JoinGameRequest(request.headers("Authorization"), joinGameRequest.gameID(), joinGameRequest.playerColor());


        System.out.println(joinGameRequest.toString());

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
                response.body(gson.toJson(result));
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