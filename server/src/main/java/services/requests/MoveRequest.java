package services.requests;

import chess.ChessMove;

public record MoveRequest(
        String authToken,
        int gameID,
        ChessMove move
) {}
