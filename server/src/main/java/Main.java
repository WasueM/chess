import chess.*;
import dataaccess.AuthDataAccessMemory;
import dataaccess.GameDataAccessMemory;
import dataaccess.UserDataAccessMemory;
import server.Server;
import services.AuthService;
import services.GameService;
import services.UserService;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        // create the memory version of the database
        AuthDataAccessMemory authDataAccessMemory = new AuthDataAccessMemory();
        GameDataAccessMemory gameDataAccessMemory = new GameDataAccessMemory();
        UserDataAccessMemory userDataAccessMemory = new UserDataAccessMemory();

        // create the services based on the version of the database we want
        AuthService authService = new AuthService(authDataAccessMemory);
        GameService gameService = new GameService(gameDataAccessMemory, authDataAccessMemory);
        UserService userService = new UserService(userDataAccessMemory);

        Server server = new Server(authService, userService, gameService);
        server.run(8080);
    }
}