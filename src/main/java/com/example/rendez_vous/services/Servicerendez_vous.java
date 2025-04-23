package com.example.rendez_vous.services;

import com.example.rendez_vous.interfaces.IRendez_Vous;
import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Servicerendez_vous implements IRendez_Vous<RendezVous> {
    Connection connection;

    public Servicerendez_vous() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouterRendezVous(RendezVous rv) {
        try {
            String sql = "INSERT INTO rendez_vous (date, statut, motif, patient_id, medecin_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            pstmt.setTimestamp(1, Timestamp.valueOf(rv.getDate()));
            pstmt.setString(2, rv.getStatut() != null ? rv.getStatut() : "En attente");
            pstmt.setString(3, rv.getMotif());
            pstmt.setInt(4, rv.getPatientId());
            pstmt.setInt(5, rv.getMedecinId());
            
            pstmt.executeUpdate();
            System.out.println("✅ Rendez-vous ajouté");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur d'ajout: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modifierRendezVous(RendezVous rv) {
        try {
            String sql = "UPDATE rendez_vous SET date = ?, statut = ?, motif = ?, patient_id = ?, medecin_id = ? WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            pstmt.setTimestamp(1, Timestamp.valueOf(rv.getDate()));
            pstmt.setString(2, rv.getStatut());
            pstmt.setString(3, rv.getMotif());
            pstmt.setInt(4, rv.getPatientId());
            pstmt.setInt(5, rv.getMedecinId());
            pstmt.setInt(6, rv.getId());
            
            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                System.out.println("✅ Rendez-vous modifié");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur de modification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void supprimerRendezVous(int id) {
        try {
            String sql = "DELETE FROM rendez_vous WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("✅ Rendez-vous supprimé");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur de suppression: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<RendezVous> listerRendezVous() {
        List<RendezVous> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM rendez_vous ORDER BY date DESC";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                RendezVous rv = new RendezVous();
                rv.setId(rs.getInt("id"));
                rv.setDate(rs.getTimestamp("date").toLocalDateTime());
                rv.setStatut(rs.getString("statut"));
                rv.setMotif(rs.getString("motif"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                
                Timestamp dateCreation = rs.getTimestamp("date_creation");
                if (dateCreation != null) {
                    rv.setDateCreation(dateCreation.toLocalDateTime());
                }
                
                list.add(rv);
            }
            System.out.println("✅ " + list.size() + " rendez-vous trouvés");
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur de lecture: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public RendezVous getRendezVousById(int id) {
        try {
            String sql = "SELECT * FROM rendez_vous WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                RendezVous rv = new RendezVous();
                rv.setId(rs.getInt("id"));
                rv.setDate(rs.getTimestamp("date").toLocalDateTime());
                rv.setStatut(rs.getString("statut"));
                rv.setMotif(rs.getString("motif"));
                rv.setPatientId(rs.getInt("patient_id"));
                rv.setMedecinId(rs.getInt("medecin_id"));
                
                Timestamp dateCreation = rs.getTimestamp("date_creation");
                if (dateCreation != null) {
                    rv.setDateCreation(dateCreation.toLocalDateTime());
                }
                
                return rv;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erreur de lecture: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    // Retourne la liste des heures déjà prises pour un médecin donné à une date donnée
    public List<LocalTime> getTakenTimesForDate(LocalDate date, int medecinId) {
        List<LocalTime> taken = new ArrayList<>();
        if (date == null) {
            // Ne rien logger si la date est nulle
            return taken;
        }
        try {
            String sql = "SELECT date FROM rendez_vous WHERE DATE(date) = ? AND medecin_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setInt(2, medecinId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                taken.add(rs.getTimestamp("date").toLocalDateTime().toLocalTime());
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getTakenTimesForDate: " + e.getMessage());
        }
        return taken;
    }
    // Version précédente conservée pour compatibilité
    public List<LocalTime> getTakenTimesForDate(LocalDate date) {
        return getTakenTimesForDate(date, 0);
    }
}
