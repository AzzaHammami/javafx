package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.MyDataBase;

public class UserFcmTokenService {
    private Connection conn = MyDataBase.getInstance().getConnection();

    // Ajouter un token pour un utilisateur
    public void addToken(int userId, String fcmToken) {
        String query = "INSERT IGNORE INTO user_fcm_token (user_id, fcm_token) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, fcmToken);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Supprimer un token (ex: à la déconnexion)
    public void removeToken(int userId, String fcmToken) {
        String query = "DELETE FROM user_fcm_token WHERE user_id = ? AND fcm_token = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, fcmToken);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer tous les tokens d'un utilisateur
    public List<String> getTokensByUserId(int userId) {
        List<String> tokens = new ArrayList<>();
        String query = "SELECT fcm_token FROM user_fcm_token WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tokens.add(rs.getString("fcm_token"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tokens;
    }
}
