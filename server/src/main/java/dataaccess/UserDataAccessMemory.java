package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class UserDataAccessMemory implements UserDataAccessObject {
    final private List<UserData> allUsers = new ArrayList<>();

    @Override
    public UserData getUserByUsername(String username) throws DataAccessException {
        for (UserData user : allUsers) {
            if (Objects.equals(user.username(), username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public UserData addUser(UserData user) throws DataAccessException {
        allUsers.add(user); // Store user separately
        return user;
    }
}
