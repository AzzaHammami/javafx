package com.example.rendez_vous.services;

import com.example.rendez_vous.models.User;
import com.example.rendez_vous.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {
    private Connection cnx;

    public ServiceUser() {
        cnx = MyDataBase.getInstance().getConnection();
    }

    public void ajouter(User user) throws SQLException {
        String req = "INSERT INTO user (email, name, roles, password, image_url, specialite) VALUES (?, ?, '[\"medecin\"]', '', ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, user.getEmail());
        pst.setString(2, user.getName());
        pst.setString(3, user.getImageUrl());
        pst.setString(4, user.getSpecialite());
        pst.executeUpdate();
    }

    public List<User> getAllMedecins() {
        List<User> medecins = new ArrayList<>();
        String req = "SELECT * FROM user WHERE roles LIKE '%medecin%'";
        try (PreparedStatement pst = cnx.prepareStatement(req); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                    rs.getInt("id"),
                    rs.getString("email"),
                    rs.getString("name"),
                    java.util.Collections.singletonList("medecin"),
                    rs.getString("password"),
                    rs.getString("image_url"),
                    rs.getString("specialite"),
                    null // adresse supprimée, car tu ne veux pas gérer la localisation
                );
                medecins.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medecins;
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM user WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public void modifier(User user) throws SQLException {
        String req = "UPDATE user SET email = ?, name = ?, image_url = ?, specialite = ? WHERE id = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, user.getEmail());
        pst.setString(2, user.getName());
        pst.setString(3, user.getImageUrl());
        pst.setString(4, user.getSpecialite());
        pst.setInt(5, user.getId());
        pst.executeUpdate();
    }
}
