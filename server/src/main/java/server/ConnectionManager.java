package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import javax.management.Notification;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, WSConnection> connections = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    public void add(String authToken, Session session, int gameID) {
        var connection = new WSConnection(authToken, session, gameID);
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void broadcastToAll(int gameID, ServerMessage serverMessage) throws IOException {

        String json = gson.toJson(serverMessage);

        var removeList = new ArrayList<WSConnection>();
        for (WSConnection c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.gameID == gameID) {
                    c.send(json);
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    public void broadcastToAllExcluding(int gameID, String excludeAuthToken, ServerMessage serverMessage) throws IOException {

        String json = gson.toJson(serverMessage);

        var removeList = new ArrayList<WSConnection>();
        for (WSConnection c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.gameID == gameID) {
                    if (!c.authToken.equals(excludeAuthToken)) {
                        c.send(json);
                    }
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    public void broadcastToSpecificConnection(String specificAuthToken, ServerMessage serverMessage) throws IOException {

        String json = gson.toJson(serverMessage);

        var removeList = new ArrayList<WSConnection>();
        for (WSConnection c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.authToken.equals(specificAuthToken)) {
                    c.send(json);
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }
}