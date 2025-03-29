package server;

import chess.ChessMove;
import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import services.requests.MoveRequest;
import spark.Spark;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();

    private final ConnectionManager connections = new ConnectionManager();

    public static void start(int port) {
        Spark.port(port);
        Spark.webSocket("/ws", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("CONNECT: " + gameID + " " + authToken);
                connections.add(authToken, session);
            }
            case LEAVE -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("LEAVE: " + gameID + " " + authToken);
                connections.remove(authToken);
            }
            case MAKE_MOVE -> {
                ChessMove move = command.getChessMove();
                System.out.println("Got chess move: " + move);
                this.handleMove(move);
            }
            case RESIGN -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("RESIGN: " + gameID + " " + authToken);
            }
        }
    }

    public void handleMove(ChessMove move) {
        // find which game its from
        GameData updatedGame = gameService.handleMove(new MoveRequest(gameID, move));  // Example method
        connections.broadcastToGame(gameID, ServerMessage.loadGame(updatedGame));
        connections.broadcastToGame(gameID, ServerMessage.notification(userName + " moved from ... to ..."));
    }
}