package tn.esprit.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private CheckBox recaptchaCheckBox;
    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        // Initialiser les rôles disponibles
        roleComboBox.setItems(FXCollections.observableArrayList(
            "ROLE_ADMIN",
            "ROLE_MEDECIN",
            "ROLE_PATIENT"
        ));
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            messageLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        // Vérifier que le mot de passe contient au moins 8 caractères
        if (password.length() < 8) {
            messageLabel.setText("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Les mots de passe ne correspondent pas");
            return;
        }

        // Vérifier que le reCAPTCHA est coché
        if (!recaptchaCheckBox.isSelected()) {
            messageLabel.setText("Veuillez confirmer que vous n'êtes pas un robot");
            return;
        }

        // Vérifier si l'email existe déjà
        try (Connection conn = DatabaseConfig.getConnection()) {
            String checkSql = "SELECT * FROM user WHERE email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                messageLabel.setText("Cet email est déjà utilisé");
                return;
            }

            // Insérer le nouvel utilisateur
            String insertSql = "INSERT INTO user (email, password, roles) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, email);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            insertStmt.executeUpdate();

            // Afficher un message de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Inscription réussie");
            alert.setHeaderText(null);
            alert.setContentText("Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.");
            alert.showAndWait();

            // Retourner à la page de connexion
            handleLogin();
        } catch (SQLException e) {
            messageLabel.setText("Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/login.fxml"));
            if (loader.getLocation() == null) {
                throw new Exception("Impossible de trouver le fichier login.fxml");
            }
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
        } catch (Exception e) {
            messageLabel.setText("Erreur lors du retour à la page de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 