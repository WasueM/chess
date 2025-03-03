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

    //        RegisterRequest registerRequest = gson.fromJson(request, RegisterRequest.class);
//        try {
//            RegisterResult result = userService.register(registerRequest);
//            if (result == null) {
//                return gson.toJson("Username already in use");
//            }
//            return gson.toJson(result);
//        } catch (Exception error) {
//            return gson.toJson(error.getMessage());
//        }
}