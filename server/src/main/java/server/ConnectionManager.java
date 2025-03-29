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
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    public void add(String visitorName, Session session) {
        var connection = new Connection(visitorName, session);
        connections.put(visitorName, connection);
    }

    public void remove(String visitorName) {
        connections.remove(visitorName);
    }

    public void broadcast(String excludeVisitorName, Notification notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (Connection c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.visitorName.equals(excludeVisitorName)) {
                    c.send(notification.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.visitorName);
        }
    }

    private void broadcastToGame(int gameID, ServerMessage serverMessage, Map<Integer, Set<spark.Session>> gameSessions) {
        String json = gson.toJson(serverMessage);
        Set<spark.Session> sessions = gameSessions.getOrDefault(gameID, Set.of());
        for (Connection c : connections.values()) {
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