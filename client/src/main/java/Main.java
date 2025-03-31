import chess.*;
import client.ConsoleCommands;
import client.ServerFacade;

import java.util.Objects;
import java.util.Scanner;

public class Main {

    static ServerFacade serverFacade;
    static ConsoleCommands consoleCommands;

    public static void main(String[] args) {
        // make the client
        try {
            serverFacade = new ServerFacade("http://localhost:" + 8080 + "/");
        } catch (Exception error) {
            System.out.println("Couldn't connect to the server at \"http://localhost:" + 8080 + "/\"");
            System.exit(0);
        }

        // make a ConsoleCommands class that helps reduce this size of this file
        consoleCommands = new ConsoleCommands(serverFacade);

        // on launch, show the menus
        try {
            runConsoleMenus();
        } catch (Exception error) {
            System.out.println("ERROR somewhere in the app! " + error.getMessage());
            error.printStackTrace();
        }
    }

    private static void runConsoleMenus() throws Exception {
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

    private static boolean handleInput(String input) throws Exception {
        // different switch statements based on the app state, logged in or not? playing game? observing?
        if (consoleCommands.appState == 0) {
            switch (input) {
                case "help":
                    return consoleCommands.helpTextPreLogin();
                case "login":
                    return consoleCommands.login();
                case "register":
                    return consoleCommands.register();
            }
        } else if (consoleCommands.appState == 1) {
            switch (input) {
                case "help":
                    return consoleCommands.helpTextPostLogin();
                case "logout":
                    return consoleCommands.logout();
                case "create game":
                    return consoleCommands.createGame();
                case "list games":
                    return consoleCommands.listGames();
                case "play game":
                    return consoleCommands.playGame(); // will return true if it doesn't work (keep getting input) or false otherwise
                case "observe game":
                    return consoleCommands.observeGame();
                case "clear database":
                    return clearDatabase();
            }
        } else if (consoleCommands.appState == 2) { // playing game
            switch (input) {
                case "help":
                    return consoleCommands.helpTextPlayingGame();
                case "redraw chess board":
                    return redrawChessBoard();
                case "leave":
                    return leaveGame();
                case "make move":
                    return makeMove();
                case "resign":
                    return resign();
                case "highlight legal moves":
                    return highlightLegalMoves();
            }
        } else if (consoleCommands.appState == 3) {
            switch (input) {
                case "help":
                    return consoleCommands.helpTextObservingGame();
                case "leave":
                    return leaveGame();
            }
        }
        // quitting should always be available, logged in or not:
        if (Objects.equals(input, "quit")) {
            // this should close the program
            consoleCommands.quit();
            return false; // break the loop, but this should never even get hit anyways if the app closed right
        }

        // if it hasn't return by now, it doesn't match anything, so we should tell them that.
        System.out.println();
        System.out.println("Looks like you typed something that wasn't a command. Need help? Just type 'help'.");
        System.out.println();
        return true;
    }

    private static void introText() {
        System.out.println();
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("\033" + "[0;33mTo get started, type 'help'");
    }

    private static boolean clearDatabase() {
        try {
            serverFacade.clearDatabase();
            System.out.println("Database deleted!");
            return true; // keep accepting input
        } catch (Exception error) {
            System.out.println("Error clearing the database.");
            return true; // still keep accepting input
        }
    }

    private static boolean redrawChessBoard() {
        // just call show again to redo the drawing
        consoleCommands.gameController.show();
        return true;
    }

    private static boolean highlightLegalMoves() {
        int row = readInValidRow();
        int column = readInValidColumn();

        if ((row > 8) || (row > 8)) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        consoleCommands.gameController.highlightLegalMoves(row, column);

        return true;
    }

    private static int readInValidRow() {
        System.out.println("Enter the row: (Number)");
        System.out.print(">>> ");

        int row = 9999;
        Scanner scanner = new Scanner(System.in);
        try {
            row = Integer.parseInt(scanner.nextLine());
        } catch (Exception error) {
            System.out.println("Please enter a number next time! \n");
            return 9999;
        }

        if ((row > 8) || (row < 1)) {
            // this is an error, so just return and tell the user to input something better next time
            System.out.println("That number is too high, too low, or not a number at all, " +
                    "there's no row for that. Please enter a better number next time.");

            // return true that we should keep accepting input in the loop
            return 9999;
        }

        return row;
    }

    private static int readInValidColumn() {
        System.out.println("Enter the column: (Letter)");
        System.out.print(">>> ");

        String character = "a";
        Scanner scanner = new Scanner(System.in);
        try {
            character = scanner.nextLine();
        } catch (Exception error) {
            System.out.println("Please enter a letter from 'a' to 'h' next time! \n");
            return 9999;
        }

        char charCharacter = character.toCharArray()[0];
        int column = charCharacter - 'a' + 1;

        if ((column > 8) || (column < 1)) {
            // this is an error, so just return and tell the user to input something better next time
            System.out.println("That column is too high, too low, or not a column at all, " +
                    "there's no column for that. Please enter a valid letter next time.");

            // return true that we should keep accepting input in the loop
            return 9999;
        }

        return column;
    }

    private static boolean leaveGame() throws Exception {
        consoleCommands.appState = 1; // return to the logged in version
        consoleCommands.gameController.endWebSocketConnection();
        System.out.println("Left the game!");
        return true;
    }

    private static boolean makeMove() throws Exception {
        System.out.println("Please enter where you want to move from");
        int fromRow = readInValidRow();
        if (fromRow > 8) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        int fromColumn = readInValidColumn();
        if (fromColumn > 8) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        System.out.println("Please enter where you want to move to");
        int toRow = readInValidRow();
        if (toRow > 8) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        int toColumn = readInValidColumn();
        if (toColumn > 8) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        consoleCommands.gameController.makeMove(fromRow, fromColumn, toRow, toColumn);
        return true;
    }

    private static boolean resign() throws Exception {
        consoleCommands.gameController.resign();
        return true;
    }
}