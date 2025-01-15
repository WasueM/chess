package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    ChessGame.TeamColor pieceColor;
    PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    // a function which takes in a chess position and a modification to it, and returns the new place on the board or null if its off the board
    public ChessPosition makeNewPositionIfPossible(ChessPosition position, int upDown, int leftRight) {
        int currentPositionRow = position.getRow();
        int currentPositionColumn = position.getColumn();

        int newPositionRow = currentPositionRow + upDown;
        int newPositionColumn = currentPositionColumn + leftRight;

        if (newPositionRow < 1 || newPositionRow > 8 || newPositionColumn < 1 || newPositionColumn > 8) {
            return null;
        } else {
            return new ChessPosition(newPositionRow, newPositionColumn);
        }
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // we will store possible moves here
        ChessPosition[] possibleMoves = new ChessPosition[32]; // not sure if 32 is the right size but it'll be big enough for sure

        // so I guess we'll do a switch statement for each possible piece that this could be to organize it?
        switch (this.pieceType) {
            case KING:
                // can go any direction straight and diagonal on space, add these
                // calculate and add each to that array
                ChessPosition straightUp = makeNewPositionIfPossible(myPosition, 1, 0);
                possibleMoves[0] = straightUp;
                ChessPosition straightDown = makeNewPositionIfPossible(myPosition, -1, 0);
                possibleMoves[1] = straightDown;
                ChessPosition straightLeft = makeNewPositionIfPossible(myPosition, 0, -1);
                possibleMoves[2] = straightLeft;
                ChessPosition straightRight = makeNewPositionIfPossible(myPosition, 0, 1);
                possibleMoves[3] = straightRight;
                ChessPosition diagonalUpLeft = makeNewPositionIfPossible(myPosition, 1, -1);
                possibleMoves[4] = diagonalUpLeft;
                ChessPosition diagonalUpRight = makeNewPositionIfPossible(myPosition, 1, 1);
                possibleMoves[5] = diagonalUpRight;
                ChessPosition diagonalDownLeft = makeNewPositionIfPossible(myPosition, -1, -1);
                possibleMoves[6] = diagonalDownLeft;
                ChessPosition diagonalDownRight = makeNewPositionIfPossible(myPosition, -1, 1);
                possibleMoves[7] = diagonalDownRight;
                break;
            case QUEEN:

                break;
            case BISHOP:

                break;
            case KNIGHT:

                break;
            case ROOK:

                break;
            case PAWN:

                break;
        }

        // loop through them and remove them if they are null or occupied by a piece of the same color
        for (ChessPosition possibleMove : possibleMoves) {
            ChessPiece possibleMovePiece = board.getPiece(possibleMove);
            if (possibleMovePiece.getTeamColor() != this.pieceColor) {
                possibleMove = null; // set to null so we know we can't move there
            }
        }
    }
}
