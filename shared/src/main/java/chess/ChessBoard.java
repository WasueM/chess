package chess;
import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    public HashMap<ChessPosition, ChessPiece> pieces = new HashMap<>();
    public int numPieces = 0;

    public ChessBoard() {

    }

    public ChessBoard(ChessBoard otherBoard) {
       // this.pieces = otherBoard.pieces;  would make a shallow copy only, so commented out

        // deep copy
        HashMap<ChessPosition, ChessPiece> piecesCopy = new HashMap<>();
        for (Map.Entry<ChessPosition, ChessPiece> entry : otherBoard.pieces.entrySet()) {
            if ((entry.getKey() != null) && (entry.getValue() != null)) {
                piecesCopy.put(entry.getKey().copy(), entry.getValue().copy());
            }
        }
        this.pieces = piecesCopy;
        this.numPieces = otherBoard.numPieces;
    }

    public ChessBoard copy() {
        return new ChessBoard(this);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        pieces.put(position, piece);
        if (piece != null) {
            numPieces++;
        }
    }

    public void addPiece(ChessPosition position, ChessPiece piece, boolean shouldPrint) {
        pieces.put(position, piece);
        if (piece != null) {
            numPieces++;
        }
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return pieces.get(position);
    }

    public ChessPiece getPiece(ChessPosition position, boolean shouldPrint) {
        ChessPiece piece = pieces.get(position);
        return piece;
    }

    // a function to get a team's king's location
    public ChessPosition getKingPosition(ChessGame.TeamColor team) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieces.entrySet()) {
            ChessPiece piece = entry.getValue(); // get a piece out of the entry if its possible
            if (piece != null) {
                if (piece.getTeamColor() == team) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    // a function that loops through all of the pieces from a team, and gets a set of all the places they could move
    public Set<ChessPosition> getPlacesTeamCouldGo(ChessGame.TeamColor team) {
        Set<ChessPosition> placesTheTeamCouldGo = new HashSet<ChessPosition>();
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieces.entrySet()) {
            ChessPiece piece = entry.getValue();
            ChessPosition position = entry.getKey();

            if (piece != null) {
                // is it from the right team?
                if (piece.getTeamColor() == team) {
                    // where can this piece move?
                    Collection<ChessMove> possibleMoves = piece.pieceMoves(this, position);

                    // just get the positions out of the move
                    for (ChessMove possibleMove : possibleMoves) {
                        ChessPosition positionToGoTo = possibleMove.getEndPosition();
                        placesTheTeamCouldGo.add(positionToGoTo);
                    }
                }
            }
        }
        return placesTheTeamCouldGo;
    }

    // Get every move possible for a team
    public Collection<ChessMove> getMovesTeamCouldDo(ChessGame.TeamColor team) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieces.entrySet()) {
            ChessPiece piece = entry.getValue();
            ChessPosition position = entry.getKey();

            if (piece != null) {
                // is it from the right team?
                if (piece.getTeamColor() == team) {
                    // where can this piece move?
                    possibleMoves.addAll(piece.pieceMoves(this, position));
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // I'm going to have bottom left be 1,1, bottom right be 1,8, top left be 1,8, and top right be 8,8
        // First, put down the rows of pawns since those are easiest
        for (int i = 1; i <= 8; i++) {
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }
        for (int i = 1; i <= 8; i++) {
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }

        // Now, lets do the bottom row, which is white's special pieces, from left to right
        addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        // Now I'm gonna add the top row which is black's special pieces
        addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));

        // set the right number of pieces
        this.numPieces = 32;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;

        // okay, I have an idea. It says the strings are identical, but "sameBoard" comes
        // out to false. That means something in the toString isnt idential. I'll find it
        String thisString = this.toString();
        String thatString = that.toString();

        char[] thisCharacterArray = thisString.toCharArray();
        char[] thatCharacterArray = thatString.toCharArray();

        // compare each char one by one
        boolean difference = false;
        for (int i = 0; i < thisCharacterArray.length; i++) {
            // uncomment the below line to see each char compared, one by one
            if (thisCharacterArray[i] != thatCharacterArray[i]) {
                // there's a difference in the chars
                difference = true;
            }
        }

        if (difference) {
            // different boards
            return false;
        } else {
            // same board!
            return true;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieces, numPieces);
    }

    @Override
    public String toString() {
        String finalString = "";

        // go through each row and column and add them
        for (int i = 8; i > 0; i--) { // row, from 8 to 1
            for (int j = 1; j < 9; j++) { // column, from 1 to 8
                ChessPosition positionIWantToRetrieve = new ChessPosition(i, j);
                ChessPiece pieceAtPosition = this.getPiece(positionIWantToRetrieve);
                if (pieceAtPosition != null) {
                    finalString = finalString + pieceAtPosition.toString();
                } else {
                    finalString = finalString + " \u2003 ";
                }

            }
            // new line after each row
            finalString = finalString + "\n";
        }

        return finalString;
    }
}
