package dataaccess;

import model.UserData;

public class UserDataAccessMySql implements UserDataAccessObject {
    @Override
    public UserData getUserByUsername(String username) throws DataAccessException {
        return null;
    }

    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        return null;
    }
}
