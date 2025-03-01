package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;
import java.util.Objects;

public class UserDataAccessMemory implements UserDataAccessObject {
    final private HashMap<AuthData, UserData> allUsers = new HashMap<>();

    @Override
    public UserData getUserByUsername(String username) throws DataAccessException {
        for (UserData user : allUsers.values()) {
            if (Objects.equals(user.username(), username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public UserData addUser(AuthData auth, UserData user) throws DataAccessException {
        allUsers.put(auth, user);
        return user;
    }

    @Override
    public UserData getUserByAuthToken(AuthData authData) throws DataAccessException {
        return allUsers.get(authData);
    }
}
