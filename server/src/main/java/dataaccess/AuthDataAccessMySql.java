package dataaccess;

import model.AuthData;
import java.sql.*;

public final class AuthDataAccessMySql implements AuthDataAccessObject {

    @Override
    public AuthData addAuthToken(AuthData authData) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData deleteAuthToken(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData[] getValidTokens() throws DataAccessException {
        return new AuthData[0];
    }

    @Override
    public String getUserByAuthToken(String authToken) throws DataAccessException {
        return "";
    }
}