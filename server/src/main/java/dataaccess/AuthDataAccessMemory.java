package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AuthDataAccessMemory implements AuthDataAccessObject {
    final private List<AuthData> authTokens = new ArrayList<>();

    @Override
    public AuthData addAuthToken(AuthData authData) throws DataAccessException {
        authTokens.add(authData);
        return null;
    }

    @Override
    public AuthData deleteAuthToken(String authToken) throws DataAccessException {
        for (AuthData authData : authTokens) {
            if (authData.authToken().equals(authToken)) {

                authTokens.remove(authData);
                return authData;
            }
        }
        return null;
    }

    @Override
    public AuthData[] getValidTokens() throws DataAccessException {
        return authTokens.toArray(new AuthData[0]);
    }

    @Override
    public String getUserByAuthToken(String authToken) throws DataAccessException {
        for (AuthData authData : authTokens) {
            if (authData.authToken().equals(authToken)) {
                return authData.username();
            }
        }
        return null;
    }

    public String getAuthTokenByUser(String username) throws DataAccessException {
        System.out.println("WE");

        // get the access token for the user
        for (AuthData authData : authTokens) {
            if (authData.username().equals(username)) {
                return authData.authToken();
            }
        }
        return null;
    }
}
