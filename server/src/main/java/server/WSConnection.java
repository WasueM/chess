package server;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class WSConnection {
    public String authToken;
    public Session session;
    public int gameID;

    public WSConnection(String authToken, Session session, int gameID) {
        this.authToken = authToken;
        this.session = session;
        this.gameID = gameID;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}