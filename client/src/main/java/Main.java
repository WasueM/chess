import chess.*;
import java.util.Scanner;

public class Main {

    static ServerFacade serverFacade;

    public static void main(String[] args) {
        // connect to the server
        try {
            serverFacade = new ServerFacade("https://localhost:8080");
        } catch (Exception error) {
            System.out.println("Couldn't connect to the server at \"https://localhost:8080\"");
            System.exit(0);
        }

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
                helpText();
                return true;
            case "quit":
                // this should close the program
                quit();
                return false;
            case "login":
                loginLoop();
        }
    }

    private static void introText() {
        System.out.println();
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("\033" + "[0;33mTo get started, type 'help'");
    }

    private static void helpText() {
        System.out.println();
        System.out.println("You can type 'quit' to leave, 'login' to log in, and 'register' to make a new account.");
        System.out.println();
    }

    private static void quit() {
        System.exit(0);
    }

    private static void loginLoop() {
        System.out.println("Great, let's get you logged in! First, type your username:");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        System.out.println("Awesome! And what's your password?");
        System.out.print(">>> ");

        String password = scanner.nextLine();

        // now that we have those, let's try and contact the server to log them in
        try {
            serverFacade.login(username, password);
        } catch (Exception error) {
            System.out.println("Couldn't log you in. Is your username and password correct?");
        }
    }
}