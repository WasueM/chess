package services;
import dataaccess.AuthDataAccessMemory;
import dataaccess.DataAccessException;
import dataaccess.GameDataAccessMemory;
import dataaccess.UserDataAccessMemory;
import model.AuthData;
import model.UserData;

import java.util.Arrays;
import java.util.UUID;

public class LoginService {
    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        boolean isValidUser = verifyCredentials(loginRequest.username(), loginRequest.password());
        if (isValidUser) {
            String authToken = generateToken();
            AuthData newAuthData = new AuthData(authToken, loginRequest.username());
            AuthDataAccessMemory.addAuthToken(newAuthData);
            UserDataAccessMemory.addTokenToUser(newAuthData, loginRequest.username());
            return new LoginResult(loginRequest.username(), authToken);
        } else {
            return null;
        }
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
