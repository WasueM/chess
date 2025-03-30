package server;

import chess.*;
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
        System.out.println("DEBUG: Received raw JSON: " + message);

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
//                    String output = " WHITE USERNAME: " + game.whiteUsername();
//                    output = output + (" BLACK USERNAME: " + game.blackUsername());
//                    output = output + (" USERNAME: " + joinerName);

                    ServerMessage playerJoinedNotification = ServerMessage.notification(joinerName + " joined the game as an observer.");
//                    playerJoinedNotification = ServerMessage.notification(output);
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
                ChessMove move = command.getMove();
                int gameID = command.getGameID();
                String authToken = command.getAuthToken();

                // if its an invalid token, stop right there
                boolean isTokenValid = authService.verifyAuthToken(authToken);
                if (!isTokenValid) {
                    // send an error directly to the sending session
                    ServerMessage errorMessage = ServerMessage.error("Error: Recieved Bad Auth Token for Make Move");
                    String json = gson.toJson(errorMessage);
                    session.getRemote().sendString(json);
                    return;
                }

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
                connections.broadcastToAll(gameID, playerJoinedNotification);
            }
        }
    }

    public void handleMove(ChessMove move, int gameID, String authToken) throws DataAccessException, InvalidMoveException, IOException {
        // see if the piece to move is from the right color
        GamesListRequest request = new GamesListRequest(authToken);
        GamesListResult gameListResult = gameService.getGamesList(request);
        GameData[] games = gameListResult.games();
        GameData game = null;
        for (GameData g : games) {
            if (g.gameID() == gameID) {
                game = g;
            }
        }

        // figure out the senders' color
        String senderUsername = authService.getUserByAuthToken(authToken);
        ChessGame.TeamColor senderColor = null;
        boolean observing = true;
        if (game.blackUsername().equals(senderUsername)) {
            senderColor = ChessGame.TeamColor.BLACK;
            observing = false;
        } else if (game.whiteUsername().equals(senderUsername)) {
            senderColor = ChessGame.TeamColor.WHITE;
            observing = false;
        }
        System.out.println("HO2");

        // get the position of the piece, then the piece
        ChessPosition position = move.getStartPosition();
        ChessPiece pieceAtPosition = game.game().chessBoard.getPiece(position);
        if (pieceAtPosition == null) {
            // they tried to move something that isn't even there
            ServerMessage errorMessage = ServerMessage.error("Error: There's no piece to move at that location!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
            return;
        }
        if (!pieceAtPosition.getTeamColor().equals(senderColor)) {
            // then its the wrong color, so can't do that!
            ServerMessage errorMessage = ServerMessage.error("Error: Can't move the other team's piece!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
            return;
        }

        try {
            // handle the move
            MoveResult result = gameService.handleMove(new MoveRequest(authToken, gameID, move));

            // find which game its from
            GameData updatedGame = result.game();
            ServerMessage gameUpdateMessage = ServerMessage.loadGame(updatedGame);
            connections.broadcastToAll(gameID, gameUpdateMessage);
            ServerMessage chessMoveNotification = ServerMessage.notification(authService.getUserByAuthToken(authToken) + " moved from ("
                    + move.getStartPosition().getRow() + "," + columnToLetter(move.getStartPosition().getColumn()) + ") to ("
                    + move.getEndPosition().getRow() + "," + columnToLetter(move.getEndPosition().getColumn()) + ").");
            connections.broadcastToAllExcluding(gameID, authToken, chessMoveNotification);

            // check for check and checkmate
            boolean isInCheckMateWhite = updatedGame.game().isInCheckmate(ChessGame.TeamColor.WHITE);
            boolean isInCheckMateBlack = updatedGame.game().isInCheckmate(ChessGame.TeamColor.WHITE);
            boolean isInCheckWhite = updatedGame.game().isInCheck(ChessGame.TeamColor.WHITE);
            boolean isInCheckBlack = updatedGame.game().isInCheck(ChessGame.TeamColor.WHITE);
            if (isInCheckMateWhite) {
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.whiteUsername() + " is in checkmate!");
                connections.broadcastToAll(gameID, checkNotification);
            } else if (isInCheckMateBlack) {
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.blackUsername() + " is in checkmate!");
                connections.broadcastToAll(gameID, checkNotification);
            } else if (isInCheckWhite) {
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.whiteUsername() + " is in check!");
                connections.broadcastToAll(gameID, checkNotification);
            } else if (isInCheckBlack) {
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.blackUsername() + " is in check!");
                connections.broadcastToAll(gameID, checkNotification);
            }
        } catch (Exception error) {
            ServerMessage errorMessage = ServerMessage.error("Error: Invalid Move!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
        }
    }

    public static String columnToLetter(int column) {
        if (column < 1 || column > 8) {
            return "FAILED TO GET THE COLUMN LETTER";
        }

        char letter = (char) ('a' + (column - 1));
        return String.valueOf(letter);
    }
}