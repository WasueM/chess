package services;
import dataaccess.AuthDataAccessMemory;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccessMemory;
import dataaccess.UserDataAccessObject;
import model.AuthData;
import model.UserData;

public class UserService {

    private final UserDataAccessObject userDataAccess;

    public UserService(UserDataAccessObject userDataAccess) {
        this.userDataAccess = userDataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        // find out if the username has been taken
        UserData user = UserDataAccessMemory.getUserByUsername(registerRequest.username());
        if (user == null) {
            // we can make a new user with this name
            UserDataAccessMemory.addUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));

            // make the authToken and add to authenticatedUsers
            String authToken = AuthService.authenticateUser(registerRequest.username());

            return new RegisterResult(registerRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        boolean isValidUser = AuthService.verifyCredentials(loginRequest.username(), loginRequest.password());
        if (isValidUser) {
            String authToken = AuthService.authenticateUser(loginRequest.username());
            return new LoginResult(loginRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws DataAccessException {
        // get the auth token to delete
        String authToken = AuthDataAccessMemory.getAuthTokenByUser(logoutRequest.username());

        // delete the auth token
        AuthDataAccessMemory.deleteAuthToken(authToken);

        return new LogoutResult(logoutRequest.username());
    }
}
