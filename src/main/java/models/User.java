package models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class User {
    private int id;
    private String name;
    private String password;
    private String email;
    private JSONArray roles;
    private Timestamp createdAt;

    public enum Role {
        ADMIN, USER, DOCTOR
    }

    // Constructeurs
    public User() {}

    public User(String name, String password, String email, JSONArray roles) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.roles = roles;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONArray getRoles() {
        return roles;
    }

    public void setRoles(JSONArray roles) {
        this.roles = roles;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Ajout pour compatibilité avec UserService et MessengerController
    public List<String> getRolesList() {
        // Si vous souhaitez gérer plusieurs rôles, adaptez ce getter. Ici, on retourne le rôle unique sous forme de liste.
        List<String> rolesList = new ArrayList<>();
        if (roles != null) {
            for (int i = 0; i < roles.length(); i++) {
                rolesList.add(roles.getString(i));
            }
        }
        return rolesList;
    }
    public void setRolesList(List<String> rolesList) {
        // Si vous souhaitez gérer plusieurs rôles, adaptez ce setter. Ici, on prend le premier rôle de la liste.
        if (rolesList != null && !rolesList.isEmpty()) {
            try {
                roles = new JSONArray(rolesList);
            } catch (IllegalArgumentException e) {
                // Rôle inconnu, ignorer ou logger
            }
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", createdAt=" + createdAt +
                '}';
    }
} 