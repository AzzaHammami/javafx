package com.example.rendez_vous.services;

import com.example.rendez_vous.models.Rating;
import com.example.rendez_vous.utils.MySQLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRating {
    private final List<Rating> ratings = new ArrayList<>();

    // Ajouter une note
    public void addRating(Rating rating) {
        String sql = "INSERT INTO rating (medecin_id, patient_id, value, commentaire, date_rating) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating.getMedecinId());
            stmt.setInt(2, rating.getPatientId());
            stmt.setInt(3, rating.getValue());
            stmt.setString(4, rating.getCommentaire());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer toutes les notes d'un médecin
    public List<Rating> getRatingsForMedecin(int medecinId) {
        List<Rating> result = new ArrayList<>();
        String sql = "SELECT * FROM rating WHERE medecin_id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medecinId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Rating rating = new Rating(
                    rs.getInt("medecin_id"),
                    rs.getInt("patient_id"),
                    rs.getInt("value"),
                    rs.getString("commentaire")
                );
                result.add(rating);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Calculer la moyenne des notes d'un médecin
    public double getMoyenneRatingMedecin(int medecinId) {
        List<Rating> medecinRatings = getRatingsForMedecin(medecinId);
        if (medecinRatings.isEmpty()) return 0.0;
        int sum = 0;
        for (Rating r : medecinRatings) {
            sum += r.getValue();
        }
        return (double) sum / medecinRatings.size();
    }

    // Récupérer toutes les notes d'un patient
    public List<Rating> getRatingsByPatient(int patientId) {
        List<Rating> result = new ArrayList<>();
        for (Rating r : ratings) {
            if (r.getPatientId() == patientId) {
                result.add(r);
            }
        }
        return result;
    }

    // Ajoute ou met à jour une note pour un médecin/patient
    public void addOrUpdateRating(Rating rating) {
        String selectSql = "SELECT COUNT(*) FROM rating WHERE medecin_id=? AND patient_id=?";
        String insertSql = "INSERT INTO rating (medecin_id, patient_id, value, commentaire, date_rating) VALUES (?, ?, ?, ?, NOW())";
        String updateSql = "UPDATE rating SET value=?, commentaire=?, date_rating=NOW() WHERE medecin_id=? AND patient_id=?";
        try (Connection conn = MySQLConnection.getConnection()) {
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, rating.getMedecinId());
                selectStmt.setInt(2, rating.getPatientId());
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Déjà noté : UPDATE
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, rating.getValue());
                        updateStmt.setString(2, rating.getCommentaire());
                        updateStmt.setInt(3, rating.getMedecinId());
                        updateStmt.setInt(4, rating.getPatientId());
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Pas encore noté : INSERT
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, rating.getMedecinId());
                        insertStmt.setInt(2, rating.getPatientId());
                        insertStmt.setInt(3, rating.getValue());
                        insertStmt.setString(4, rating.getCommentaire());
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
