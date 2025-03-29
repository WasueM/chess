package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import spark.Spark;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();

    public static void start(int port) {
        Spark.port(port);
        Spark.webSocket("/ws", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {
        System.out.printf("Received: %s", message);

        // figure out what command was send
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT:
                session.getRemote().sendString(gson.toJson(new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION)));
                break;
            case MAKE_MOVE:
                // Handle making a move (You will integrate this with game logic)
                session.getRemote().sendString("Move received");
                break;
            case LEAVE:
                session.getRemote().sendString("User left the game");
                break;
            case RESIGN:
                session.getRemote().sendString("User resigned");
                break;
        }

        session.getRemote().sendString("WebSocket response: " + message);
    }
}