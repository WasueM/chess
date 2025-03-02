package services;

import dataaccess.AuthDataAccessObject;
import dataaccess.DataAccessException;
import model.AuthData;

import java.util.UUID;

public class AuthService {

    private final AuthDataAccessObject authDataAccess;

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
            if (authData.authToken() == authToken) {
                return true;
            }
        }
        return false;
    }

    public LogoutResult logout(LogoutRequest logoutRequest) throws DataAccessException {
        // get the auth token to delete
        String authToken = authDataAccess.getAuthTokenByUser(logoutRequest.username());

        // delete the auth token
        authDataAccess.deleteAuthToken(authToken);

        return new LogoutResult(logoutRequest.username());
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
