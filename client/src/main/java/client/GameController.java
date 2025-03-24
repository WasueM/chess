package client;

import chess.ChessGame;
import model.GameData;

public class GameController {

    GameData game;
    ChessGame.TeamColor playerColor;

    public GameController(GameData game) {
        this.game = game;
    }

    public void setPlayer(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

    public void show() {
        if (playerColor == ChessGame.TeamColor.WHITE) {
            // draw it with white at the bottom
            System.out.println("White player: " + game.whiteUsername());
            System.out.print(game.game().chessBoard);
        } else {
            // draw it with black at the bottom
            System.out.println("White player: " + game.whiteUsername());
            System.out.print(game.game().chessBoard);
        }
    }
}
