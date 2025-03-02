package dataaccess;

import model.AuthData;

public interface AuthDataAccessObject {
    AuthData addAuthToken(AuthData authData) throws DataAccessException;
    AuthData deleteAuthToken(String authToken) throws DataAccessException;
    AuthData[] getValidTokens(AuthData authData) throws DataAccessException;
    String getUserByAuthToken(String authToken) throws DataAccessException;
    String getAuthTokenByUser(String username) throws DataAccessException;
}
