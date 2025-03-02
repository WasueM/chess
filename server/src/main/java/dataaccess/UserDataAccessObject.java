package dataaccess;

import model.AuthData;
import model.UserData;

public interface UserDataAccessObject {
    UserData getUserByUsername(String username) throws DataAccessException;
    UserData addUser(UserData user) throws DataAccessException;
    static UserData getUserByAuthToken(AuthData authData) throws DataAccessException;
}
