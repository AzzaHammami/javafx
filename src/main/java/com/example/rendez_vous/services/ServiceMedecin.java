package com.example.rendez_vous.services;

import com.example.rendez_vous.models.Medecin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMedecin {
    private Connection connection;

    public ServiceMedecin() {
        try {
            // Adapter selon ta config de connexion
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rendezvous", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean ajouterMedecin(String nom) {
        String sql = "INSERT INTO medecin (nom) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Medecin> getAllMedecins() {
        List<Medecin> medecins = new ArrayList<>();
        String sql = "SELECT id, nom FROM medecin";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                medecins.add(new Medecin(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medecins;
    }

    public Medecin getMedecinByNom(String nom) {
        String sql = "SELECT id, nom FROM medecin WHERE nom = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Medecin(rs.getInt("id"), rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Medecin getMedecinById(int id) {
        String sql = "SELECT id, nom FROM medecin WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Medecin(rs.getInt("id"), rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
