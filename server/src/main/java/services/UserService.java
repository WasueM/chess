package services;
import dataaccess.AuthDataAccessMemory;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccessMemory;
import model.AuthData;
import model.UserData;

import java.util.Arrays;
import java.util.UUID;

public class UserService {
    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        // find out if the username has been taken
        UserData user = UserDataAccessMemory.getUserByUsername(registerRequest.username());
        if (user == null) {
            // we can make a new user with this name
            UserDataAccessMemory.addUser(new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email()));

            // make the authToken and add to authenticatedUsers
            String authToken = authenticateUser(registerRequest.username());

            return new RegisterResult(registerRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        boolean isValidUser = verifyCredentials(loginRequest.username(), loginRequest.password());
        if (isValidUser) {
            String authToken = authenticateUser(loginRequest.username());
            return new LoginResult(loginRequest.username(), authToken);
        } else {
            return null;
        }
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws DataAccessException {
        // get the user and its auth data
        UserData user = UserDataAccessMemory.getUserByUsername(logoutRequest.username());
        AuthData authData = UserDataAccessMemory.getAuthTokenByUser(user);

        // set the authData to null in the user object and in the auth object
        AuthDataAccessMemory.deleteAuthToken(authData);
        UserDataAccessMemory.addTokenToUser(null, user.username());

        return new LogoutResult(logoutRequest.username());
    }

    public boolean verifyCredentials(String username, String password) throws DataAccessException {
        UserData user = UserDataAccessMemory.getUserByUsername(username);
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

    public String authenticateUser(String username) throws DataAccessException {
        String authToken = generateToken();
        AuthData newAuthData = new AuthData(authToken, username);
        AuthDataAccessMemory.addAuthToken(newAuthData);
        UserDataAccessMemory.addTokenToUser(newAuthData, username);
        return authToken;
    }

    public boolean verifyAuthToken(AuthData authToken) throws DataAccessException {
        AuthData[] validCredentials = AuthDataAccessMemory.getValidTokens();
        if (Arrays.asList(validCredentials).contains(authToken)) {
            return true;
        } else {
            return false;
        }
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
