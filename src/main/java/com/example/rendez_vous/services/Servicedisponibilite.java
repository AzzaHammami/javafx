package com.example.rendez_vous.services;

import com.example.rendez_vous.models.Disponibilite;
import com.example.rendez_vous.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Servicedisponibilite {

    private Connection conn;

    public Servicedisponibilite() {
        conn = MyDataBase.getInstance().getConnection();
    }

    public boolean ajouterDisponibilite(Disponibilite d) throws SQLException {
        try {
            // Vérifier la connexion
            if (conn == null || conn.isClosed()) {
                conn = MyDataBase.getInstance().getConnection();
            }

            // Set auto-commit to false to control transaction
            conn.setAutoCommit(false);

            // Vérifier la cohérence des dates
            if (d.getDateDebut().isAfter(d.getDateFin())) {
                throw new SQLException("La date de début doit être antérieure à la date de fin");
            }

            String sql = "INSERT INTO disponibilite (medecin_id, date_debut, date_fin, statut) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, d.getMedecinId());
                ps.setTimestamp(2, Timestamp.valueOf(d.getDateDebut()));
                ps.setTimestamp(3, Timestamp.valueOf(d.getDateFin()));
                ps.setString(4, d.getStatut() != null ? d.getStatut() : "Disponible");

                int rowsAffected = ps.executeUpdate();

                System.out.println("Tentative d'insertion - Médecin ID: " + d.getMedecinId() +
                        ", Date début: " + d.getDateDebut() +
                        ", Date fin: " + d.getDateFin() +
                        ", Statut: " + d.getStatut());
                System.out.println("Résultat: " + rowsAffected + " ligne(s) affectée(s)");

                if (rowsAffected > 0) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        d.setId(rs.getInt(1));
                        System.out.println("ID généré: " + d.getId());
                    }
                    conn.commit(); // Explicit commit
                    return true;
                }
                conn.rollback(); // Rollback if no rows affected
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            System.err.println("Erreur SQL lors de l'ajout de disponibilité:");
            e.printStackTrace();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // Reset auto-commit
            }
        }
    }

    public List<Disponibilite> getAllDisponibilites() throws SQLException {
        List<Disponibilite> list = new ArrayList<>();
        String sql = "SELECT * FROM disponibilite ORDER BY date_debut DESC";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(mapResultSetToDisponibilite(rs));
            }
        }
        
        return list;
    }

    public List<Disponibilite> getDisponibilitesByMedecin(int medecinId) throws SQLException {
        List<Disponibilite> list = new ArrayList<>();
        String sql = "SELECT * FROM disponibilite WHERE medecin_id = ? ORDER BY date_debut DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDisponibilite(rs));
                }
            }
        }
        
        return list;
    }

    private Disponibilite mapResultSetToDisponibilite(ResultSet rs) throws SQLException {
        Disponibilite d = new Disponibilite();
        d.setId(rs.getInt("id"));
        d.setMedecinId(rs.getInt("medecin_id"));
        d.setDateDebut(rs.getTimestamp("date_debut").toLocalDateTime());
        d.setDateFin(rs.getTimestamp("date_fin").toLocalDateTime());
        d.setStatut(rs.getString("statut"));
        return d;
    }

    public void supprimerDisponibilite(int id) throws SQLException {
        String sql = "DELETE FROM disponibilite WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucune disponibilité trouvée avec l'ID: " + id);
            }
        }
    }

    public void modifierDisponibilite(Disponibilite d) throws SQLException {
        if (d.getDateDebut().isAfter(d.getDateFin())) {
            throw new SQLException("La date de début doit être antérieure à la date de fin");
        }

        String sql = "UPDATE disponibilite SET medecin_id = ?, date_debut = ?, date_fin = ?, statut = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, d.getMedecinId());
            ps.setTimestamp(2, Timestamp.valueOf(d.getDateDebut()));
            ps.setTimestamp(3, Timestamp.valueOf(d.getDateFin()));
            ps.setString(4, d.getStatut());
            ps.setInt(5, d.getId());
            
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Aucune disponibilité trouvée avec l'ID: " + d.getId());
            }
        }
    }
    public Disponibilite getDisponibiliteById(int id) throws SQLException {
        String sql = "SELECT * FROM disponibilite WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDisponibilite(rs);
                }
            }
        }
        return null;
    }

    // Statistiques sur les disponibilités : nombre total, par médecin, etc.
    public int countAllDisponibilites() throws SQLException {
        String sql = "SELECT COUNT(*) FROM disponibilite";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countDisponibilitesByMedecin(int medecinId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM disponibilite WHERE medecin_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medecinId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
