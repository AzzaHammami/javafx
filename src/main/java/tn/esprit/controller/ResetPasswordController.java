package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.config.DatabaseConfig;
import tn.esprit.util.EmailService;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.SQLException;

public class ResetPasswordController {
    private static final Logger LOGGER = Logger.getLogger(ResetPasswordController.class.getName());

    @FXML private VBox emailStep;
    @FXML private VBox codeStep;
    @FXML private VBox passwordStep;
    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private String resetCode;
    private String userEmail;

    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        LOGGER.info("Tentative de réinitialisation de mot de passe pour : " + email);
        
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer votre adresse email.");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT id FROM user WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                LOGGER.info("Vérification de l'existence de l'email dans la base de données");
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    try {
                        // Générer un code à 6 chiffres
                        resetCode = String.format("%06d", new Random().nextInt(999999));
                        userEmail = email;
                        LOGGER.info("Code de réinitialisation généré : " + resetCode);

                        // Envoyer le code par email via Mailtrap
                        LOGGER.info("Envoi du code via Mailtrap à : " + email);
                        EmailService.sendPasswordResetCode(email, resetCode);

                        // Passer à l'étape suivante
                        emailStep.setVisible(false);
                        codeStep.setVisible(true);
                        showAlert(Alert.AlertType.INFORMATION, "Code envoyé", 
                                "Un code de réinitialisation a été envoyé à votre adresse email via Mailtrap.\n" +
                                "Vous pouvez voir le code dans les logs de l'application.");
                    } catch (RuntimeException e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi de l'email via Mailtrap", e);
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Impossible d'envoyer le code via Mailtrap. Veuillez réessayer plus tard.\n" +
                                "Erreur : " + e.getMessage());
                    }
                } else {
                    LOGGER.warning("Tentative de réinitialisation pour un email inexistant : " + email);
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Aucun compte n'est associé à cette adresse email.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification de l'email", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de la vérification de l'email.");
        }
    }

    @FXML
    private void handleVerifyCode() {
        String enteredCode = codeField.getText().trim();
        if (enteredCode.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer le code reçu.");
            return;
        }

        if (enteredCode.equals(resetCode)) {
            codeStep.setVisible(false);
            passwordStep.setVisible(true);
        } else {
            showAlert(Alert.AlertType.ERROR, "Code incorrect", 
                    "Le code entré est incorrect. Veuillez réessayer.");
        }
    }

    @FXML
    private void handleResetPassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Veuillez remplir tous les champs.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Les mots de passe ne correspondent pas.");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE user SET password = ? WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                pstmt.setString(1, hashedPassword);
                pstmt.setString(2, userEmail);
                
                int updated = pstmt.executeUpdate();
                if (updated > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", 
                            "Votre mot de passe a été réinitialisé avec succès.");
                    handleBackToLogin();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Impossible de mettre à jour le mot de passe.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la réinitialisation du mot de passe", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Une erreur est survenue lors de la réinitialisation du mot de passe.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du retour à la page de connexion", e);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 