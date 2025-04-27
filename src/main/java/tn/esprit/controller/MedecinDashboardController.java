package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;
import tn.esprit.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

public class MedecinDashboardController {
    private static final Logger LOGGER = Logger.getLogger(MedecinDashboardController.class.getName());

    @FXML
    private HBox headerBox;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label todayAppointmentsLabel;
    @FXML
    private Label monthlyConsultationsLabel;
    @FXML
    private FlowPane appointmentsContainer;
    @FXML
    private Label nameLabel;
    @FXML
    private Label specialityLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label rppsLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label phoneLabel;

    private User currentUser;
    private Stage currentStage;

    @FXML
    public void initialize() {
        try {
            LOGGER.info("Initialisation du dashboard médecin");
            loadStatistics();
            // Vérifier si un utilisateur est déjà en session
            User sessionUser = SessionManager.getCurrentUser();
            if (sessionUser != null) {
                LOGGER.info("Utilisateur trouvé en session: " + sessionUser.getEmail());
                setUser(sessionUser);
            } else {
                LOGGER.warning("Aucun utilisateur en session");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'initialisation du dashboard", e);
        }
    }

    public void setStage(Stage stage) {
        this.currentStage = stage;
    }

    private void loadStatistics() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                LOGGER.severe("La connexion à la base de données est null");
                return;
            }
            
            // Nombre total de patients
            String patientsQuery = "SELECT COUNT(*) as total FROM user WHERE roles = 'ROLE_PATIENT'";
            try (PreparedStatement patientsStmt = conn.prepareStatement(patientsQuery)) {
                ResultSet patientsRs = patientsStmt.executeQuery();
                if (patientsRs.next()) {
                    totalPatientsLabel.setText(String.valueOf(patientsRs.getInt("total")));
                }
            }
            
            todayAppointmentsLabel.setText("0");
            monthlyConsultationsLabel.setText("0");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des statistiques", e);
            showError("Erreur", "Impossible de charger les statistiques : " + e.getMessage());
        }
    }

    @FXML
    public void handleLogout() {
        try {
            LOGGER.info("Tentative de déconnexion...");
            
            // Effacer les données de session
            SessionManager.clearUserSession();
            LOGGER.info("Session effacée");

            // Obtenir la scène actuelle à partir de n'importe quel élément FXML
            Scene currentScene = headerBox.getScene();
            if (currentScene == null) {
                LOGGER.severe("La scène est null");
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();
            if (stage == null) {
                LOGGER.severe("Le stage est null");
                return;
            }

            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            // Configurer et afficher la nouvelle scène
            Scene loginScene = new Scene(root);
            stage.setScene(loginScene);
            stage.setTitle("Connexion");
            stage.show();
            
            LOGGER.info("Déconnexion réussie");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la page de connexion: " + e.getMessage(), e);
            showError("Erreur", "Impossible de charger la page de connexion : " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la déconnexion: " + e.getMessage(), e);
            showError("Erreur", "Impossible de se déconnecter : " + e.getMessage());
        }
    }

    public void setUser(User user) {
        try {
            LOGGER.info("Définition de l'utilisateur courant...");
            if (user == null) {
                LOGGER.warning("L'utilisateur fourni est null");
                return;
            }
            
            this.currentUser = user;
            LOGGER.info("ID utilisateur: " + user.getId() + ", Email: " + user.getEmail());
            
            // Charger les informations immédiatement
            loadDoctorInfo();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la configuration de l'utilisateur", e);
            showError("Erreur", "Impossible de charger les informations de l'utilisateur : " + e.getMessage());
        }
    }

    private void loadDoctorInfo() {
        if (currentUser == null) {
            LOGGER.warning("Impossible de charger les informations - currentUser est null");
            return;
        }
        
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn == null) {
                LOGGER.severe("La connexion à la base de données est null");
                return;
            }
            
            LOGGER.info("Chargement des informations du médecin pour l'ID: " + currentUser.getId());
            
            // Vérifier d'abord si l'utilisateur est un médecin
            String checkRoleQuery = "SELECT roles FROM user WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkRoleQuery)) {
                checkStmt.setLong(1, currentUser.getId());
                LOGGER.info("Vérification du rôle pour l'utilisateur ID: " + currentUser.getId());
                ResultSet roleRs = checkStmt.executeQuery();
                
                if (roleRs.next()) {
                    String role = roleRs.getString("roles");
                    LOGGER.info("Rôle trouvé dans la base de données: " + role);
                    
                    if (!"ROLE_MEDECIN".equals(role)) {
                        LOGGER.warning("L'utilisateur n'est pas un médecin, rôle trouvé: " + role);
                        return;
                    }
                } else {
                    LOGGER.warning("Aucun rôle trouvé pour l'utilisateur ID: " + currentUser.getId());
                    return;
                }
            }
            
            // Charger les informations du médecin
            String sql = "SELECT * FROM doctor WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, currentUser.getId());
                LOGGER.info("Recherche du médecin avec user_id = " + currentUser.getId());
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String speciality = rs.getString("speciality");
                    String licenseNumber = rs.getString("license_number");
                    String officeAddress = rs.getString("office_address");
                    String phoneNumber = rs.getString("phone_number");
                    
                    LOGGER.info("Données du médecin trouvées:");
                    LOGGER.info("- Nom: " + firstName + " " + lastName);
                    LOGGER.info("- Spécialité: " + speciality);
                    LOGGER.info("- Numéro RPPS: " + licenseNumber);
                    LOGGER.info("- Adresse: " + officeAddress);
                    LOGGER.info("- Téléphone: " + phoneNumber);
                    
                    String fullName = firstName + " " + lastName;
                    welcomeLabel.setText("Bienvenue, Dr. " + fullName);
                    nameLabel.setText("Dr. " + fullName);
                    emailLabel.setText(currentUser.getEmail());
                    specialityLabel.setText(speciality);
                    rppsLabel.setText(licenseNumber);
                    addressLabel.setText(officeAddress);
                    phoneLabel.setText(phoneNumber);
                    
                    LOGGER.info("Labels mis à jour avec succès");
                } else {
                    LOGGER.warning("Aucune donnée trouvée dans la table doctor pour l'ID: " + currentUser.getId());
                    welcomeLabel.setText("Bienvenue, " + currentUser.getEmail());
                    emailLabel.setText(currentUser.getEmail());
                    clearOtherLabels();
                    
                    // Afficher une alerte pour informer l'utilisateur
                    showError("Information", "Votre profil médecin n'est pas encore complété. Veuillez le mettre à jour.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur SQL lors du chargement des informations du médecin: " + e.getMessage(), e);
            showError("Erreur", "Impossible de charger les informations du profil : " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur inattendue lors du chargement des informations du médecin", e);
            showError("Erreur", "Une erreur inattendue est survenue : " + e.getMessage());
        }
    }

    private void clearOtherLabels() {
        nameLabel.setText("Non renseigné");
        specialityLabel.setText("Non renseigné");
        rppsLabel.setText("Non renseigné");
        addressLabel.setText("Non renseigné");
        phoneLabel.setText("Non renseigné");
    }

    private void showError(String title, String content) {
        javafx.scene.control.Alert alert;
        if (title.equals("Information")) {
            alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        } else {
            alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleEditProfile() {
        try {
            LOGGER.info("Ouverture de la fenêtre de modification du profil");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-profile.fxml"));
            Parent root = loader.load();
            
            EditProfileController editController = loader.getController();
            editController.setUser(currentUser);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier mon profil");
            stage.setScene(new Scene(root));
            
            stage.setOnHidden(event -> {
                LOGGER.info("Fenêtre de modification fermée - Rechargement des informations");
                loadDoctorInfo();
            });
            
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture de la fenêtre de modification du profil", e);
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification : " + e.getMessage());
        }
    }
}