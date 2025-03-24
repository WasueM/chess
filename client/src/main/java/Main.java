import chess.*;
import client.GameController;
import client.ServerFacade;
import model.GameData;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import server.Server;

public class Main {

    static ServerFacade serverFacade;
    static int appState = 0; // 0 is not logged in, 1 in logged in, // 2 for in-game
    static GameData[] gameList; // store the games list in an array
    static GameController gameController;

    public static void main(String[] args) {
        // for testing locally, make the local server
        Server server = new Server();
        var port = server.run(8080);
        System.out.println("Started local HTTP server on " + port);

        // make the client
        try {
            serverFacade = new ServerFacade("http://localhost:" + port + "/");
        } catch (Exception error) {
            System.out.println("Couldn't connect to the server at \"http://localhost:" + port + "/\"");
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
                    return createGame();
                case "list games":
                    listGames();
                    return true;
                case "play game":
                    return playGame(); // will return true if it doesn't work (keep getting input) or false otherwise
                case "observe game":
                    return observeGame();
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
            System.out.println("You're logged in!");

            // list the games immediately
            System.out.println();
            listGames();
        } catch (Exception error) {
            System.out.println("Couldn't log you in. Is your username and password correct?");
        }
    }

    private static void logout() {
        try {
            serverFacade.logout();
            appState = 0;
            System.out.println("You're now logged out!");
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
            System.out.println("You're all registered!");

            // list the games immediately
            System.out.println();
            listGames();

        } catch (Exception error) {
            System.out.println("Couldn't register you. Is the username and email already in use? " + error.getMessage());
        }
    }

    private static void listGames() {
        try {
            gameList = serverFacade.listGames();

            if (gameList.length == 0) {
                System.out.println("There are no active games. Make one with 'create game'");
                return;
            }

            System.out.println("Active Games: ");

            int counter = 0;
            for (GameData game : gameList) {
                counter++;
                // print the game name, then the white player, then the black play
                System.out.println(counter + ". " + game.gameName() + ", WHITE: " + game.whiteUsername() + ", BLACK: " + game.blackUsername());
            }
            System.out.println();

        } catch (Exception error) {
            System.out.println("Couldn't get a list of games. Sorry, that's our bad. \n" + error.getMessage());
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

            // grab the actual game data for that game ID
            for (GameData game : gameList) {
                if (game.gameID() == gameID) {
                    gameController = new GameController(game);

                    // switch to game mode
                    appState = 2;
                    gameController = new GameController(game);
                    gameController.show();

                    return false; // no more input for now, since they'll be in a game
                }
            }

            // there was no game found
            System.out.println("Failed to create the game,");
            return true;

        } catch (Exception error) {
            System.out.println("Couldn't make a game, we're sorry! \n" + error.getMessage());
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
            System.out.println("Please enter a number next time! \n" + error.getMessage());
            gameToJoinNumber = 9999;
        }

        if (gameToJoinNumber > gameList.length) {
            // this is an error, so just return and tell the user to input something better next time
            System.out.println("That number is too high or not a number at all, there's no game for that. Please enter a smaller number next time.");

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
                    gameController = new GameController(game);

                    // switch to game mode
                    appState = 2;
                    System.out.println("THE GAME HAS A WHITE USERNAME OF: " + game.whiteUsername());
                    gameController = new GameController(game);
                    gameController.show();

                    return false; // no more input for now, since they'll be in a game
                }
            }

            // there was no game found
            System.out.println("Failed to join the game,");
            return true;

        } catch (Exception error) {
            System.out.println("Couldn't join that game. Was the game name correct? \n" + error.getMessage());
            return true; // can keep getting input
        }
    }

    private static boolean observeGame() {
        System.out.println("Sounds good! Which game do you want to observe?");
        System.out.print(">>> ");

        Scanner scanner = new Scanner(System.in);
        String gameToObserveName = scanner.nextLine();

        try {
            // won't do anything since it says to get this working only in phase 6

            // if it didn't throw an error, it worked, so let them know
            System.out.println("We'll get this up and running in phase 6 so nothing for now");
            return true;

        } catch (Exception error) {
            System.out.println("Couldn't join that game. Was the game name correct?");
            return true;
        }
    }
}