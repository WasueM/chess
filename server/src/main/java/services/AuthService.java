package services;

import dataaccess.AuthDataAccessMemory;
import dataaccess.DataAccessException;
import dataaccess.UserDataAccessMemory;
import model.AuthData;
import model.UserData;

import java.util.Arrays;
import java.util.UUID;

public class AuthService {
    public static boolean verifyCredentials(String username, String password) throws DataAccessException {
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

    public static String authenticateUser(String username) throws DataAccessException {
        String authToken = generateToken();
        AuthData newAuthData = new AuthData(authToken, username);
        AuthDataAccessMemory.addAuthToken(newAuthData);
        UserDataAccessMemory.addTokenToUser(newAuthData, username);
        return authToken;
    }

    public static boolean verifyAuthToken(String authToken) throws DataAccessException {
        AuthData[] validCredentials = AuthDataAccessMemory.getValidTokens();
        for (AuthData authData : validCredentials) {
            if (authData.authToken() == authToken) {
                return true;
            }
        }
        return false;
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
