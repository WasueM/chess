package dataaccess;

import model.AuthData;

public interface AuthDataAccessObject {
    AuthData addAuthToken(AuthData authData) throws DataAccessException;
    AuthData deleteAuthToken(String authToken) throws DataAccessException;
    AuthData[] getValidTokens() throws DataAccessException;
    String getUserByAuthToken(String authToken) throws DataAccessException;
    String getAuthTokenByUser(String authToken) throws DataAccessException;
}
