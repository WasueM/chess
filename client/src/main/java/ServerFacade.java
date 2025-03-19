import chess.ChessBoard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    private String serverURL = "http://localhost:8080/";
    private String authToken = "emptyDefaultToken";

    // the same as the one used on the server side
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoardJSONAdapter())
            .create();

    public ServerFacade(String url) throws Exception {
        serverURL = url;
    }

    public void connectToServer(String url) throws Exception {
        serverURL = url;
        HttpURLConnection http = sendRequest(url, "GET", null, null);
        System.out.println("Connected to server!");

        JsonObject response = receiveResponse(http);
        authToken = response.;
    }

    public void register(String username, String password, String email) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        body.addProperty("email", email);

        HttpURLConnection http = sendRequest(serverURL + "user", "POST", null, body.toString());
        String response = receiveResponse(http);

        // turn the response into our auth data model so we can use it
        AuthData authData = new Gson().fromJson(response, AuthData.class);

        // store the auth token here in the facade so we can easily use it whenever we need to
        this.authToken = authData.authToken();
    }

    public void login(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        HttpURLConnection http = sendRequest(serverURL + "session", "POST", null, body.toString());
        String response = receiveResponse(http);

        // turn the response into our auth data model so we can use it
        AuthData authData = new Gson().fromJson(response, AuthData.class);

        // store the auth token here in the facade so we can easily use it whenever we need to
        this.authToken = authData.authToken();
    }

    public void logout(String authToken) throws Exception {
        sendRequest(serverURL + "session", "DELETE", authToken, null);
        this.authToken = "loggedOutSoNoTokenHere";
    }

    public void listGames(String authToken) throws Exception {
        HttpURLConnection http = sendRequest(serverURL + "game", "GET", authToken, null);
        String response = receiveResponse(http);

        // turn the response into our game data models so we can use it
        GameData[] games = new GameData[]{new Gson().fromJson(response, GameData.class)};

        // store the auth token here in the facade so we can easily use it whenever we need to
        this.authToken = authData.authToken();
    }

    public void createGame(String authToken, String gameName) throws Exception {
        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("gameName", gameName);

        HttpURLConnection http = sendRequest(serverURL + "game", "POST", authToken, bodyJson.toString());
        receiveResponse(http);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("playerColor", playerColor);
        body.addProperty("gameID", gameID);

        HttpURLConnection http = sendRequest(serverURL + "game", "PUT", authToken, body.toString());
        receiveResponse(http);
    }

    public void clearDatabase() throws Exception {
        HttpURLConnection http = sendRequest(serverURL + "db", "DELETE", null, null);
        receiveResponse(http);
    }

    private static HttpURLConnection sendRequest(String url, String method, String authToken, String body) throws URISyntaxException, IOException {
        URI uri = new URI(url);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type", "application/json");

        // if we have an auth token then add it to the request
        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }

        // put the body into the request
        if (body != null && !body.isEmpty()) {
            http.setDoOutput(true);
            try (OutputStream os = http.getOutputStream()) {
                os.write(body.getBytes());
            }
        }

        // debug
        System.out.printf("= Request =========\n[%s] %s\n\n%s\n\n", method, url, body != null ? body : "");

        // make the request
        http.connect();

        // Print why when it can't connect
        var status = http.getResponseCode();
        if ( status >= 200 && status < 300) {
            try (InputStream in = http.getInputStream()) {
                System.out.println(new Gson().fromJson(new InputStreamReader(in), Map.class));
            }
        } else {
            try (InputStream in = http.getErrorStream()) {
                System.out.println(new Gson().fromJson(new InputStreamReader(in), Map.class));
            }
        }

        return http;
    }

    // returns a string version of the response body so that Gson can convert that into whatever we need later
    private static String receiveResponse(HttpURLConnection http) throws IOException {
        var statusCode = http.getResponseCode();
        var statusMessage = http.getResponseMessage();

        String responseBody = readResponseBody(http);
        System.out.printf("= Response =========\n[%d] %s\n\n%s\n\n", statusCode, statusMessage, responseBody);
        return responseBody;
    }

    // using StringBuilder and string readers to understand the response and make it a string for Gson
    private static String readResponseBody(HttpURLConnection http) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        try (InputStream respBody = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(respBody)) {
            int c;
            while ((c = reader.read()) != -1) {
                responseBuilder.append((char) c);
            }
        }
        return responseBuilder.toString();
    }
}
