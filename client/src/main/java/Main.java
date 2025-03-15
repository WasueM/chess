import chess.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        while (true) {
            System.out.printf("Type your numbers%n>>> ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            var numbers = line.split(" ");

            int result = 0;
            for (var number : numbers) {
                result += Integer.parseInt(number);
            }
            var equation = String.join(" + ", numbers);
            System.out.printf("%s = %d%n", equation, result);
        }
    }
}