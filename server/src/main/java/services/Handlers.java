package services;

import com.google.gson.Gson;

//record ServerResponse(int statusCode, String message) {}

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

    public String handleRegister(String request, String response) {
        RegisterRequest registerRequest = gson.fromJson(request, RegisterRequest.class);
        try {
            RegisterResult result = userService.register(registerRequest);
            if (result == null) {
                return gson.toJson("Username already in use");
            }
            return gson.toJson(result);
        } catch (Exception error) {
            return gson.toJson(error.getMessage());
        }
    }
}