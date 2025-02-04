package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    ChessGame.TeamColor whoseTurnItIs;
    ChessBoard chessBoard;

    public ChessGame() {
        this.whoseTurnItIs = TeamColor.WHITE;
        this.chessBoard = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.whoseTurnItIs;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.whoseTurnItIs = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece pieceInLocation = chessBoard.getPiece(startPosition);
        ChessGame.TeamColor pieceColor = pieceInLocation.getTeamColor();
        if (pieceInLocation != null) { // there's a piece here
            Collection<ChessMove> moves = pieceInLocation.pieceMoves(chessBoard, startPosition);
            for (ChessMove move : moves) {
                if (isMoveValid(move, pieceColor) == false) {
                    moves.remove(move);
                }
            }
            return moves;
        }
        return null;
    }

    public boolean isMoveValid(ChessMove move, ChessGame.TeamColor color) {
        // first, make sure there's a piece there
        ChessPosition movingFrom = move.getStartPosition();
        ChessPiece piece = chessBoard.getPiece(movingFrom);
        if (piece == null) {
            return false;
        }

        // make a copy of the chessboard, do the move, then see if that would've put us in check
        ChessBoard boardCopy = chessBoard.copy();
        makeMoveWithNoChecks(move, boardCopy);

        // now that we've made the move, are we in check or not?
        boolean wouldThisPutThemInCheck = isInCheck(color, boardCopy);
        if (wouldThisPutThemInCheck == true) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (isMoveValid(move, this.getTeamTurn())) {
            ChessPiece pieceToMove = chessBoard.getPiece(move.getStartPosition());
            chessBoard.addPiece(move.getStartPosition(), null); // remove piece from the old location
            chessBoard.addPiece(move.getEndPosition(), pieceToMove); // add the piece to the new location
        } else {
            throw new InvalidMoveException("INVALID MOVE");
        }
    }

    public void makeMoveWithNoChecks(ChessMove move, ChessBoard board) {
        ChessPiece pieceToMove = board.getPiece(move.getStartPosition());
        board.addPiece(move.getStartPosition(), null); // remove piece from the old location
        board.addPiece(move.getEndPosition(), pieceToMove); // add the piece to the new location
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingLocation = chessBoard.getKingPosition(teamColor);

        // get other team color
        TeamColor enemyTeamColor = TeamColor.WHITE;
        if (teamColor == TeamColor.WHITE) {
            enemyTeamColor = TeamColor.BLACK;
        }

        // loop through every piece on the enemy team, and see if they include this position
        Set<ChessPosition> placesTeamCouldGo = chessBoard.getPlacesTeamCouldGo(enemyTeamColor);
        if (placesTeamCouldGo.contains(kingLocation)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition kingLocation = chessBoard.getKingPosition(teamColor);

        // get other team color
        TeamColor enemyTeamColor = TeamColor.WHITE;
        if (teamColor == TeamColor.WHITE) {
            enemyTeamColor = TeamColor.BLACK;
        }

        // loop through every piece on the enemy team, and see if they include this position
        Set<ChessPosition> placesTeamCouldGo = board.getPlacesTeamCouldGo(enemyTeamColor);
        if (placesTeamCouldGo.contains(kingLocation)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // first, are we in check at all?
        if (this.isInCheck(teamColor)) {
            // get other team color
            TeamColor enemyTeamColor = TeamColor.WHITE;
            if (teamColor == TeamColor.WHITE) {
                enemyTeamColor = TeamColor.BLACK;
            }

            Collection<ChessPosition> placesTheKingCouldGo = chessBoard.getPlacesKingCouldGo(teamColor);
            Set<ChessPosition> placesTeamCouldGo = chessBoard.getPlacesTeamCouldGo(enemyTeamColor);

            boolean foundAnEscape = false;
            for (ChessPosition place : placesTheKingCouldGo) {
                if (placesTeamCouldGo.contains(place) == false) {
                    foundAnEscape = true;
                }
            }

            if (foundAnEscape == false) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // the same as checking if we're in checkmate, except isInCheck should be false
        if (this.isInCheck(teamColor) == false) {
            // get other team color
            TeamColor enemyTeamColor = TeamColor.WHITE;
            if (teamColor == TeamColor.WHITE) {
                enemyTeamColor = TeamColor.BLACK;
            }

            Collection<ChessPosition> placesTheKingCouldGo = chessBoard.getPlacesKingCouldGo(teamColor);
            Set<ChessPosition> placesTeamCouldGo = chessBoard.getPlacesTeamCouldGo(enemyTeamColor);

            boolean foundAnEscape = false;
            for (ChessPosition place : placesTheKingCouldGo) {
                if (placesTeamCouldGo.contains(place) == false) {
                    foundAnEscape = true;
                }
            }

            if (foundAnEscape == false) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.chessBoard;
    }
}
