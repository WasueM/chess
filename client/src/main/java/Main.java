import chess.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // on launch, show the menus
        runConsoleMenus();
    }

    private static void runConsoleMenus() {
        // print the initial welcome text
        introText();

        // input-receiving loop
        boolean keepLooping = true;
        while (keepLooping) {
            System.out.print(">>> ");

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            keepLooping = handleInput(input);
        }
    }

    private static boolean handleInput(String input) {
        switch (input) {
            case "help":

        }
    }

    private static void introText() {
        System.out.println();
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("\033" + "[0;33mTo get started, type 'help'");
    }

    private static void helpText() {

    }
}