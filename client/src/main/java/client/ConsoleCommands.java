package client;

import chess.ChessGame;
import model.GameData;

import java.util.Objects;
import java.util.Scanner;

// this is to help move stuff from the client's main so it doesn't handle so much
public class ConsoleCommands {
    private static ServerFacade serverFacade;
    public static GameController gameController;
    public static int appState = 0; // 0 is not logged in, 1 in logged in, // 2 for playing game, // 3 for observing game
    static GameData[] gameList; // store the games list in an array

    public ConsoleCommands(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public static boolean helpTextPreLogin() {
        System.out.println();
        System.out.println("You can type" +
                "\n'quit' to leave," +
                "\n'login' to log in, " +
                "\n'register' to make a new account, and" +
                "\n'help' to bring up this list.");
        System.out.println();

        return true;
    }

    public static boolean helpTextPostLogin() {
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

    public static boolean helpTextPlayingGame() {
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

    public static boolean helpTextObservingGame() {
        System.out.println();
        System.out.println("You can type" +
                "\n'quit' to quite the application," +
                "\n'leave' to to leave the game, or" +
                "\n'help' to bring up this list.");
        System.out.println();

        return true;
    }

    public static void quit() {
        System.exit(0);
    }

    public static boolean login() {
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

    public static boolean logout() {
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

    public static boolean register() {
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

    public static boolean listGames() {
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

    public static String printUsernameOrAvailable(String username) {
        if (username == null) {
            return "available";
        } else {
            return username;
        }
    }

    public static boolean createGame() {
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

    public static boolean playGame() {
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
            System.out.println("That number is too high, too low, or not a number at all, " +
                    "there's no game for that. Please enter a smaller number next time.");

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
                    // switch to game mode
                    appState = 2;
                    gameController = new GameController(game, serverFacade);
                    serverFacade.setGameController(gameController);

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

    public static boolean observeGame() {
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
            System.out.println("That number is too high, too low, or not a number at all, " +
                    "there's no game for that. Please enter a smaller number next time.");

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
                    // switch to game mode
                    appState = 3;
                    gameController = new GameController(game, serverFacade);
                    serverFacade.setGameController(gameController);

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
}
