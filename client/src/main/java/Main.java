import chess.*;
import client.GameController;
import client.ServerFacade;
import model.GameData;

import java.util.Objects;
import java.util.Scanner;

public class Main {

    static ServerFacade serverFacade;
    static int appState = 0; // 0 is not logged in, 1 in logged in, // 2 for playing game, // 3 for observing game
    static GameData[] gameList; // store the games list in an array
    static GameController gameController;

    public static void main(String[] args) {
        // make the client
        try {
            serverFacade = new ServerFacade("http://localhost:" + 8080 + "/");
        } catch (Exception error) {
            System.out.println("Couldn't connect to the server at \"http://localhost:" + 8080 + "/\"");
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
        // different switch statements based on the app state, logged in or not? playing game? observing?
        if (appState == 0) {
            switch (input) {
                case "help":
                    return helpTextPreLogin();
                case "login":
                    return login();
                case "register":
                    return register();
            }
        } else if (appState == 1) {
            switch (input) {
                case "help":
                    return helpTextPostLogin();
                case "logout":
                    return logout();
                case "create game":
                    return createGame();
                case "list games":
                    return listGames();
                case "play game":
                    return playGame(); // will return true if it doesn't work (keep getting input) or false otherwise
                case "observe game":
                    return observeGame();
                case "clear database":
                    return clearDatabase();
            }
        } else if (appState == 2) { // playing game
            switch (input) {
                case "help":
                    return helpTextPlayingGame();
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
        } else if (appState == 3) {
            switch (input) {
                case "help":
                    return helpTextObservingGame();
                case "leave":
                    return leaveGame();
            }
        }
        // quitting should always be available, logged in or not:
        if (Objects.equals(input, "quit")) {
            // this should close the program
            quit();
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

    private static boolean helpTextPreLogin() {
        System.out.println();
        System.out.println("You can type" +
                "\n'quit' to leave," +
                "\n'login' to log in, " +
                "\n'register' to make a new account, and" +
                "\n'help' to bring up this list.");
        System.out.println();

        return true;
    }

    private static boolean helpTextPostLogin() {
        System.out.println();
        System.out.println("You can type" +
                "\n'quit' to leave," +
                "\n'logout' to log out," +
                "\n'create game' to make a new game," +
                "\n'list games' to see active games," +
                "\n'play game' to join a game," +
                "\n'observe game' to spectate, or" +
                "\n'help' to bring up this list.");
        System.out.println();

        return true;
    }

    private static boolean helpTextPlayingGame() {
        System.out.println();
        System.out.println("You can type" +
                "\n'quit' to quit the application," +
                "\n'redraw chess board' to redraw the board," +
                "\n'leave' to leave the game," +
                "\n'make move' to make a move," +
                "\n'resign' to forfeit the match," +
                "\n'highlight legal moves' to highlight your possible moves, or" +
                "\n'help' to bring up this list.");
        System.out.println();

        return true;
    }

    private static boolean helpTextObservingGame() {
        System.out.println();
        System.out.println("You can type" +
                "\n'quit' to quite the application," +
                "\n'leave' to to leave the game, or" +
                "\n'help' to bring up this list.");
        System.out.println();

        return true;
    }

    private static void quit() {
        System.exit(0);
    }

    private static boolean login() {
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

            // if it makes it this far, it means the server responded 200 so the login worked. Change the app state accordingly
            appState = 1;
            System.out.println("You're logged in!");

            // list the games immediately
            System.out.println();
            listGames();

            return true;

        } catch (Exception error) {
            System.out.println("Couldn't log you in. Is your username and password correct?");
            return true;
        }
    }

    private static boolean logout() {
        try {
            serverFacade.logout();
            appState = 0;
            System.out.println("You're now logged out!");
            return true;
        } catch (Exception error ){
            System.out.println("Couldn't log you out. How odd. Just quit instead to restart.");
            return true;
        }
    }

    private static boolean register() {
        System.out.println("Great, let's get you logged in! First, type your username:");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        System.out.println("Awesome! And what's your password?");
        System.out.print(">>> ");

        String password = scanner.nextLine();

        System.out.println("Woohoo! Now just give us your email:");
        System.out.print(">>> ");

        String email = scanner.nextLine();

        // now that we have those, let's try and contact the server to log them in
        try {
            serverFacade.register(username, password, email);

            // if it makes it this far, it means the server responded 200 so the login worked. Change the app state accordingly
            appState = 1;
            System.out.println("You're all registered!");

            // list the games immediately
            System.out.println();
            listGames();

            return true;

        } catch (Exception error) {
            System.out.println("Couldn't register you. Is the username and email already in use? ");
            return true;
        }
    }

    private static boolean listGames() {
        try {
            gameList = serverFacade.listGames();

            if (gameList.length == 0) {
                System.out.println("There are no active games. Make one with 'create game'");
                return true;
            }

            System.out.println("Active Games: ");

            int counter = 0;
            for (GameData game : gameList) {
                counter++;
                // print the game name, then the white player, then the black play
                System.out.println(counter + ". " + game.gameName() +
                        ", WHITE: " + printUsernameOrAvailable(game.whiteUsername()) +
                        ", BLACK: " + printUsernameOrAvailable(game.blackUsername()));
            }
            System.out.println();

            return true;

        } catch (Exception error) {
            System.out.println("Couldn't get a list of games. Sorry, that's our bad. \n");
            return true;
        }
    }

    private static String printUsernameOrAvailable(String username) {
        if (username == null) {
            return "available";
        } else {
            return username;
        }
    }

    private static boolean createGame() {
        System.out.println("Great! Let's make you a new game. Tell us the game name: ");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String gameName = scanner.nextLine();

        try {
            int gameID = serverFacade.createGame(gameName);

            // update the list of games now that it's created the game
            listGames();

            // if it didn't throw an error, it worked, so let them know
            System.out.println("Awesome! That game has been created.");

            // keep accepting input
            return true;

        } catch (Exception error) {
            System.out.println("Couldn't make a game, we're sorry! \n");
            return true; // keep accepting input
        }
    }

    private static boolean playGame() {
        System.out.println("Sounds good! Which game do you want to play? (Number)");
        System.out.print(">>> ");

        int gameToJoinNumber = 9999;
        Scanner scanner = new Scanner(System.in);
        try {
            gameToJoinNumber = Integer.parseInt(scanner.nextLine());
        } catch (Exception error) {
            System.out.println("Please enter a number next time! \n");
            return true;
        }

        if ((gameToJoinNumber > gameList.length) || (gameToJoinNumber < 1)) {
            // this is an error, so just return and tell the user to input something better next time
            System.out.println("That number is too high, too low, or not a number at all, there's no game for that. Please enter a smaller number next time.");

            // return true that we should keep accepting input in the loop
            return true;
        }

        GameData gameToJoin = gameList[gameToJoinNumber - 1];
        int gameIDToJoin = gameToJoin.gameID();

        System.out.println("Awesome! Type 'WHITE' to be white or 'BLACK' to be black!");
        System.out.print(">>> ");

        String color = scanner.nextLine();
        if ((!Objects.equals(color, "WHITE")) && (!Objects.equals(color, "BLACK"))) {
            // this is an error, tell the user to be better next time
            System.out.println("You didn't type 'WHITE' or 'BLACK' so be better next time!");
        }

        try {
            int gameID = serverFacade.joinGame(color, gameIDToJoin);

            // update the list of games now that it's created the game
            listGames();

            // if it didn't throw an error, it worked, so let them know
            System.out.println("Awesome! You've joined the game. Happy playing!");

            // grab the actual game data for that game ID
            for (GameData game : gameList) {
                if (game.gameID() == gameID) {
                    gameController = new GameController(game, serverFacade);

                    // switch to game mode
                    appState = 2;
                    gameController = new GameController(game, serverFacade);

                    // set the team color
                    ChessGame.TeamColor teamColor = ChessGame.TeamColor.WHITE;
                    if (Objects.equals(color, "BLACK")) {
                        teamColor = ChessGame.TeamColor.BLACK;
                    }

                    gameController.setPlayer(teamColor);
                    gameController.show();

                    return true; // no more input for now, since they'll be in a game //false but for now it's easy true
                }
            }

            // there was no game found
            System.out.println("Failed to join the game,");
            return true;

        } catch (Exception error) {
            System.out.println("Couldn't join that game. Was the game name correct? Or was that color already taken? \n");
            return true; // can keep getting input
        }
    }

    private static boolean observeGame() {
        System.out.println("Sounds good! Which game do you want to observe? (Number)");
        System.out.print(">>> ");

        int gameToObserveNumber = 9999;
        Scanner scanner = new Scanner(System.in);
        try {
            gameToObserveNumber = Integer.parseInt(scanner.nextLine());
        } catch (Exception error) {
            System.out.println("Please enter a number next time! \n");
            return true;
        }

        if ((gameToObserveNumber > gameList.length) || (gameToObserveNumber < 1)) {
            // this is an error, so just return and tell the user to input something better next time
            System.out.println("That number is too high, too low, or not a number at all, there's no game for that. Please enter a smaller number next time.");

            // return true that we should keep accepting input in the loop
            return true;
        }

        GameData gameToJoin = gameList[gameToObserveNumber - 1];
        int gameIDToObserve = gameToJoin.gameID();

        try {
            // won't do anything since it says to get this working only in phase 6
            // grab the actual game data for that game ID
            for (GameData game : gameList) {
                if (game.gameID() == gameIDToObserve) {
                    gameController = new GameController(game, serverFacade);

                    // switch to game mode
                    appState = 3;
                    gameController = new GameController(game, serverFacade);

                    // set the team color to white for observe
                    ChessGame.TeamColor teamColor = ChessGame.TeamColor.WHITE;

                    gameController.setPlayer(teamColor);
                    gameController.show();

                    return true; // no more input for now, since they'll be in a game //false but for now it's easy true
                }
            }

            // if it didn't throw an error, it worked, so let them know
            System.out.println("We'll get this up and running in phase 6 so nothing else for now");
            return true; // false later but for now it's easier true

        } catch (Exception error) {
            System.out.println("Couldn't join that game. Was the game name correct?");
            return true;
        }
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
        gameController.show();
        return true;
    }

    private static boolean highlightLegalMoves() {
        int row = readInValidRow();
        int column = readInValidColumn();

        if ((row > 8) || (row > 8)) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        gameController.highlightLegalMoves(row, column);

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

    private static boolean leaveGame() {
        appState = 2; // return to the logged in version
        return true;
    }

    private static boolean makeMove() throws Exception {
        System.out.println("Please enter where you want to move from");
        int fromRow = readInValidRow();
        int fromColumn = readInValidColumn();

        if ((fromRow > 8) || (fromColumn > 8)) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        System.out.println("Please enter where you want to move to");
        int toRow = readInValidRow();
        int toColumn = readInValidColumn();

        if ((toRow > 8) || (toColumn > 8)) {
            // just making sure they didn't return 9999, which they do for errors. If they do, just go back to the input loop
            return true;
        }

        gameController.makeMove(fromRow, fromColumn, toRow, toColumn);
        return true;
    }

    private static boolean resign() throws Exception {
        gameController.resign();
        return true;
    }
}