package chess;

import java.util.ArrayList;
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
        ChessPosition[] possiblePositions = new ChessPosition[32]; // not sure if 32 is the right size but it'll be big enough for sure

        // so I guess we'll do a switch statement for each possible piece that this could be to organize it?
        switch (this.pieceType) {
            case KING:
                addKingMoves(possiblePositions, 0, myPosition, board);
                break;
            case QUEEN:
                addDiagonalMoves(possiblePositions, 0, myPosition, board);
                addStraightMoves(possiblePositions, 32, myPosition, board);
                break;
            case BISHOP:
                addDiagonalMoves(possiblePositions, 0, myPosition, board);
                break;
            case KNIGHT:
                addKnightMoves(possiblePositions, 0, myPosition, board);
                break;
            case ROOK:
                addStraightMoves(possiblePositions, 0, myPosition, board);
                break;
            case PAWN:
                addPawnMoves(possiblePositions, 0, myPosition, board);
                break;
        }

        // count how many non-null there are
        int numNonNull = 0;
        for (int i = 0; i < possiblePositions.length; i++) {
            if (possiblePositions[i] != null) {
                numNonNull++;
            }
        }

        // turn the chess positions (that aren't null) into chess moves
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (int i = 0; i < possiblePositions.length; i++) {
            if (possiblePositions[i] != null) {
                ChessMove move = new ChessMove(myPosition, possiblePositions[i], null);
                possibleMoves.add(move);
            }
        }

        return possibleMoves;
    }

    public void addKnightMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition, ChessBoard board) {
        // add two up and one left
        ChessPosition topLeft = makeNewPositionIfPossible(myPosition, 2, -1, board);
        if (topLeft != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(topLeft);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = topLeft;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = topLeft;
                index++;
            }
        }

        // add two up and one right
        ChessPosition topRight = makeNewPositionIfPossible(myPosition, 2, 1, board);
        if (topRight != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(topRight);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = topRight;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = topRight;
                index++;
            }
        }

        // add two left one up
        ChessPosition leftTop = makeNewPositionIfPossible(myPosition, 1, -2, board);
        if (leftTop != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(leftTop);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = leftTop;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = leftTop;
                index++;
            }
        }

        // add two left one down
        ChessPosition leftBottom = makeNewPositionIfPossible(myPosition, -1, -2, board);
        if (leftBottom != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(leftBottom);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = leftBottom;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = leftBottom;
                index++;
            }
        }

        // add two down and one left
        ChessPosition bottomLeft = makeNewPositionIfPossible(myPosition, -2, -1, board);
        if (bottomLeft != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(bottomLeft);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = bottomLeft;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = bottomLeft;
                index++;
            }
        }

        // add two down and one right
        ChessPosition bottomRight = makeNewPositionIfPossible(myPosition, -2, 1, board);
        if (bottomRight != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(bottomRight);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = bottomRight;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = bottomRight;
                index++;
            }
        }

        // add two right and one down
        ChessPosition rightBottom = makeNewPositionIfPossible(myPosition, -1, 2, board);
        if (rightBottom != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(rightBottom);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = rightBottom;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = rightBottom;
                index++;
            }
        }

        // add two right and one up
        ChessPosition rightUp = makeNewPositionIfPossible(myPosition, 1, 2, board);
        if (rightUp != null) { // so this location is on the board
            ChessPiece pieceInLocation = board.getPiece(rightUp);
            if (pieceInLocation == null) {
                // nothing's there, so we can add this location
                possibleMoves[index] = rightUp;
                index++;
            } else if (pieceInLocation.getTeamColor() != pieceColor) {
                // the piece there is theirs; we should add this location
                possibleMoves[index] = rightUp;
                index++;
            }
        }
    }

    public void addKingMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition, ChessBoard board) {
        // can go any direction straight and diagonal on space, add these
        // calculate and add each to that array
        ChessPosition straightUp = makeNewPositionIfPossible(myPosition, 1, 0, board);
        possibleMoves[index] = straightUp;
        index++;
        ChessPosition straightDown = makeNewPositionIfPossible(myPosition, -1, 0, board);
        possibleMoves[index] = straightDown;
        index++;
        ChessPosition straightLeft = makeNewPositionIfPossible(myPosition, 0, -1, board);
        possibleMoves[index] = straightLeft;
        index++;
        ChessPosition straightRight = makeNewPositionIfPossible(myPosition, 0, 1, board);
        possibleMoves[index] = straightRight;
        index++;
        ChessPosition diagonalUpLeft = makeNewPositionIfPossible(myPosition, 1, -1, board);
        possibleMoves[index] = diagonalUpLeft;
        index++;
        ChessPosition diagonalUpRight = makeNewPositionIfPossible(myPosition, 1, 1, board);
        possibleMoves[index] = diagonalUpRight;
        index++;
        ChessPosition diagonalDownLeft = makeNewPositionIfPossible(myPosition, -1, -1, board);
        possibleMoves[index] = diagonalDownLeft;
        index++;
        ChessPosition diagonalDownRight = makeNewPositionIfPossible(myPosition, -1, 1, board);
        possibleMoves[index] = diagonalDownRight;
        index++;
    }

    public void addPawnMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition, ChessBoard board) {
        // check if the space right in front is empty, if so, it can go there
        ChessPosition straightForward = makeNewPositionIfPossible(myPosition, 1, 0, board);
        ChessPiece pieceInStraightForward = board.getPiece(straightForward);
        if (pieceInStraightForward == null) {
            possibleMoves[index] = straightForward;
            index++;
        }

        // check if the space to the front left has an enemy, if so, it can go there too
        ChessPosition upLeft = makeNewPositionIfPossible(myPosition, 1, -1, board);
        ChessPiece pieceInUpLeft = board.getPiece(upLeft);
        if (pieceInUpLeft == null) {
            possibleMoves[index] = upLeft;
            index++;
        }

        // same for front right
        ChessPosition upRight = makeNewPositionIfPossible(myPosition, 1, 1, board);
        ChessPiece pieceInUpRight = board.getPiece(upRight);
        if (pieceInUpRight == null) {
            possibleMoves[index] = upRight;
        }
    }

    public void addDiagonalMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition, ChessBoard board) {
        // this function adds all the diagonal movements as possible moves, for bishop and queen
        boolean continueLoop = true;
        int i = 0;
        while (continueLoop) {
            i++;
            ChessPosition upRight = makeNewPositionIfPossible(myPosition, i, i, board);
            if (upRight == null) {
                // this location isn't on the board, we shouldn't add it and stop looping
                continueLoop = false;
            } else {
                ChessPiece pieceInLocation = board.getPiece(upRight);
                if (pieceInLocation == null) {
                    // nothing's there, keep going this direction further
                    possibleMoves[index] = upRight;
                    index++;
                } else {
                    if (pieceInLocation.getTeamColor() == pieceColor) {
                        // the piece there is ours; we shouldn't add this location, and we should stop looping
                        continueLoop = false;
                    } else {
                        // the pice there is theirs; we should add this location, but we should stop looping
                        possibleMoves[index] = upRight;
                        index++;
                        continueLoop = false;
                    }
                }
            }

            continueLoop = true;
            i = 0;
            while (continueLoop) {
                i--;
                ChessPosition bottomLeft = makeNewPositionIfPossible(myPosition, i, i, board);
                if (bottomLeft == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(bottomLeft);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = bottomLeft;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = bottomLeft;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }

            continueLoop = true;
            i = 0;
            while (continueLoop) {
                i++;
                ChessPosition bottomRight = makeNewPositionIfPossible(myPosition, -i, i, board);
                if (bottomRight == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(bottomRight);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = bottomRight;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = bottomRight;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }

            continueLoop = true;
            i = 0;
            while (continueLoop) {
                i++;
                ChessPosition upLeft = makeNewPositionIfPossible(myPosition, i, -i, board);
                if (upLeft == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(upLeft);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = upLeft;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = upLeft;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }
        }
    }

    public void addStraightMoves(ChessPosition[] possibleMoves, int index, ChessPosition myPosition, ChessBoard board) {
        // the same thing but for the rook and queen's straight moves
        for (int i = 1; i < 8; i++) {
            boolean continueLoop = true;
            int i = 0;
            while (continueLoop) {
                i++;
                ChessPosition up = makeNewPositionIfPossible(myPosition, i, 0, board);
                if (up == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(up);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = up;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = up;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }

            continueLoop = true;
            i = 0;
            while (continueLoop) {
                i++;
                ChessPosition up = makeNewPositionIfPossible(myPosition, -i, 0, board);
                if (up == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(up);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = up;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = up;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }

            continueLoop = true;
            i = 0;
            while (continueLoop) {
                i++;
                ChessPosition up = makeNewPositionIfPossible(myPosition, 0, -i, board);
                if (up == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(up);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = up;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = up;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }

            continueLoop = true;
            i = 0;
            while (continueLoop) {
                i++;
                ChessPosition up = makeNewPositionIfPossible(myPosition, 0, -i, board);
                if (up == null) {
                    // this location isn't on the board, we shouldn't add it and stop looping
                    continueLoop = false;
                } else {
                    ChessPiece pieceInLocation = board.getPiece(up);
                    if (pieceInLocation == null) {
                        // nothing's there, keep going this direction further
                        possibleMoves[index] = up;
                        index++;
                    } else {
                        if (pieceInLocation.getTeamColor() == pieceColor) {
                            // the piece there is ours; we shouldn't add this location, and we should stop looping
                            continueLoop = false;
                        } else {
                            // the piece there is theirs; we should add this location, but we should stop looping
                            possibleMoves[index] = up;
                            index++;
                            continueLoop = false;
                        }
                    }
                }
            }
        }
    }
}
