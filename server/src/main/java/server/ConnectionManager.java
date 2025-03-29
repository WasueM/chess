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

    public void add(String authToken, Session session) {
        var connection = new WSConnection(authToken, session);
        connections.put(authToken, connection);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    public void broadcastToAll(Notification notification) throws IOException {
        var removeList = new ArrayList<WSConnection>();
        for (WSConnection c : connections.values()) {
            if (c.session.isOpen()) {
                    c.send(notification.toString());
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.remove(c.authToken);
        }
    }

    public void broadcastToAllExcluding(String excludeAuthToken, Notification notification) throws IOException {
        var removeList = new ArrayList<WSConnection>();
        for (WSConnection c : connections.values()) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(excludeAuthToken)) {
                    c.send(notification.toString());
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

    public void broadcastToSpecificConnection(String specificAuthToken, Notification notification) throws IOException {
        var removeList = new ArrayList<WSConnection>();
        for (WSConnection c : connections.values()) {
            if (c.session.isOpen()) {
                if (c.authToken.equals(specificAuthToken)) {
                    c.send(notification.toString());
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