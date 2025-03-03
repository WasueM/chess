package services;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccessObject;
import model.UserData;

public class UserService {

    private UserDataAccessObject userDataAccess;
    private AuthService authService;

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
            // make the authToken and add to authenticatedUsers
            //String authToken = authService.authenticateUser(registerRequest.username());

            return null;
          //  return new RegisterResult(null, null, "ERROR: Already registered this username. Use this authToken to get in for it");
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        boolean isValidUser = verifyCredentials(loginRequest.username(), loginRequest.password());
        if (isValidUser) {
            String authToken = authService.authenticateUser(loginRequest.username());
            return new LoginResult(loginRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public boolean verifyCredentials(String username, String password) throws DataAccessException {
        UserData user = userDataAccess.getUserByUsername(username);
        if (user != null) {
            if (user.password().equals(password)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
