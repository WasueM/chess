package dataaccess;

import model.AuthData;

public interface AuthDataAccessObject {
    AuthData addAuthToken(AuthData authData) throws DataAccessException;
    AuthData deleteAuthToken(AuthData authData) throws DataAccessException;
    AuthData[] getValidTokens(AuthData authData) throws DataAccessException;
}
