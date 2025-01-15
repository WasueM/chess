package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    int rowNumber;
    int columnNumber;

    public ChessPosition(int row, int col) {
        this.rowNumber = row;
        this.columnNumber = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.rowNumber;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.columnNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return rowNumber == that.rowNumber && columnNumber == that.columnNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowNumber, columnNumber);
    }

    @Override
    public String toString() {
        return "(" + this.rowNumber + "," + this.columnNumber + ")";
    }
}
