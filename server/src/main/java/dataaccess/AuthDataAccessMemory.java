package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.List;

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
}
