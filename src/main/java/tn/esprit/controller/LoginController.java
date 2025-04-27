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
import tn.esprit.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.scene.control.Alert;

public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox recaptchaCheckBox;

    @FXML
    private Label messageLabel;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @FXML
    public void initialize() {
        // Vérifier s'il y a une session active et la nettoyer
        if (SessionManager.isLoggedIn()) {
            SessionManager.clearUserSession();
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            messageLabel.setText("Format d'email invalide");
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
            LOGGER.info("Tentative de connexion pour l'utilisateur: " + email);
            
            String sql = "SELECT * FROM user WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    LOGGER.info("Utilisateur trouvé, vérification du mot de passe");
                    
                    // Pour le débogage, vérifiez si le mot de passe correspond exactement
                    if (password.equals(storedPassword)) {
                        LOGGER.info("Mot de passe correct, création de l'objet User");
                        
                        User user = new User(
                            rs.getLong("id"),
                            rs.getString("email"),
                            storedPassword,
                            rs.getString("roles")
                        );
                        
                        LOGGER.info("Rôle de l'utilisateur: " + user.getRoles());
                        SessionManager.saveUserSession(user);
                        
                        try {
                            openDashboardForUser(user);
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du dashboard: ", e);
                            messageLabel.setText("Erreur lors de l'ouverture du dashboard: " + e.getMessage());
                        }
                    } else {
                        LOGGER.warning("Mot de passe incorrect pour l'utilisateur: " + email);
                        messageLabel.setText("Email ou mot de passe incorrect");
                    }
                } else {
                    LOGGER.warning("Aucun utilisateur trouvé avec l'email: " + email);
                    messageLabel.setText("Email ou mot de passe incorrect");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors de la connexion: " + e.getMessage(), e);
            messageLabel.setText("Erreur de connexion à la base de données: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue lors de la connexion: " + e.getMessage(), e);
            messageLabel.setText("Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Fichier register.fxml introuvable");
            }
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inscription");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture de la page d'inscription", e);
            messageLabel.setText("Erreur lors de l'ouverture de la page d'inscription");
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reset-password.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réinitialisation du mot de passe");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture de la page de réinitialisation", e);
            showError("Erreur", "Impossible d'ouvrir la page de réinitialisation du mot de passe.");
        }
    }

    private void openDashboardForUser(User user) throws Exception {
        String roles = user.getRoles();
        if (roles.contains("ROLE_ADMIN")) {
            openAdminDashboard(user);
        } else if (roles.contains("ROLE_MEDECIN")) {
            openMedecinDashboard(user);
        } else if (roles.contains("ROLE_PATIENT")) {
            openPatientDashboard(user);
        } else {
            throw new IllegalStateException("Rôle utilisateur inconnu: " + roles);
        }
    }

    private void openAdminDashboard(User user) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Fichier admin-dashboard.fxml introuvable");
            }
            Parent root = loader.load();
            Scene currentScene = emailField.getScene();
            if (currentScene == null) {
                throw new IllegalStateException("Scene introuvable");
            }
            Stage stage = (Stage) currentScene.getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Admin");
            AdminDashboardController controller = loader.getController();
            controller.setUser(user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du dashboard admin", e);
            throw e;
        }
    }

    private void openMedecinDashboard(User user) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/medecin-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Fichier medecin-dashboard.fxml introuvable");
            }
            Parent root = loader.load();
            Scene currentScene = emailField.getScene();
            if (currentScene == null) {
                throw new IllegalStateException("Scene introuvable");
            }
            Stage stage = (Stage) currentScene.getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Médecin");
            MedecinDashboardController controller = loader.getController();
            controller.setUser(user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du dashboard médecin: " + e.getMessage(), e);
            throw new Exception("Impossible d'ouvrir le dashboard médecin: " + e.getMessage());
        }
    }

    private void openPatientDashboard(User user) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-dashboard.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("Fichier patient-dashboard.fxml introuvable");
            }
            Parent root = loader.load();
            Scene currentScene = emailField.getScene();
            if (currentScene == null) {
                throw new IllegalStateException("Scene introuvable");
            }
            Stage stage = (Stage) currentScene.getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard Patient");
            PatientDashboardController controller = loader.getController();
            controller.setUser(user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du dashboard patient", e);
            throw e;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}