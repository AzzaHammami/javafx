package services;

import interfaces.IReponse;
import models.Reclamation;
import models.Reponse;
import utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReponseService implements IReponse {
    private Connection connection;

    public ReponseService() {
        connection = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Reponse reponse) {
        String sql = "INSERT INTO reponse (contenu, date_reponse, reclamation_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reponse.getContenu());
            ps.setDate(2, Date.valueOf(reponse.getDateReponse()));
            ps.setInt(3, reponse.getReclamation().getId());
            ps.executeUpdate();
            System.out.println("Réponse ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public List<Reponse> getAll() {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ReclamationService reclamationService = new ReclamationService();

            while (rs.next()) {
                int id = rs.getInt("id");
                String contenu = rs.getString("contenu");
                LocalDate dateReponse = rs.getDate("date_reponse").toLocalDate();
                int reclamationId = rs.getInt("reclamation_id");

                Reclamation reclamation = reclamationService.getById(reclamationId);

                Reponse reponse = new Reponse(id, contenu, dateReponse, reclamation);
                list.add(reponse);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses : " + e.getMessage());
        }

        return list;
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM reponse WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Réponse supprimée !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Reponse reponse) {
        String sql = "UPDATE reponse SET contenu = ?, date_reponse = ?, reclamation_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reponse.getContenu());
            ps.setDate(2, Date.valueOf(reponse.getDateReponse()));
            ps.setInt(3, reponse.getReclamation().getId());
            ps.setInt(4, reponse.getId());
            ps.executeUpdate();
            System.out.println("Réponse modifiée !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public Reponse getById(int id) {
        String sql = "SELECT * FROM reponse WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String contenu = rs.getString("contenu");
                LocalDate dateReponse = rs.getDate("date_reponse").toLocalDate();
                int reclamationId = rs.getInt("reclamation_id");

                ReclamationService reclamationService = new ReclamationService();
                Reclamation reclamation = reclamationService.getById(reclamationId);

                return new Reponse(id, contenu, dateReponse, reclamation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réponse : " + e.getMessage());
        }
        return null;
    }

    public List<Reponse> getReponsesByReclamation(int reclamationId) {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse WHERE reclamation_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, reclamationId);
            ResultSet rs = ps.executeQuery();

            ReclamationService reclamationService = new ReclamationService();
            Reclamation reclamation = reclamationService.getById(reclamationId);

            while (rs.next()) {
                int id = rs.getInt("id");
                String contenu = rs.getString("contenu");
                LocalDate dateReponse = rs.getDate("date_reponse").toLocalDate();

                Reponse reponse = new Reponse(id, contenu, dateReponse, reclamation);
                list.add(reponse);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réponses : " + e.getMessage());
        }

        return list;
    }
}
