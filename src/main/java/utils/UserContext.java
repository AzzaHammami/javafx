package utils;

import models.User;

public class UserContext {
    private static UserContext instance;
    private User currentUser;

    private UserContext() {}

    public static UserContext getInstance() {
        if (instance == null) {
            instance = new UserContext();
        }
        return instance;
    }

    public User getCurrentUser() {
        System.out.println("[UserContext] getCurrentUser: " + (currentUser != null ? currentUser.getName() : "null") + " | instance: " + this);
        return currentUser;
    }

    public void setCurrentUser(User user) {
        System.out.println("[UserContext] setCurrentUser: " + (user != null ? user.getName() : "null") + " | instance: " + this);
        this.currentUser = user;
    }
}
