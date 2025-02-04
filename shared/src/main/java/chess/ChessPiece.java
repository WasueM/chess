package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    ChessGame.TeamColor pieceColor;
    PieceType pieceType;
    int nextIndexToAdd;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.pieceType = type;
        this.nextIndexToAdd = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, pieceType);
    }

    @Override
    public String toString() {
        switch (this.pieceType) {
            case KING:
                return "K";
            case QUEEN:
                return "Q";
            case BISHOP:
                return "B";
            case KNIGHT:
                return "K";
            case ROOK:
                return "R";
            case PAWN:
                return "P";
        }
        return "FAILED";
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

        ChessPosition newPosition = new ChessPosition(newPositionRow, newPositionColumn);

        // check if its actually on the board
        if (newPositionRow < 1 || newPositionRow > 8 || newPositionColumn < 1 || newPositionColumn > 8) {
            return null;
        }

        // check if there's a same-team piece in there
        if (isSameTeamPieceThere(newPosition, board)) {
            return null;
        }

        // look's like it's a possible position!
        return newPosition;
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
        ChessPosition[] possiblePositions = new ChessPosition[64]; // not sure if 32 is the right size but it'll be big enough for sure

        // so I guess we'll do a switch statement for each possible piece that this could be to organize it?
        switch (this.pieceType) {
            case KING:
                addKingMoves(possiblePositions, myPosition, board);
                break;
            case QUEEN:
                addDiagonalMoves(possiblePositions, myPosition, board);
                addStraightMoves(possiblePositions, myPosition, board);
                break;
            case BISHOP:
                addDiagonalMoves(possiblePositions, myPosition, board);
                break;
            case KNIGHT:
                addKnightMoves(possiblePositions, myPosition, board);
                break;
            case ROOK:
                addStraightMoves(possiblePositions, myPosition, board);
                break;
            case PAWN:
                addPawnMoves(possiblePositions, myPosition, board);
                break;
        }

        // turn the chess positions (that aren't null) into chess moves
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (int i = 0; i < possiblePositions.length; i++) {
            if (possiblePositions[i] != null) {
                // if its a pawn moving ot the end, it will have a promotion, otherwise no
                if (pieceType == PieceType.PAWN && ((possiblePositions[i].getRow() == 1) && (pieceColor == ChessGame.TeamColor.BLACK) || (possiblePositions[i].getRow() == 8) && (pieceColor == ChessGame.TeamColor.WHITE))) {
                    ChessMove promoteBishopMove = new ChessMove(myPosition, possiblePositions[i], PieceType.BISHOP);
                    possibleMoves.add(promoteBishopMove);
                    ChessMove promoteQueenMove = new ChessMove(myPosition, possiblePositions[i], PieceType.QUEEN);
                    possibleMoves.add(promoteQueenMove);
                    ChessMove promoteKnightMove = new ChessMove(myPosition, possiblePositions[i], PieceType.KNIGHT);
                    possibleMoves.add(promoteKnightMove);
                    ChessMove promoteRookMove = new ChessMove(myPosition, possiblePositions[i], PieceType.ROOK);
                    possibleMoves.add(promoteRookMove);
                } else {
                    ChessMove move = new ChessMove(myPosition, possiblePositions[i], null);
                    possibleMoves.add(move);
                }
            }
        }

        return possibleMoves;
    }

    // the same thing as pieceMoves but it returns just the positions instead of the moves
    public Collection<ChessPosition> piecePositions(ChessBoard board, ChessPosition myPosition) {
        // we will store possible moves here
        ChessPosition[] possiblePositions = new ChessPosition[64]; // not sure if 32 is the right size but it'll be big enough for sure

        // so I guess we'll do a switch statement for each possible piece that this could be to organize it?
        switch (this.pieceType) {
            case KING:
                addKingMoves(possiblePositions, myPosition, board);
                break;
            case QUEEN:
                addDiagonalMoves(possiblePositions, myPosition, board);
                addStraightMoves(possiblePositions, myPosition, board);
                break;
            case BISHOP:
                addDiagonalMoves(possiblePositions, myPosition, board);
                break;
            case KNIGHT:
                addKnightMoves(possiblePositions, myPosition, board);
                break;
            case ROOK:
                addStraightMoves(possiblePositions, myPosition, board);
                break;
            case PAWN:
                addPawnMoves(possiblePositions, myPosition, board);
                break;
        }

        Collection<ChessPosition> positions = new ArrayList<>();
        for (int i = 0; i < possiblePositions.length; i++) {
            if (possiblePositions[i] != null) {
                positions.add(possiblePositions[i]);
            }
        }

        return positions;
    }

    public void addSinglePositionIfPossible(ChessPosition[] possibleMoves, ChessBoard board, ChessPosition myPosition, int upDown, int leftRight) {
        ChessPosition newPosition = makeNewPositionIfPossible(myPosition, upDown, leftRight, board);
        if (newPosition != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(newPosition);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[this.nextIndexToAdd] = newPosition;
                this.nextIndexToAdd++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[this.nextIndexToAdd] = newPosition;
                this.nextIndexToAdd++;
            }
        }
    }

    public void addPositionsInDirectionIfPossible(ChessPosition[] possibleMoves, ChessBoard board, ChessPosition myPosition, int upDown, int leftRight) {
        boolean continueLoop = true;
        int i = 0;
        while (continueLoop) {
            i++;
            ChessPosition newPosition = makeNewPositionIfPossible(myPosition, i * upDown, i * leftRight, board);
            if (newPosition == null) {
                // this location isn't on the board, we shouldn't add it and stop looping
                continueLoop = false;
            } else {
                ChessPiece pieceInLocation = board.getPiece(newPosition);
                if (pieceInLocation == null) {
                    // nothing's there, keep going this direction further
                    possibleMoves[this.nextIndexToAdd] = newPosition;
                    this.nextIndexToAdd++;
                } else {
                    if (pieceInLocation.getTeamColor() == pieceColor) {
                        // the piece there is ours; we shouldn't add this location, and we should stop looping
                        continueLoop = false;
                    } else {
                        // the piece there is theirs; we should add this location, but we should stop looping
                        possibleMoves[this.nextIndexToAdd] = newPosition;
                        this.nextIndexToAdd++;
                        continueLoop = false;
                    }
                }
            }
        }
    }

    public void addKnightMoves(ChessPosition[] possibleMoves, ChessPosition myPosition, ChessBoard board) {
        // add two up and one left
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 2, -1);

        // add two up and one right
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 2, 1);

        // add two left one up
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 1, -2);

        // add two left one down
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -1, -2);

        // add two down and one left
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -2, -1);

        // add two down and one right
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -2, 1);

        // add two right and one down
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -1, 2);

        // add two right and one up
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 1, 2);
    }

    public void addKingMoves(ChessPosition[] possibleMoves, ChessPosition myPosition, ChessBoard board) {
        // can go any direction straight and diagonal on space, add these
        // calculate and add each to that array
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 1, 0);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -1, 0);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 0, -1);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 0, 1);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 1, -1);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, 1, 1);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -1, -1);
        addSinglePositionIfPossible(possibleMoves, board, myPosition, -1, 1);
    }

    public void addPawnMoves(ChessPosition[] possibleMoves, ChessPosition myPosition, ChessBoard board) {
        // based on the color, flip the directions
        int colorMultiplier = 1;
        if (pieceColor == ChessGame.TeamColor.BLACK) {
            colorMultiplier = -1;
        }

        // check if the space right in front is empty, if so, it can go there
        ChessPosition straightForward = makeNewPositionIfPossible(myPosition, 1 * colorMultiplier, 0, board);
        if (straightForward != null) {
            ChessPiece pieceInStraightForward = board.getPiece(straightForward);
            if (pieceInStraightForward == null) {
                possibleMoves[this.nextIndexToAdd] = straightForward;
                this.nextIndexToAdd++;

                // if its on the second row (in the case of WHITE) or the seventh row (in the case of BLACK), it check about moving two in front as well
                if ((pieceColor == ChessGame.TeamColor.WHITE && myPosition.getRow() == 2) || (pieceColor == ChessGame.TeamColor.BLACK && myPosition.getRow() == 7)) {
                    ChessPosition twoForward = makeNewPositionIfPossible(myPosition, 2 * colorMultiplier, 0, board);
                    ChessPiece pieceInTwoForward = board.getPiece(twoForward);
                    if (pieceInTwoForward == null) {

                        possibleMoves[this.nextIndexToAdd] = twoForward;
                        this.nextIndexToAdd++;
                    }
                }
            }
        }

        // check if the space to the front left has an enemy, if so, it can go there too
        ChessPosition upLeft = makeNewPositionIfPossible(myPosition, 1 * colorMultiplier, -1, board);
        ChessPiece pieceInUpLeft = board.getPiece(upLeft);
        if (pieceInUpLeft != null) {
            if (pieceInUpLeft.getTeamColor() != pieceColor) {
                // enemy piece
                possibleMoves[this.nextIndexToAdd] = upLeft;
                this.nextIndexToAdd++;
            }
        }

        // same for front right
        ChessPosition upRight = makeNewPositionIfPossible(myPosition, 1 * colorMultiplier, 1, board);
        ChessPiece pieceInUpRight = board.getPiece(upRight);
        if (pieceInUpRight != null) {
            if (pieceInUpRight.getTeamColor() != pieceColor) {
                // enemy piece
                possibleMoves[this.nextIndexToAdd] = upRight;
            }
        }
    }

    public void addDiagonalMoves(ChessPosition[] possibleMoves, ChessPosition myPosition, ChessBoard board) {
        // this function adds all the diagonal movements as possible moves, for bishop and queen
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, 1, 1);
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, -1, -1);
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, -1, 1);
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, 1, -1);
    }

    public void addStraightMoves(ChessPosition[] possibleMoves, ChessPosition myPosition, ChessBoard board) {
        // the same thing but for the rook and queen's straight moves
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, 1, 0);
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, -1, 0);
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, 0, 1);
        addPositionsInDirectionIfPossible(possibleMoves, board, myPosition, 0, -1);
    }
}
