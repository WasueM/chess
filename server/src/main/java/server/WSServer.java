package server;

import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.*;
import services.AuthService;
import services.GameService;
import services.UserService;
import services.requests.MoveRequest;
import services.results.MoveResult;
import spark.Spark;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();
    private AuthService authService;
    private GameService gameService;
    private UserService userService;

    private final ConnectionManager connections = new ConnectionManager();

    public static void start(int port) {
        Spark.port(port);
        Spark.webSocket("/ws", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    public void setServices(AuthService authService, GameService gameService, UserService userService) {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("CONNECT: " + gameID + " " + authToken);
                connections.add(authToken, session, gameID);
            }
            case LEAVE -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("LEAVE: " + gameID + " " + authToken);
                connections.remove(authToken);
            }
            case MAKE_MOVE -> {
                ChessMove move = command.getChessMove();
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("Got chess move: " + move);
                this.handleMove(move, gameID, authToken);
            }
            case RESIGN -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("RESIGN: " + gameID + " " + authToken);
            }
        }
    }

    public void handleMove(ChessMove move, int gameID, String authToken) throws DataAccessException, InvalidMoveException {
        // find which game its from
        MoveResult result = gameService.handleMove(new MoveRequest(authToken, gameID, move));
        GameData updatedGame = result.game();
        ServerMessage gameUpdateMessage = ServerMessage.loadGame(updatedGame);
        connections.broadcastToAll(gameID, ServerMessage.loadGame(updatedGame));
        ServerMessage chessMoveNotification = ServerMessage.error(ServerMessage.notification(authService.getUserByAuthToken(authToken)) + " moved from ... to ...");
        connections.broadcastToAll(gameID, chessMoveNotification);
    }
}