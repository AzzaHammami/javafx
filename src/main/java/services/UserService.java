package services;

import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.MyDataBase;
import org.json.JSONArray;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private Connection conn;

    public UserService() {
        conn = MyDataBase.getInstance().getConnection();
    }

    public User authenticate(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(password, hashedPassword)) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setEmail(rs.getString("email"));
                        user.setName(rs.getString("name"));
                        // Convertir la chaîne JSON des rôles en JSONArray
                        String rolesStr = rs.getString("roles");
                        JSONArray roles = new JSONArray(rolesStr);
                        user.setRoles(roles);
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Authentication error", e);
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setName(rs.getString("name"));
                String rolesStr = rs.getString("roles");
                JSONArray roles = new JSONArray(rolesStr);
                user.setRoles(roles);
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all users", e);
        }
        return users;
    }

    public User getUserById(int id) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setName(rs.getString("name"));
                    String rolesStr = rs.getString("roles");
                    JSONArray roles = new JSONArray(rolesStr);
                    user.setRoles(roles);
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user by id", e);
        }
        return null;
    }
}