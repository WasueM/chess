package services;

import com.google.gson.Gson;
import spark.*;

public class Handlers {

    private final UserService userService;
    private final GameService gameService;
    private final AuthService authService;

    private static final Gson gson = new Gson();

    public Handlers(UserService userService, GameService gameService, AuthService authService) {
        this.userService = userService;
        this.gameService = gameService;
        this.authService = authService;
    }

    public Response handleRegister(Request request, Response response) {
        response.type("application/json");

        RegisterRequest registerRequest = gson.fromJson(request.body(), RegisterRequest.class);

        // debug helps
//        System.out.println(request.body().toString());

//        System.out.println("Username: " + registerRequest.username());
//        System.out.println("Password: " + registerRequest.password());
//        System.out.println("Email: " + registerRequest.email());

        try {
            RegisterResult result = userService.register(registerRequest);

            response.status(201);
            response.body(gson.toJson(result));
        } catch (Exception error) {
            response.body("Problem registering you");
            response.status(500);
        }

        return response;
    }

    public Response handleLogin(Request request, Response response) {
        response.type("application/json");

        LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);

        try {
            LoginResult result = userService.login(loginRequest);

            response.status(201);
            response.body(gson.toJson(result));
        } catch (Exception error) {
            response.body("Problem logging you in");
            response.status(500);
        }

        return response;
    }

    public Response handleLogout(Request request, Response response) {
        response.type("application/json");

        LogoutRequest logoutRequest = gson.fromJson(request.body(), LogoutRequest.class);

        try {
            LogoutResult result = authService.logout(logoutRequest);

            response.status(201);
            response.body(gson.toJson(result));
        } catch (Exception error) {
            response.body("Problem logging you out");
            response.status(500);
        }

        return response;
    }

    public Response handleGetGamesList(Request request, Response response) {
        response.type("application/json");

        GamesListRequest gamesListRequest = gson.fromJson(request.body(), GamesListRequest.class);

        try {
            GamesListResult result = gameService.getGamesList(gamesListRequest);

            response.status(201);
            response.body(gson.toJson(result));
        } catch (Exception error) {
            response.body("Problem getting active games");
            response.status(500);
        }

        return response;
    }

    public Response handleCreateGame(Request request, Response response) {
        response.type("application/json");

        CreateGameRequest createGameRequest = gson.fromJson(request.body(), CreateGameRequest.class);

        try {
            CreateGameResult result = gameService.createGame(createGameRequest);

            response.status(201);
            response.body(gson.toJson(result));
        } catch (Exception error) {
            response.body("Problem creating the game");
            response.status(500);
        }

        return response;
    }

    public Response handleJoinGame(Request request, Response response) {
        response.type("application/json");

        JoinGameRequest joinGameRequest = gson.fromJson(request.body(), JoinGameRequest.class);

        try {
            JoinGameResult result = gameService.joinGame(joinGameRequest);

            response.status(201);
            response.body(gson.toJson(result));
        } catch (Exception error) {
            response.body("Problem joining the game");
            response.status(500);
        }

        return response;
    }
}