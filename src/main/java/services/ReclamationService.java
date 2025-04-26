package services;

import interfaces.IReclamation;
import models.Reclamation;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService implements IReclamation {

    private Connection connection;

    public ReclamationService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reclamation r) {
        // Correction : ajout du champ user_id dans l'insertion
        String sql = "INSERT INTO reclamation (sujet, description, statut, date_reclamation, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getSujet());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatut());
            ps.setDate(4, Date.valueOf(r.getDateReclamation()));
            ps.setInt(5, r.getUserId());
            ps.executeUpdate();
            System.out.println("Reclamation ajoutée !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la réclamation : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Reclamation r) {
        String sql = "UPDATE reclamation SET sujet = ?, description = ?, statut = ?, date_reclamation = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, r.getSujet());
            ps.setString(2, r.getDescription());
            ps.setString(3, r.getStatut());
            ps.setDate(4, Date.valueOf(r.getDateReclamation()));
            ps.setInt(5, r.getId());
            ps.executeUpdate();
            System.out.println("Reclamation modifiée !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM reclamation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Reclamation supprimée !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<Reclamation> getAll() {

        List<Reclamation> list = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Lors de la récupération des réclamations depuis la base, il faut remplir le champ userId
                // Exemple pour la méthode getAll() et toute méthode qui crée un objet Reclamation
                // rs.getInt("user_id")
                // new Reclamation(id, sujet, description, statut, dateReclamation, userId)
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getString("sujet"),
                        rs.getString("description"),
                        rs.getString("statut"),
                        rs.getDate("date_reclamation").toLocalDate(),
                        rs.getInt("user_id")
                );
                list.add(r);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return list;
    }

    @Override
    public Reclamation getById(int id) {
        String sql = "SELECT * FROM reclamation WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Lors de la récupération des réclamations depuis la base, il faut remplir le champ userId
                // Exemple pour la méthode getAll() et toute méthode qui crée un objet Reclamation
                // rs.getInt("user_id")
                // new Reclamation(id, sujet, description, statut, dateReclamation, userId)
                return new Reclamation(
                        rs.getInt("id"),
                        rs.getString("sujet"),
                        rs.getString("description"),
                        rs.getString("statut"),
                        rs.getDate("date_reclamation").toLocalDate(),
                        rs.getInt("user_id")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par ID : " + e.getMessage());
        }
        return null;
    }

    // Ces deux méthodes ne sont plus nécessaires (version liste)

}
