package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.List;

public class AuthDataAccessMemory implements AuthDataAccessObject {
    final private List<AuthData> authTokens = new ArrayList<>();

    @Override
    public AuthData addAuthToken(AuthData authData) throws DataAccessException {
        authTokens.add(authData);
        return null;
    }

    @Override
    public AuthData deleteAuthToken(AuthData authData) throws DataAccessException {
        authTokens.remove(authData)
        return null;
    }

    @Override
    public AuthData[] getValidTokens(AuthData authData) throws DataAccessException {
        return authTokens.toArray(new AuthData[0]);
    }
}
