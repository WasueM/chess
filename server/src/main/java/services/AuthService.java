package services;

import dataaccess.AuthDataAccessObject;
import dataaccess.DataAccessException;
import model.AuthData;

import javax.xml.crypto.Data;
import java.util.UUID;

public class AuthService {

    private AuthDataAccessObject authDataAccess;

    public AuthService(AuthDataAccessObject authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public String authenticateUser(String username) throws DataAccessException {
        String authToken = generateToken();
        AuthData newAuthData = new AuthData(authToken, username);
        authDataAccess.addAuthToken(newAuthData);
        return authToken;
    }

    public boolean verifyAuthToken(String authToken) throws DataAccessException {
        AuthData[] validCredentials = authDataAccess.getValidTokens();
        for (AuthData authData : validCredentials) {
            if (authData.authToken().equals(authToken)) {
                return true;
            }
        }
        return false;
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws DataAccessException {
        // delete the auth token
        AuthData authData = authDataAccess.deleteAuthToken(logoutRequest.authToken());

        if (authData != null) {
            System.out.println("WO");
            return new LogoutResult(logoutRequest.authToken());
        } else {
            System.out.println("WI");
            return null;
        }
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String getUserByAuthToken(String authToken) throws DataAccessException {
        return authDataAccess.getUserByAuthToken(authToken);
    }
}
