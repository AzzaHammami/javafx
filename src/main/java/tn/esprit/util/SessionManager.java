package tn.esprit.util;

import tn.esprit.model.User;
import java.io.*;
import java.util.prefs.Preferences;

/**
 * Gestionnaire de session pour maintenir l'utilisateur connecté
 */
public class SessionManager {
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_EMAIL = "user_email";
    private static final String PREF_USER_ROLES = "user_roles";
    private static final String PREF_IS_LOGGED_IN = "is_logged_in";
    
    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);
    private static User currentUser = null;
    
    /**
     * Sauvegarde les informations de l'utilisateur connecté
     * @param user L'utilisateur à sauvegarder
     */
    public static void saveUserSession(User user) {
        if (user != null) {
            prefs.putLong(PREF_USER_ID, user.getId());
            prefs.put(PREF_USER_EMAIL, user.getEmail());
            prefs.put(PREF_USER_ROLES, user.getRoles());
            prefs.putBoolean(PREF_IS_LOGGED_IN, true);
            currentUser = user;
        }
    }
    
    /**
     * Récupère l'utilisateur connecté depuis les préférences
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public static User getCurrentUser() {
        if (currentUser == null && isLoggedIn()) {
            currentUser = new User(
                prefs.getLong(PREF_USER_ID, 0),
                prefs.get(PREF_USER_EMAIL, ""),
                "", // Le mot de passe n'est pas stocké pour des raisons de sécurité
                prefs.get(PREF_USER_ROLES, "")
            );
        }
        return currentUser;
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté, false sinon
     */
    public static boolean isLoggedIn() {
        return prefs.getBoolean(PREF_IS_LOGGED_IN, false);
    }
    
    /**
     * Déconnecte l'utilisateur actuel
     */
    public static void logout() {
        prefs.remove(PREF_USER_ID);
        prefs.remove(PREF_USER_EMAIL);
        prefs.remove(PREF_USER_ROLES);
        prefs.putBoolean(PREF_IS_LOGGED_IN, false);
        currentUser = null;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clearUserSession() {
        logout();
    }
} 