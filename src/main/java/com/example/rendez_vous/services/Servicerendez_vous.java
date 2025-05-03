package com.example.rendez_vous.services;

import com.example.rendez_vous.interfaces.IRendez_Vous;
import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.HashMap;

public class Servicerendez_vous implements IRendez_Vous<RendezVous> {
    Connection connection;

    public Servicerendez_vous() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public Boolean ajouterRendezVous(RendezVous rv) {
        try {
            String sql = "INSERT INTO rendez_vous (date, statut, motif, patient_id, medecin_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);

            pstmt.setTimestamp(1, Timestamp.valueOf(rv.getDate()));
            pstmt.setString(2, rv.getStatut() != null ? rv.getStatut() : "En attente");
            pstmt.setString(3, rv.getMotif());
            pstmt.setInt(4, rv.getPatientId());
            pstmt.setInt(5, rv.getMedecinId());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Rendez-vous ajouté");
            return rowsAffected > 0;  // Return Boolean instead of always true

        } catch (SQLException e) {
            System.err.println("❌ Erreur d'ajout: " + e.getMessage());
            return false;  // Or consider throwing an exception
        }
    }

    @Override
    public Boolean modifierRendezVous(RendezVous rv) {
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
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("❌ Erreur de modification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean supprimerRendezVous(int id) {
        try {
            String sql = "DELETE FROM rendez_vous WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);

            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                System.out.println("✅ Rendez-vous supprimé");
                return true;
            }
            return false;

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

    // Nouvelle méthode : obtenir le prochain rendez-vous pour un patient
    public RendezVous getNextAppointmentForPatient(int patientId) {
        try {
            String sql = "SELECT * FROM rendez_vous WHERE patient_id = ? AND date > NOW() ORDER BY date ASC LIMIT 1";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, patientId);
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
            System.err.println("❌ Erreur lors de la récupération du prochain rendez-vous: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    // --- STATISTIQUES ---
    // 1. Nombre total de rendez-vous
    public int countAllRendezVous() throws SQLException {
        String sql = "SELECT COUNT(*) FROM rendez_vous";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // 2. Nombre de rendez-vous par statut
    public int countRendezVousByStatut(String statut) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rendez_vous WHERE statut = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // 3. Rendez-vous par plage horaire (exemple: matin, après-midi, soir)
    public int countRendezVousByPlageHoraire(String plage) throws SQLException {
        // Plages possibles : "Matin", "Après-midi", "Soir"
        String condition = "";
        switch (plage) {
            case "Matin":
                condition = "HOUR(date) >= 6 AND HOUR(date) < 12";
                break;
            case "Après-midi":
                condition = "HOUR(date) >= 12 AND HOUR(date) < 18";
                break;
            case "Soir":
                condition = "HOUR(date) >= 18 OR HOUR(date) < 6";
                break;
            default:
                throw new IllegalArgumentException("Plage horaire inconnue: " + plage);
        }
        String sql = "SELECT COUNT(*) FROM rendez_vous WHERE " + condition;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // Rendez-vous par mois (pour LineChart)
    public Map<String, Integer> countRendezVousByMonth(int year) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT MONTH(date) as m, COUNT(*) as c FROM rendez_vous WHERE YEAR(date) = ? GROUP BY m ORDER BY m";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                for (int i = 1; i <= 12; i++) result.put(String.format("%02d", i), 0);
                while (rs.next()) {
                    String month = String.format("%02d", rs.getInt("m"));
                    result.put(month, rs.getInt("c"));
                }
            }
        }
        return result;
    }

    // Statut par plage horaire (pour BarChart groupé)
    public Map<String, Map<String, Integer>> countRdvByStatutAndPlage() throws SQLException {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        String[] plages = {"Matin", "Après-midi", "Soir"};
        String[] statuts = {"Confirmé", "Annulé", "En attente"};
        for (String plage : plages) {
            result.put(plage, new HashMap<>());
            for (String statut : statuts) result.get(plage).put(statut, 0);
        }
        String sql = "SELECT " +
            "CASE WHEN HOUR(date) >= 6 AND HOUR(date) < 12 THEN 'Matin' " +
            "     WHEN HOUR(date) >= 12 AND HOUR(date) < 18 THEN 'Après-midi' " +
            "     ELSE 'Soir' END as plage, statut, COUNT(*) as c " +
            "FROM rendez_vous GROUP BY plage, statut";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String plage = rs.getString("plage");
                String statut = rs.getString("statut");
                int count = rs.getInt("c");
                result.get(plage).put(statut, count);
            }
        }
        return result;
    }

    // Version filtrée : total rendez-vous
    public int countAllRendezVousFiltered(Integer year, String month, LocalDate start, LocalDate end) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rendez_vous WHERE 1=1");
        if (year != null) sql.append(" AND YEAR(date) = ").append(year);
        if (month != null && !month.equals("Tous")) sql.append(" AND MONTH(date) = ").append(Integer.parseInt(month));
        if (start != null) sql.append(" AND date >= '").append(start).append("'");
        if (end != null) sql.append(" AND date <= '").append(end).append("'");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // Version filtrée : par statut
    public int countRendezVousByStatutFiltered(String statut, Integer year, String month, LocalDate start, LocalDate end) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rendez_vous WHERE statut = '").append(statut).append("'");
        if (year != null) sql.append(" AND YEAR(date) = ").append(year);
        if (month != null && !month.equals("Tous")) sql.append(" AND MONTH(date) = ").append(Integer.parseInt(month));
        if (start != null) sql.append(" AND date >= '").append(start).append("'");
        if (end != null) sql.append(" AND date <= '").append(end).append("'");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // Version filtrée : par plage horaire
    public int countRendezVousByPlageHoraireFiltered(String plage, Integer year, String month, LocalDate start, LocalDate end) throws SQLException {
        String condition = "";
        switch (plage) {
            case "Matin":
                condition = "HOUR(date) >= 6 AND HOUR(date) < 12";
                break;
            case "Après-midi":
                condition = "HOUR(date) >= 12 AND HOUR(date) < 18";
                break;
            case "Soir":
                condition = "HOUR(date) >= 18 OR HOUR(date) < 6";
                break;
            default:
                throw new IllegalArgumentException("Plage horaire inconnue: " + plage);
        }
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rendez_vous WHERE ").append(condition);
        if (year != null) sql.append(" AND YEAR(date) = ").append(year);
        if (month != null && !month.equals("Tous")) sql.append(" AND MONTH(date) = ").append(Integer.parseInt(month));
        if (start != null) sql.append(" AND date >= '").append(start).append("'");
        if (end != null) sql.append(" AND date <= '").append(end).append("'");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // Version filtrée : LineChart par mois
    public Map<String, Integer> countRendezVousByMonthFiltered(int year, String month, LocalDate start, LocalDate end) throws SQLException {
        Map<String, Integer> result = new LinkedHashMap<>();
        StringBuilder sql = new StringBuilder("SELECT MONTH(date) as m, COUNT(*) as c FROM rendez_vous WHERE YEAR(date) = ").append(year);
        if (month != null && !month.equals("Tous")) sql.append(" AND MONTH(date) = ").append(Integer.parseInt(month));
        if (start != null) sql.append(" AND date >= '").append(start).append("'");
        if (end != null) sql.append(" AND date <= '").append(end).append("'");
        sql.append(" GROUP BY m ORDER BY m");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            for (int i = 1; i <= 12; i++) result.put(String.format("%02d", i), 0);
            while (rs.next()) {
                String m = String.format("%02d", rs.getInt("m"));
                result.put(m, rs.getInt("c"));
            }
        }
        return result;
    }

    // Version filtrée : BarChart groupé
    public Map<String, Map<String, Integer>> countRdvByStatutAndPlageFiltered(Integer year, String month, LocalDate start, LocalDate end) throws SQLException {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        String[] plages = {"Matin", "Après-midi", "Soir"};
        String[] statuts = {"Confirmé", "Annulé", "En attente"};
        for (String plage : plages) {
            result.put(plage, new HashMap<>());
            for (String statut : statuts) result.get(plage).put(statut, 0);
        }
        StringBuilder sql = new StringBuilder("SELECT " +
            "CASE WHEN HOUR(date) >= 6 AND HOUR(date) < 12 THEN 'Matin' " +
            "     WHEN HOUR(date) >= 12 AND HOUR(date) < 18 THEN 'Après-midi' " +
            "     ELSE 'Soir' END as plage, statut, COUNT(*) as c FROM rendez_vous WHERE 1=1");
        if (year != null) sql.append(" AND YEAR(date) = ").append(year);
        if (month != null && !month.equals("Tous")) sql.append(" AND MONTH(date) = ").append(Integer.parseInt(month));
        if (start != null) sql.append(" AND date >= '").append(start).append("'");
        if (end != null) sql.append(" AND date <= '").append(end).append("'");
        sql.append(" GROUP BY plage, statut");
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql.toString())) {
            while (rs.next()) {
                String plage = rs.getString("plage");
                String statut = rs.getString("statut");
                int count = rs.getInt("c");
                result.get(plage).put(statut, count);
            }
        }
        return result;
    }
}