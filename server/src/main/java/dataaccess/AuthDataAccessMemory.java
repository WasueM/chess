package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AuthDataAccessMemory implements AuthDataAccessObject {
    final static private List<AuthData> authTokens = new ArrayList<>();

    @Override
    public static AuthData addAuthToken(AuthData authData) throws DataAccessException {
        authTokens.add(authData);
        return null;
    }

    @Override
    public static AuthData deleteAuthToken(AuthData authData) throws DataAccessException {
        authTokens.remove(authData);
        return null;
    }

    @Override
    public static AuthData[] getValidTokens() throws DataAccessException {
        return authTokens.toArray(new AuthData[0]);
    }

    @Override
    public static String getUserByAuthToken(String authToken) throws DataAccessException {
        for (AuthData authData : authTokens) {
            if (authData.authToken() == authToken) {
                return authData.username();
            }
        }
        return null;
    }

    public static String getAuthTokenByUser(String username) throws DataAccessException {
        // get the access token for the user
        for (AuthData authData : authTokens) {
            if (authData.username() == username) {
                return authData.authToken();
            }
        }
        return null;
    }
}
