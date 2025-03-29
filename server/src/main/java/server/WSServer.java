package server;

import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import spark.Spark;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();

    public static void start(int port) {
        Spark.port(port);
        Spark.webSocket("/ws", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        ServerMessage sm = gson.fromJson(message, ServerMessage.class);
        switch (sm.getServerMessageType()) {
            case LOAD_GAME -> {
                GameData gameData = sm.getGame();
                
            }
            case ERROR -> {
                System.err.println("Received error: " + sm.getErrorMessage());
            }
            case NOTIFICATION -> {
                System.out.println("Notification: " + sm.getMessage());
            }
        }
    }


    private void broadcastToGame(int gameID, ServerMessage serverMessage, Map<Integer, Set<spark.Session>> gameSessions) {
        String json = gson.toJson(serverMessage);
        Set<spark.Session> sessions = gameSessions.getOrDefault(gameID, Set.of());
        for (spark.Session s : sessions) {
            try {
                s.getRemote().sendString(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendToUser(Session session, Object serverMessageObj) {
        String json = gson.toJson(serverMessageObj);
        try {
            session.getRemote().sendString(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}