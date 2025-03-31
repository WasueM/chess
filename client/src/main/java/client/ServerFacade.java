package client;

import chess.ChessBoard;
import chess.ChessBoardJSONAdapter;
import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.Map;

public class ServerFacade {

    private String serverURL = "http://localhost:8080/";
    private String authToken = "emptyDefaultToken";
    private WSClient websocketClient;

    // the same as the one used on the server side
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ChessBoard.class, new ChessBoardJSONAdapter())
            .create();

    public ServerFacade(String url) throws Exception {
        serverURL = url;
        this.websocketClient = new WSClient(url);
    }

    public AuthData register(String username, String password, String email) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        body.addProperty("email", email);

        HttpURLConnection http = sendRequest(serverURL + "user", "POST", null, body.toString());
        String response = receiveResponse(http);

        // turn the response into our auth data model so we can use it
        AuthData authData = GSON.fromJson(response, AuthData.class);

        // store the auth token here in the facade so we can easily use it whenever we need to
        this.authToken = authData.authToken();

        // return the auth data
        return authData;
    }

    public AuthData login(String username, String password) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);

        HttpURLConnection http = sendRequest(serverURL + "session", "POST", null, body.toString());
        String response = receiveResponse(http);

        // turn the response into our auth data model so we can use it
        AuthData authData = GSON.fromJson(response, AuthData.class);

        // store the auth token here in the facade so we can easily use it whenever we need to
        this.authToken = authData.authToken();

        // return auth data
        return authData;
    }

    public boolean logout() throws Exception {
        try {
            sendRequest(serverURL + "session", "DELETE", this.authToken, null);
            this.authToken = "loggedOutSoNoTokenHere";

            return true; // successful
        } catch (Exception error) {
            return false; // failed
        }
    }

    public GameData[] listGames() throws Exception {
        HttpURLConnection http = sendRequest(serverURL + "game", "GET", this.authToken, null);
        String response = receiveResponse(http);

        // since the games come wrapped in another level of json, we need this to help gson get the games out
        var type = new TypeToken<Map<String, GameData[]>>(){}.getType();
        Map<String, GameData[]> map = GSON.fromJson(response, type);

        // get the games from the wrapped data structure
        GameData[] games = map.get("games");

        return games;
    }

    public int createGame(String gameName) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("gameName", gameName);

        HttpURLConnection http = sendRequest(serverURL + "game", "POST", this.authToken, body.toString());
        String response = receiveResponse(http);

        // make the response into a game id
        JsonObject json = GSON.fromJson(response, JsonObject.class);
        int gameID = json.get("gameID").getAsInt();

        return gameID;
    }

    public int joinGame(String playerColor, int gameID) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("playerColor", playerColor);
        body.addProperty("gameID", gameID);

        HttpURLConnection http = sendRequest(serverURL + "game", "PUT", this.authToken, body.toString());
        String response = receiveResponse(http);

        JsonObject json = GSON.fromJson(response, JsonObject.class);
        int id = json.get("gameID").getAsInt();

        return id;
    }

    public boolean clearDatabase() throws Exception {
        HttpURLConnection http = sendRequest(serverURL + "db", "DELETE", null, null);
        String response = receiveResponse(http);
        if (response != null) {
            return true; // yes it was deleted
        } else {
            return false; // failed to delete
        }
    }

    private static HttpURLConnection sendRequest(String url, String method, String authToken, String body) throws Exception {
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

        // make the request
        http.connect();

        return http;
    }

    // returns a string version of the response body so that Gson can convert that into whatever we need later
    private static String receiveResponse(HttpURLConnection http) throws Exception {
        // Print why when it can't connect
        var status = http.getResponseCode();
        if ( status < 200 || status >= 300) {
            String errorResponse = readResponseBody(http);
            throw new Exception("Server didn't respond 200 OK, so there was a problem: " + errorResponse);
        }

        String responseBody = readResponseBody(http);
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

    public void sendConnect(int gameID) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, this.authToken, gameID, null);
        this.websocketClient.sendCommand(command);
    }

    public void sendLeave(int gameID) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, this.authToken, gameID, null);
        this.websocketClient.sendCommand(command);
    }

    public void sendMakeMove(ChessMove move, int gameID) throws Exception {
        System.out.println("SENDING MOVE: " + move.toString());
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, this.authToken, gameID, move);
        this.websocketClient.sendCommand(command);
    }

    public void sendResign(int gameID) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, this.authToken, gameID, null);
        this.websocketClient.sendCommand(command);
    }
}
