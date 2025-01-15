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
    public ChessPosition makeNewPositionIfPossible(ChessPosition position, int upDown, int leftRight, ChessBoard board) {
        int currentPositionRow = position.getRow();
        int currentPositionColumn = position.getColumn();

        int newPositionRow = currentPositionRow + upDown;
        int newPositionColumn = currentPositionColumn + leftRight;

        // check if its actually on the board
        if (newPositionRow < 1 || newPositionRow > 8 || newPositionColumn < 1 || newPositionColumn > 8) {
            return null;
        }

        // check if there's a same-team piece in there
        if (isSameTeamPieceThere(position, board)) {
            return null;
        }

        // look's like it's a possible position!
        return new ChessPosition(newPositionRow, newPositionColumn);
    }

    public boolean isSameTeamPieceThere(ChessPosition position, ChessBoard board) {
        ChessPiece pieceInPlace = board.getPiece(position);
        if (pieceInPlace == null) {
            return false;
        } else {
            if (pieceInPlace.getTeamColor() == this.getTeamColor()) {
                return true;
            } else {
                return false;
            }
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
                ChessPosition straightUp = makeNewPositionIfPossible(myPosition, 1, 0, board);
                possibleMoves[0] = straightUp;
                ChessPosition straightDown = makeNewPositionIfPossible(myPosition, -1, 0, board);
                possibleMoves[1] = straightDown;
                ChessPosition straightLeft = makeNewPositionIfPossible(myPosition, 0, -1, board);
                possibleMoves[2] = straightLeft;
                ChessPosition straightRight = makeNewPositionIfPossible(myPosition, 0, 1, board);
                possibleMoves[3] = straightRight;
                ChessPosition diagonalUpLeft = makeNewPositionIfPossible(myPosition, 1, -1, board);
                possibleMoves[4] = diagonalUpLeft;
                ChessPosition diagonalUpRight = makeNewPositionIfPossible(myPosition, 1, 1, board);
                possibleMoves[5] = diagonalUpRight;
                ChessPosition diagonalDownLeft = makeNewPositionIfPossible(myPosition, -1, -1, board);
                possibleMoves[6] = diagonalDownLeft;
                ChessPosition diagonalDownRight = makeNewPositionIfPossible(myPosition, -1, 1, board);
                possibleMoves[7] = diagonalDownRight;
                break;
            case QUEEN:
                addDiagonalMoves(possibleMoves, 0, myPosition);
                addStraightMoves(possibleMoves, 32, myPosition);
                break;
            case BISHOP:
                addDiagonalMoves(possibleMoves, 0, myPosition);
                break;
            case KNIGHT:

                break;
            case ROOK:
                addStraightMoves(possibleMoves, 0, myPosition);
                break;
            case PAWN:
                ChessPosition diagonalUpRight = makeNewPositionIfPossible(myPosition, 1, 1);


                break;
        }
    }

    public void addDiagonalMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition) {
        // this function adds all the diagonal movements as possible moves, for bishop and queen
        for (int i = 1; i < 8; i++) {
            ChessPosition upRight = makeNewPositionIfPossible(myPosition, i, i, board);
            possibleMoves[index] = upRight;
            index++;
            ChessPosition upLeft = makeNewPositionIfPossible(myPosition, i, -i, board);
            possibleMoves[index] = upLeft;
            index++;
            ChessPosition downRight = makeNewPositionIfPossible(myPosition, -i, i, board);
            possibleMoves[index] = downRight;
            index++;
            ChessPosition downLeft = makeNewPositionIfPossible(myPosition, -i, -i, board);
            possibleMoves[index] = downLeft;
            index++;
        }
    }

    public void addStraightMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition) {
        // the same thing but for the rook and queen's straight moves
        for (int i = 1; i < 8; i++) {
            ChessPosition up = makeNewPositionIfPossible(myPosition, i, 0);
            possibleMoves[index] = up;
            index++;
            ChessPosition down = makeNewPositionIfPossible(myPosition, -i, 0);
            possibleMoves[index] = down;
            index++;
            ChessPosition left = makeNewPositionIfPossible(myPosition, 0, i);
            possibleMoves[index] = left;
            index++;
            ChessPosition right = makeNewPositionIfPossible(myPosition, 0, -i);
            possibleMoves[index] = right;
            index++;
        }
    }
}
