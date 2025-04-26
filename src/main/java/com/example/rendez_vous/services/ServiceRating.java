package com.example.rendez_vous.services;

import com.example.rendez_vous.models.Rating;
import com.example.rendez_vous.utils.MyDataBase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceRating {
    private Connection cnx;

    public ServiceRating() {
        // À adapter selon ta méthode de connexion
        cnx = MyDataBase.getInstance().getConnection();
    }

    public boolean ajouterRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (object_id, object_type, rating_value, comment, user_id, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, rating.getObjectId());
        ps.setString(2, rating.getObjectType());
        ps.setInt(3, rating.getRatingValue());
        ps.setString(4, rating.getComment());
        ps.setInt(5, rating.getUserId());
        ps.setTimestamp(6, Timestamp.valueOf(rating.getTimestamp()));
        return ps.executeUpdate() > 0;
    }

    public List<Rating> getRatingsForMedecin(int medecinId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE object_id = ? AND object_type = 'medecin' ORDER BY timestamp DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, medecinId);
        ResultSet rs = ps.executeQuery();
        List<Rating> ratings = new ArrayList<>();
        while (rs.next()) {
            Rating r = new Rating(
                rs.getInt("id"),
                rs.getInt("object_id"),
                rs.getString("object_type"),
                rs.getInt("rating_value"),
                rs.getString("comment"),
                rs.getInt("user_id"),
                rs.getTimestamp("timestamp").toLocalDateTime()
            );
            ratings.add(r);
        }
        return ratings;
    }

    public double getMoyenneRatingMedecin(int medecinId) throws SQLException {
        String sql = "SELECT AVG(rating_value) FROM ratings WHERE object_id = ? AND object_type = 'medecin'";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, medecinId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0.0;
    }

    public boolean userHasRated(int medecinId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ratings WHERE object_id = ? AND object_type = 'medecin' AND user_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, medecinId);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}
