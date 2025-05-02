package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import tn.esprit.config.DatabaseConfig;
import tn.esprit.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox recaptchaCheckBox;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        if (password.length() < 8) {
            messageLabel.setText("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        if (!recaptchaCheckBox.isSelected()) {
            messageLabel.setText("Veuillez confirmer que vous n'êtes pas un robot");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("roles")
                );
                
                String roles = user.getRoles();
                try {
                    if (roles.contains("ROLE_ADMIN")) {
                        openAdminDashboard(user);
                    } else if (roles.contains("ROLE_MEDECIN")) {
                        openMedecinDashboard(user);
                    } else if (roles.contains("ROLE_PATIENT")) {
                        openPatientDashboard(user);
                    }
                } catch (Exception e) {
                    messageLabel.setText("Erreur lors de l'ouverture du dashboard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                messageLabel.setText("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            messageLabel.setText("Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/register.fxml"));
            if (loader.getLocation() == null) {
                throw new Exception("Impossible de trouver le fichier register.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inscription");
        } catch (Exception e) {
            messageLabel.setText("Erreur lors de l'ouverture de la page d'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openAdminDashboard(User user) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/admin-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new Exception("Impossible de trouver le fichier admin-dashboard.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Admin");

            AdminDashboardController controller = loader.getController();
            controller.setUser(user);
        } catch (Exception e) {
            messageLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void openMedecinDashboard(User user) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/medecin-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new Exception("Impossible de trouver le fichier medecin-dashboard.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Médecin");

            MedecinDashboardController controller = loader.getController();
            controller.setUser(user);
        } catch (Exception e) {
            messageLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void openPatientDashboard(User user) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/patient-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new Exception("Impossible de trouver le fichier patient-dashboard.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Patient");

            PatientDashboardController controller = loader.getController();
            controller.setUser(user);
        } catch (Exception e) {
            messageLabel.setText("Erreur: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 