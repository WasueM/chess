import chess.*;
import model.GameData;

import java.util.Objects;
import java.util.Scanner;

public class Main {

    static ServerFacade serverFacade;
    static int appState = 0; // 0 is not logged in, 1 in logged in, // 2 for in-game

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
        // different switch statements based on the app state, logged in or not?
        if (appState == 0) {
            switch (input) {
                case "help":
                    helpTextPreLogin();
                    return true;
                case "login":
                    login();
                    return true;
                case "register":
                    register();
                    return true;
            }
        } else if (appState == 1) {
            switch (input) {
                case "help":
                    helpTextPostLogin();
                    return true;
                case "logout":
                    logout();
                    return true;
                case "create game":
                    createGame();
                    return false;
                case "list games":
                    listGames();
                    return true;
                case "play game":
                    playGame();
                    return false;
                case "observe game":
                    observeGame();
                    return false;
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
        System.out.println("Looks like you typed something that wasn't a commend. Need help? Just type 'help'.");
        System.out.println();
        return true;
    }

    private static void introText() {
        System.out.println();
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        System.out.println("♕ 240 Chess Client: " + piece);
        System.out.println("\033" + "[0;33mTo get started, type 'help'");
    }

    private static void helpTextPreLogin() {
        System.out.println();
        System.out.println("You can type 'quit' to leave, 'login' to log in, and 'register' to make a new account.");
        System.out.println();
    }

    private static void helpTextPostLogin() {
        System.out.println();
        System.out.println("You can type 'quit' to leave, 'logout' to log out, and 'create game' to make a new game, 'list games' to see active games, 'play game' to join a game, or 'observe game' to spectate.");
        System.out.println();
    }

    private static void quit() {
        System.exit(0);
    }

    private static void login() {
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
        } catch (Exception error) {
            System.out.println("Couldn't log you in. Is your username and password correct?");
        }
    }

    private static void logout() {
        try {
            serverFacade.logout();
            appState = 0;
        } catch (Exception error ){
            System.out.println("Couldn't log you out. How odd. Just quit instead to restart.");
        }
    }

    private static void register() {
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
        } catch (Exception error) {
            System.out.println("Couldn't register you. Is the username and email already in use?");
        }
    }

    private static void listGames() {
        try {
            GameData[] games = serverFacade.listGames();

            System.out.println("Active Games: ");

            int counter = 0;
            for (GameData game : games) {
                counter++;
                System.out.println(counter + ". " + game.gameName());
            }
            System.out.println();

        } catch (Exception error) {
            System.out.println("Couldn't get a list of games. Sorry, that's our bad.");
        }
    }

    private static void createGame() {
        System.out.println("Great! Let's make you a new game. Tell us the game name: ");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String gameName = scanner.nextLine();

        try {
            GameData game = serverFacade.createGame(gameName);

            // if it didn't throw an error, it worked, so let them know
            System.out.println("Awesome! That game has been created.");

        } catch (Exception error) {
            System.out.println("Couldn't make a game, we're sorry!");
        }
    }

    private static void playGame() {
        System.out.println("Sounds good! Which game do you want to play?");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String gameToJoinName = scanner.nextLine();

        try {
            GameData game = serverFacade.createGame(gameToJoinName);

            // if it didn't throw an error, it worked, so let them know
            System.out.println("Awesome! You've joined the game. Happy playing!");

        } catch (Exception error) {
            System.out.println("Couldn't join that game. Was the game name correct?");
        }
    }

    private static void observeGame() {
        System.out.println("Sounds good! Which game do you want to observe?");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String gameToObserveName = scanner.nextLine();

        try {
            // won't do anything since it says to get this working only in phase 6
            // GameData game = serverFacade.(gameToJoinName);

            // if it didn't throw an error, it worked, so let them know
            System.out.println("We'll get this up and running in phase 6 so nothing for now");

        } catch (Exception error) {
            System.out.println("Couldn't join that game. Was the game name correct?");
        }
    }
}