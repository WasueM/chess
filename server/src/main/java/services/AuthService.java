package services;

import dataaccess.AuthDataAccessObject;
import dataaccess.DataAccessException;
import model.AuthData;
import services.requests.LogoutRequest;
import services.results.LogoutResult;

import java.util.UUID;

public class AuthService {

    private final AuthDataAccessObject authDataAccess;

    public AuthService(AuthDataAccessObject authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    public String authenticateUser(String username) throws DataAccessException {
        if (username == "") {
            throw new DataAccessException("Username can't be an empty string");
        }

        String authToken = generateToken();
        System.out.println("GENERATED TOKEN IS: " + authToken);
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
            return new LogoutResult(logoutRequest.authToken());
        } else {
            return null;
        }
    }

    public String getUserByAuthToken(String authToken) throws DataAccessException {
        return authDataAccess.getUserByAuthToken(authToken);
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
