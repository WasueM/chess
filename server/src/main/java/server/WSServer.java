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
import services.requests.GamesListRequest;
import services.requests.MoveRequest;
import services.results.GamesListResult;
import services.results.MoveResult;
import spark.Spark;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WSServer {
    private static final Gson gson = new Gson();
    private static AuthService authService;
    private static GameService gameService;
    private static UserService userService;

    private final ConnectionManager connections = new ConnectionManager();

    public static void start(int port) {
        Spark.webSocket("/ws", WSServer.class);
        Spark.get("/echo/:msg", (req, res) -> "HTTP response: " + req.params(":msg"));
    }

    public void setServices(AuthService authService, GameService gameService, UserService userService) {
        this.authService = authService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException, InvalidMoveException, IOException {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("CONNECT: " + gameID + " " + authToken);
                connections.add(authToken, session, gameID);

                // if its an invalid token, stop right there
                boolean isTokenValid = authService.verifyAuthToken(authToken);
                if (!isTokenValid) {
                    ServerMessage errorMessage = ServerMessage.error("Error: Bad Auth Token");
                    connections.broadcastToSpecificConnection(authToken, errorMessage);
                    return;
                }

                // get the player name
                String joinerName = authService.getUserByAuthToken(authToken);

                // get the game
                GamesListRequest request = new GamesListRequest(authToken);
                GamesListResult gameListResult = gameService.getGamesList(request);
                GameData[] games = gameListResult.games();
                GameData game = null;
                for (GameData g : games) {
                    if (g.gameID() == gameID) {
                        game = g;
                    }
                }

                // if the game is still null, return an error to the sender
                if (game == null) {
                    ServerMessage errorMessage = ServerMessage.error("Error: Bad Game ID");
                    connections.broadcastToSpecificConnection(authToken, errorMessage);
                    return;
                }

                // figure out what color they joined
                String teamJoined = "NONE";
                boolean observing = true;
                if (game.blackUsername().equals(joinerName)) {
                    teamJoined = "BLACK";
                    observing = false;
                } else if (game.whiteUsername().equals(joinerName)) {
                    teamJoined = "WHITE";
                    observing = false;
                }

                // make the message to send to the original sender
                ServerMessage loadGameMessage = ServerMessage.loadGame(game);
                connections.broadcastToSpecificConnection(authToken, loadGameMessage);

                if (observing == true) {
                    // make the message to send to all the others
                    ServerMessage playerJoinedNotification = ServerMessage.notification(joinerName + " joined the game as an observer.");
                    connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
                } else {
                    // make the message to send to all the others
                    ServerMessage playerJoinedNotification = ServerMessage.notification(joinerName + " joined the " + teamJoined + "  team.");
                    connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
                }
            }
            case LEAVE -> {
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();
                System.out.println("LEAVE: " + gameID + " " + authToken);
                connections.remove(authToken);

                // get the player name
                String leaverName = authService.getUserByAuthToken(authToken);

                // make the message to everyone
                ServerMessage playerJoinedNotification = ServerMessage.notification(leaverName + " left the game.");
                connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
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

                // get the player name
                String resignerName = authService.getUserByAuthToken(authToken);

                // make the message to everyone
                ServerMessage playerJoinedNotification = ServerMessage.notification(resignerName + " RESIGNED! Game over!");
                connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
            }
        }
    }

    public void handleMove(ChessMove move, int gameID, String authToken) throws DataAccessException, InvalidMoveException, IOException {
        // find which game its from
        MoveResult result = gameService.handleMove(new MoveRequest(authToken, gameID, move));
        GameData updatedGame = result.game();
        ServerMessage gameUpdateMessage = ServerMessage.loadGame(updatedGame);
        connections.broadcastToAll(gameID, gameUpdateMessage);
        ServerMessage chessMoveNotification = ServerMessage.notification(authService.getUserByAuthToken(authToken) + " moved from ("
                + move.getStartPosition().getRow() + "," + columnToLetter(move.getStartPosition().getColumn()) + ") to ("
                + move.getEndPosition().getRow() + "," + columnToLetter(move.getEndPosition().getColumn()) + ").");
        connections.broadcastToAll(gameID, chessMoveNotification);
    }

    public static String columnToLetter(int column) {
        if (column < 1 || column > 8) {
            return "FAILED TO GET THE COLUMN LETTER";
        }

        char letter = (char) ('a' + (column - 1));
        return String.valueOf(letter);
    }
}