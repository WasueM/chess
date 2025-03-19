package chess;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChessBoardJSONAdapter extends TypeAdapter<ChessBoard> {
    @Override
    public void write(JsonWriter jsonWriter, ChessBoard chessboard) throws IOException {

        // this is the beginning of the whole object:
        jsonWriter.beginObject();

        // write each piece in a specialized way
        jsonWriter.name("pieces");
        jsonWriter.beginObject();


        for (Map.Entry<ChessPosition, ChessPiece> entry : chessboard.pieces.entrySet()) {
            ChessPosition pos = entry.getKey();
            ChessPiece piece = entry.getValue();

            // Turn the key into a string instead of a ChessPosition so that it can be understood
            String posKey = "(" + pos.getRow() + "," + pos.getColumn() + ")";

            jsonWriter.name(posKey);
            new Gson().toJson(piece, ChessPiece.class, jsonWriter);
        }

        // end the pieces object
        jsonWriter.endObject();

        // Write 'numPieces' field separately, should be easy
        jsonWriter.name("numPieces");
        jsonWriter.value(chessboard.numPieces);

        jsonWriter.endObject();
    }

    @Override
    public ChessBoard read(JsonReader jsonReader) throws IOException {
        ChessBoard chessBoard = new ChessBoard();

        // Start reading
        jsonReader.beginObject();

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();

            // Two possibilities, it's either the numPieces int (easy) or the pieces (hard)
            switch (name) {
                case "pieces":
                    chessBoard.pieces = readPieces(jsonReader);
                    break;

                case "numPieces":
                    chessBoard.numPieces = jsonReader.nextInt();
                    break;

                default:
                    // something different and strange, so skip?
                    jsonReader.skipValue();
            }
        }

        jsonReader.endObject();

        return chessBoard;
    }

    private HashMap<ChessPosition, ChessPiece> readPieces(JsonReader jsonReader) throws IOException {
        HashMap<ChessPosition, ChessPiece> decodedMap = new HashMap<>();

        // start the object
        jsonReader.beginObject();

        // Go through the entries and turn them from strings to Chess Positions
        while (jsonReader.hasNext()) {
            String chessPositionString = jsonReader.nextName();

            // cut the parenthesis out
            chessPositionString = chessPositionString.replace("(", "").replace(")", "");

            // get the row and column from each side of the comma
            String[] parts = chessPositionString.split(",");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            // make the position!!
            ChessPosition position = new ChessPosition(row, col);

            // We can get the piece part without anything fancy
            ChessPiece piece = new Gson().fromJson(jsonReader, ChessPiece.class);

            decodedMap.put(position, piece);
        }

        jsonReader.endObject();

        return decodedMap;
    }
}
