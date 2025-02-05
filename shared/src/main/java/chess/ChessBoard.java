package chess;
import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    HashMap<ChessPosition, ChessPiece> pieces = new HashMap<>();
    int numPieces = 0;

    public ChessBoard() {

    }

    public ChessBoard(ChessBoard otherBoard) {
       // this.pieces = otherBoard.pieces;  would make a shallow copy only, so commented out

        // deep copy
        HashMap<ChessPosition, ChessPiece> piecesCopy = new HashMap<>();
        for (Map.Entry<ChessPosition, ChessPiece> entry : otherBoard.pieces.entrySet()) {
            piecesCopy.put(entry.getKey().copy(), entry.getValue().copy());
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
        System.out.println(this);

        System.out.println("IT WAS:");
        System.out.println(pieces.get(position));

        System.out.println("PUTTING IN:");
        System.out.println(pieces.get(piece));

        pieces.put(position, piece);
        System.out.println("NOW ITS:");
        System.out.println(pieces.get(position));

        System.out.println(this);

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
        System.out.println("THE PIECE WE FOUND WAS:");
        System.out.println(piece);
        return piece;
    }

    public ChessPiece getKing(ChessGame.TeamColor team) {
        for (Map.Entry<ChessPosition, ChessPiece> entry : pieces.entrySet()) {
            ChessPiece piece = entry.getValue(); // get a piece out of the entry if its possible
            if (piece != null) {
                if (piece.getTeamColor() == team) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        return piece;
                    }
                }
            }
        }
        return null;
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

    // a function that finds out where the king can move for a specific color
    public Collection<ChessPosition> getPlacesKingCouldGo(ChessGame.TeamColor team) {
        // use the pieceMoves on the king, once we get its position and object
        ChessPosition kingPosition = getKingPosition(team);
        ChessPiece king = getKing(team);
        Collection<ChessPosition> placesKingCouldGo = king.piecePositions(this, kingPosition);

        return placesKingCouldGo;
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
        return numPieces == that.numPieces && Objects.equals(pieces, that.pieces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieces, numPieces);
    }

    @Override
    public String toString() {
        // Go through each location on the board, and add then in the string builder prettily
        StringBuilder sb = new StringBuilder();
        for (int i = 8; i > 0; i--) { // row, go from 8 at the top to 1 at the bottom
            for (int j = 1; j <= 8; j++) { // column, go from 1 at the left to 8 at the right
                ChessPiece piece = getPiece(new ChessPosition(i, j));
                if (piece == null) {
                    sb.append("0");
                } else {
                    sb.append(piece);
                }
            }
            sb.append("\n"); // this row is done, go to the next one
        }
        return sb.toString();
    }
}
