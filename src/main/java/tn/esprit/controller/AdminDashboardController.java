package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDashboardController {

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalDoctorsLabel;
    @FXML
    private Label totalPatientsLabel;

    private User currentUser;

    @FXML
    public void initialize() {
        updateStatistics();
    }

    public void updateStatistics() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Total users
            String totalQuery = "SELECT COUNT(*) FROM user";
            PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                totalUsersLabel.setText(String.valueOf(totalRs.getInt(1)));
            }

            // Total doctors
            String doctorsQuery = "SELECT COUNT(*) FROM user WHERE roles = 'ROLE_MEDECIN'";
            PreparedStatement doctorsStmt = conn.prepareStatement(doctorsQuery);
            ResultSet doctorsRs = doctorsStmt.executeQuery();
            if (doctorsRs.next()) {
                totalDoctorsLabel.setText(String.valueOf(doctorsRs.getInt(1)));
            }

            // Total patients
            String patientsQuery = "SELECT COUNT(*) FROM user WHERE roles = 'ROLE_PATIENT'";
            PreparedStatement patientsStmt = conn.prepareStatement(patientsQuery);
            ResultSet patientsRs = patientsStmt.executeQuery();
            if (patientsRs.next()) {
                totalPatientsLabel.setText(String.valueOf(patientsRs.getInt(1)));
            }
        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour des statistiques");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDashboard() {
        // Déjà sur le tableau de bord
    }

    @FXML
    private void handleUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-list.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Utilisateurs");
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture de la liste des utilisateurs");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            showError("Erreur lors de la déconnexion");
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
    }
} 