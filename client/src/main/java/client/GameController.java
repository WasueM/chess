package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;

import java.util.Collection;

public class GameController {

    GameData game;
    ChessGame.TeamColor playerColor;
    ServerFacade serverFacade;

    public GameController(GameData game, ServerFacade serverFacade) throws Exception {
        this.game = game;
        this.serverFacade = serverFacade;

        this.createWebsocketConnection();
    }

    public void setPlayer(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

    public void show() {
        // "clear" the viewing space a bit
        this.clear();

        // print the usernames at the top
        this.printGameInfo();

        // drawing the board
        this.drawBoard();
    }

    public void clear() {
        System.out.print("\n\n\n\n\n\n\n\n\n\n"); // 10 new lines to push things out of view from earlier
    }

    public void printGameInfo() {
        System.out.println("---------------");
        this.printGameName();
        this.printUsernames();
        System.out.println("---------------");
    }

    public void printGameName() {
        System.out.print("Game: " + this.game.gameName() + "\n");
    }

    public void printUsernames() {
        System.out.print("White: " + this.game.whiteUsername() + " | Black: " + this.game.blackUsername() + "\n");
    }

    public void drawBoard() {
        if (playerColor == ChessGame.TeamColor.WHITE) {
            // draw it with white at the bottom
            System.out.print(game.game().chessBoard);
        } else {
            // draw it with black at the bottom
            System.out.print(game.game().chessBoard.toStringInverted());
        }
    }

    public void highlightLegalMoves(int row, int column) {
        // first, we need the valid moves starting at that location
        ChessPosition startingPosition = new ChessPosition(row, column);
        Collection<ChessMove> validMoves = this.game.game().validMoves(startingPosition);

        if (playerColor == ChessGame.TeamColor.WHITE) {
            // draw it with white at the bottom
            System.out.print(game.game().chessBoard.toStringHighlighted(startingPosition, validMoves));
        } else {
            // draw it with black at the bottom
            System.out.print(game.game().chessBoard.toStringInvertedHighlighted(startingPosition, validMoves));
        }
    }

    // this is something it sends
    public void makeMove(int fromRow, int fromColumn, int toRow, int toColumn) throws Exception {
        ChessMove move = new ChessMove(
                new ChessPosition(fromRow, fromColumn),
                new ChessPosition(toRow, toColumn),
                null
        );
        serverFacade.sendMakeMove(move, game.gameID());
    }

    // this is something it sends
     public void resign() throws Exception {
        System.out.println("YOU LOSE!");
        serverFacade.sendResign(game.gameID());
     }

     // this is something we receive
     public void showNotication(String notificationMessage) {
        System.out.println("NOTIFICATION: " + notificationMessage);
     }

     // this is something we recieve
     public void showError(String errorMessage) {
        System.out.println("ERROR: " + errorMessage);
     }

     public void createWebsocketConnection() throws Exception {
         serverFacade.sendConnect(this.game.gameID());
     }

     public void endWebSocketConnection() throws Exception {
        serverFacade.sendLeave(this.game.gameID());
     }
}
