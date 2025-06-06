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
import services.requests.JoinGameRequest;
import services.requests.MoveRequest;
import services.results.GamesListResult;
import services.results.MoveResult;
import spark.Spark;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WebSocket
public class WSServer {
    private static final Gson GSON = new Gson();
    private static AuthService authService;
    private static GameService gameService;
    private static UserService userService;

    List<Integer> finishedGames = new ArrayList<>();

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

        UserGameCommand command = GSON.fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> {
                this.handleConnect(command, session);
            }
            case LEAVE -> {
                this.handleLeave(command, session);
            }
            case MAKE_MOVE -> {
                this.handleMakeMove(command, session);
            }
            case RESIGN -> {
                this.handleResign(command, session);
            }
        }
    }

    public void handleResign(UserGameCommand command, Session session) throws IOException, DataAccessException {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        System.out.println("RESIGN: " + gameID + " " + authToken);

        // get the player name
        String resignerName = authService.getUserByAuthToken(authToken);

        // get the game
        GameData theGame = this.getGame(authToken, gameID);
        if (theGame == null) {
            return; // the error notification is handled inside get game, so nothing else needed
        }

        // figure out if they're an observer
        boolean observing = true;
        if (Objects.equals(theGame.whiteUsername(), resignerName)) {
            observing = false;
        } else if (Objects.equals(theGame.blackUsername(), resignerName)) {
            observing = false;
        }

        if (observing == true) {
            // can't resign since this is from an observer!
            ServerMessage errorMessage = ServerMessage.error("Error: Can't resign, as you are an observer!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
            return;
        }

        if (this.finishedGames.contains(gameID)) {
            // can't resign from it since it's already resigned from!
            ServerMessage errorMessage = ServerMessage.error("Error: Can't resign, as someone already lost!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
            return;
        }

        // lock that game so no future things can be sent from it
        this.finishedGames.add(gameID);

        // make the message to everyone
        ServerMessage playerJoinedNotification = ServerMessage.notification(resignerName + " RESIGNED! Game over!");
        connections.broadcastToAll(gameID, playerJoinedNotification);
    }

    public void handleMakeMove(UserGameCommand command, Session session) throws DataAccessException, IOException, InvalidMoveException {
        ChessMove move = command.getMove();
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();

        // if its an invalid token, stop right there
        boolean isTokenValid = authService.verifyAuthToken(authToken);
        if (!isTokenValid) {
            // send an error directly to the sending session
            ServerMessage errorMessage = ServerMessage.error("Error: Recieved Bad Auth Token for Make Move");
            String json = GSON.toJson(errorMessage);
            session.getRemote().sendString(json);
            return;
        }

        // if this game's already over, don't let them make any moves
        if (this.finishedGames.contains(gameID)) {
            ServerMessage errorMessage = ServerMessage.error("Error: Can't make a move, as someone already lost!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
            return;
        }

        System.out.println("Got chess move: " + move);
        this.handleMove(move, gameID, authToken);
    }

    public void handleLeave(UserGameCommand command, Session session) throws DataAccessException, IOException {
        int gameID = command.getGameID();
        String authToken = command.getAuthToken();
        System.out.println("LEAVE: " + gameID + " " + authToken);
        connections.remove(authToken);

        // get the player name
        String leaverName = authService.getUserByAuthToken(authToken);

        // update the game so it has null there instead of username, so others can join
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
        String senderColor = null;
        boolean observing = true;
        if (Objects.equals(game.blackUsername(), leaverName)) {
            senderColor = "BLACK";
            observing = false;
        } else if (Objects.equals(game.whiteUsername(), leaverName)) {
            senderColor = "WHITE";
            observing = false;
        }

        if (!observing) {
            // actually leave the game in the database
            JoinGameRequest leaveRequest = new JoinGameRequest(authToken, gameID, senderColor);
            gameService.leaveGame(leaveRequest);
        }

        // make the message to everyone
        ServerMessage playerJoinedNotification = ServerMessage.notification(leaverName + " left the game.");
        connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
    }

    public void handleConnect(UserGameCommand command, Session session) throws IOException, DataAccessException {
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
        GameData game = this.getGame(authToken, gameID);
        if (game == null) {
            return; // the error notification is handled inside get game, so nothing else needed
        }

        // figure out what color they joined
        String teamJoined = "NONE";
        boolean observing = true;
        if (Objects.equals(game.blackUsername(), joinerName)) {
            teamJoined = "BLACK";
            observing = false;
        } else if (Objects.equals(game.whiteUsername(), joinerName)) {
            teamJoined = "WHITE";
            observing = false;
        }

        // make the message to send to the original sender
        ServerMessage loadGameMessage = ServerMessage.loadGame(game);
        connections.broadcastToSpecificConnection(authToken, loadGameMessage);

        if (observing == true) {
            ServerMessage playerJoinedNotification = ServerMessage.notification(joinerName + " joined the game as an observer.");
            connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
        } else {
            // make the message to send to all the others
            ServerMessage playerJoinedNotification = ServerMessage.notification(joinerName + " joined the " + teamJoined + "  team.");
            connections.broadcastToAllExcluding(gameID, authToken, playerJoinedNotification);
        }
    }

    public GameData getGame(String authToken, int gameID) throws IOException, DataAccessException {
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
            return null;
        } else {
            return game;
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
        if (Objects.equals(game.blackUsername(), senderUsername)) {
            senderColor = ChessGame.TeamColor.BLACK;
        } else if (Objects.equals(game.whiteUsername(), senderUsername)) {
            senderColor = ChessGame.TeamColor.WHITE;
            observing = false;
        }

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

        GameData updatedGame = null;

        try {
            // handle the move
            MoveResult result = gameService.handleMove(new MoveRequest(authToken, gameID, move));

            // find which game its from
            updatedGame = result.game();
            ServerMessage gameUpdateMessage = ServerMessage.loadGame(updatedGame);
            connections.broadcastToAll(gameID, gameUpdateMessage);
            ServerMessage chessMoveNotification = ServerMessage.notification(authService.getUserByAuthToken(authToken) + " moved from ("
                    + move.getStartPosition().getRow() + "," + columnToLetter(move.getStartPosition().getColumn()) + ") to ("
                    + move.getEndPosition().getRow() + "," + columnToLetter(move.getEndPosition().getColumn()) + ").");
            connections.broadcastToAllExcluding(gameID, authToken, chessMoveNotification);
        } catch (Exception error) {
            ServerMessage errorMessage = ServerMessage.error("Error: Invalid Move! Wrong turn, or disallowed movement!");
            connections.broadcastToSpecificConnection(authToken, errorMessage);
            return;
        }
        try {
            // check for check and checkmate
            boolean isInCheckMateWhite = updatedGame.game().isInCheckmate(ChessGame.TeamColor.WHITE);
            boolean isInCheckMateBlack = updatedGame.game().isInCheckmate(ChessGame.TeamColor.BLACK);
            boolean isInCheckWhite = updatedGame.game().isInCheck(ChessGame.TeamColor.WHITE);
            boolean isInCheckBlack = updatedGame.game().isInCheck(ChessGame.TeamColor.BLACK);
            System.out.println("HEY");
            if (isInCheckMateWhite) {
                System.out.println("HI");
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.whiteUsername() + " is in checkmate!");
                connections.broadcastToAll(gameID, checkNotification);
            } else if (isInCheckMateBlack) {
                System.out.println("WEE");
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.blackUsername() + " is in checkmate!");
                connections.broadcastToAll(gameID, checkNotification);
            } else if (isInCheckWhite) {
                System.out.println("WOW");
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.whiteUsername() + " is in check!");
                connections.broadcastToAll(gameID, checkNotification);
            } else if (isInCheckBlack) {
                System.out.println("HOWDY");
                ServerMessage checkNotification = ServerMessage.notification(updatedGame.blackUsername() + " is in check!");
                connections.broadcastToAll(gameID, checkNotification);
            }
        } catch (Exception error) {
            ServerMessage errorMessage = ServerMessage.error("Error: Check and Checkmate Checking Problem!" + error.getMessage());
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