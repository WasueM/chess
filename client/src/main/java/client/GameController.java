package client;

import chess.ChessGame;
import model.GameData;

public class GameController {

    GameData game;

    public GameController(GameData game) {
        this.game = game;
    }

    public show(ChessGame.TeamColor playerColor) {
        if (playerColor == ChessGame.TeamColor.WHITE) {
            // draw it with white at the bottom
            System.out.print(game);
        } else {
            // draw it with black at the bottom
            System.out.print(game);
        }
    }
}
