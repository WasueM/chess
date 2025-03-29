package services.requests;

import chess.ChessMove;

public record MoveRequest(
        int gameID,
        ChessMove move
) {}
