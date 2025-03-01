package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class UserDataAccessMemory implements UserDataAccessObject {
    final static private List<UserData> allUsers = new ArrayList<>();
    final static private HashMap<UserData, AuthData> userAuthMapping = new HashMap<>();

    @Override
    public static UserData getUserByUsername(String username) throws DataAccessException {
        for (UserData user : allUsers) {
            if (Objects.equals(user.username(), username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public static UserData addUser(UserData user) throws DataAccessException {
        allUsers.add(user); // Store user separately
        userAuthMapping.put(user, null); // Associate with auth data
        return user;
    }

    public static UserData addTokenToUser(AuthData auth, String username) {
        for (UserData user : allUsers) {
            if (Objects.equals(user.username(), username)) {
                userAuthMapping.put(user, auth);
            }
        }
    }

    @Override
    public static UserData getUserByAuthToken(AuthData authData) throws DataAccessException {
        for (UserData user : userAuthMapping.keySet()) {
            if (Objects.equals(userAuthMapping.get(user), authData)) {
                return user;
            }
        }
        return null;
    }

    public static AuthData getAuthTokenByUser(UserData user) throws DataAccessException {
        // get the access token for the user
        return userAuthMapping.get(user);
    }
}
