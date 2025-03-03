package services;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccessObject;
import model.UserData;

public class UserService {

    private final UserDataAccessObject userDataAccess;
    private final AuthService authService;

    public UserService(UserDataAccessObject userDataAccess, AuthService authService) {
        this.userDataAccess = userDataAccess;
        this.authService = authService;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        // find out if the username has been taken
        UserData user = userDataAccess.getUserByUsername(registerRequest.username());
        if (user == null) {
            // we can make a new user with this name
            userDataAccess.addUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));

            // make the authToken and add to authenticatedUsers
            String authToken = authService.authenticateUser(registerRequest.username());

            return new RegisterResult(registerRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        System.out.println("ALIVE");
        boolean isValidUser = verifyCredentials(loginRequest.username(), loginRequest.password());
        System.out.println("STILL ALIVE");
        if (isValidUser) {
            System.out.println("STILL ALIVE SOMEHOW");
            String authToken = authService.authenticateUser(loginRequest.username());
            System.out.println("BWAHAHAHA");
            return new LoginResult(loginRequest.username(), authToken);
        } else {
            System.out.println("Not valid user");
            return null;
        }
    }

    public boolean verifyCredentials(String username, String password) throws DataAccessException {
        UserData user = userDataAccess.getUserByUsername(username);
        System.out.println("USERS: " + user.toString());
        System.out.println("PASSWORD TO LOG IN WITH: " + password);
        if (user != null) {
            if (user.password().equals(password)) {
                System.out.println("HOWDY");
                return true;
            } else {
                System.out.println("WOOO");
                return false;
            }
        } else {
            return false;
        }
    }
}
