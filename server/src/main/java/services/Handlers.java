package services;

import com.google.gson.Gson;

//record ServerResponse(int statusCode, String message) {}

public class Handlers {
    private static final Gson gson = new Gson();
    private static final UserService userService = new UserService();

    public static String handleRegister(String request, String response) {
        RegisterRequest registerRequest = gson.fromJson(request, RegisterRequest.class);
        try {
            RegisterResult result = userService.register(registerRequest);
            if (result == null) {
//                ServerResponse serverResponse = new ServerResponse(450, "Username already in use");
                return gson.toJson("Username already in use");
            }
            return gson.toJson(result);
        } catch (Exception error) {
            return gson.toJson(error.getMessage());
        }
    }
}