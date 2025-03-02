package services;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccessObject;
import model.UserData;

public class UserService {

    private final UserDataAccessObject userDataAccess;

    public UserService(UserDataAccessObject userDataAccess) {
        this.userDataAccess = userDataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        // find out if the username has been taken
        UserData user = userDataAccess.getUserByUsername(registerRequest.username());
        if (user == null) {
            // we can make a new user with this name
            userDataAccess.addUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));

            // make the authToken and add to authenticatedUsers
            String authToken = AuthService.authenticateUser(registerRequest.username());

            return new RegisterResult(registerRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        boolean isValidUser = verifyCredentials(loginRequest.username(), loginRequest.password());
        if (isValidUser) {
            String authToken = AuthService.authenticateUser(loginRequest.username());
            return new LoginResult(loginRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public boolean verifyCredentials(String username, String password) throws DataAccessException {
        UserData user = userDataAccess.getUserByUsername(username);
        if (user != null) {
            if (user.password() == password) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
