package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;

import java.util.Collection;

public class GameController {

    GameData game;
    ChessGame.TeamColor playerColor;

    public GameController(GameData game) throws Exception {
        this.game = game;
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
        System.out.print("Game: " + this.game.gameName());
    }

    public void printUsernames() {
        System.out.print("White: " + this.game.whiteUsername() + " | Black: " + this.game.blackUsername());
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

    public void makeMove(int fromRow, int fromColumn, int toRow, int toColumn) {

    }

     public void resign() {

     }
}
