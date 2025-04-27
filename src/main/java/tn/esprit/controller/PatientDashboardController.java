package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import tn.esprit.model.User;
import tn.esprit.util.SessionManager;
import tn.esprit.config.DatabaseConfig;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javafx.scene.control.Alert;

public class PatientDashboardController {

    private static final Logger LOGGER = Logger.getLogger(PatientDashboardController.class.getName());

    @FXML
    private Circle userAvatar;

    @FXML
    private Circle profileAvatar;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label nameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private Label birthDateLabel;

    @FXML
    private Label ageLabel;

    @FXML
    private Label bloodGroupLabel;

    @FXML
    private Label allergiesLabel;

    @FXML
    private Label chronicDiseasesLabel;

    @FXML
    private Label emergencyContactNameLabel;

    @FXML
    private Label emergencyContactRelationLabel;

    @FXML
    private Label emergencyContactPhoneLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
        initializeDashboard();
    }

    @FXML
    public void initialize() {
        // Vérifier et créer la colonne profile_photo si nécessaire
        try {
            createProfilePhotoColumnIfNotExists();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vérification/création de la colonne profile_photo", e);
        }
    }

    private void createProfilePhotoColumnIfNotExists() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Vérifier si la colonne existe déjà
            boolean columnExists = false;
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "user_profile", "profile_photo")) {
                columnExists = rs.next();
            }

            // Si la colonne n'existe pas, la créer
            if (!columnExists) {
                try (PreparedStatement stmt = conn.prepareStatement(
                    "ALTER TABLE user_profile ADD COLUMN profile_photo VARCHAR(255)")) {
                    stmt.executeUpdate();
                    LOGGER.info("Colonne profile_photo créée avec succès");
                }
            }
        }
    }

    public void updateUserInfo(User updatedUser) {
        this.user = updatedUser;
        initializeDashboard();
    }

    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit-patient-profile.fxml"));
            Parent root = loader.load();
            
            EditPatientProfileController controller = loader.getController();
            controller.setUser(user);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier mon profil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ouverture du formulaire de modification", e);
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profileAvatar.getScene().getWindow());
        if (selectedFile != null) {
        try {
                LOGGER.info("Fichier sélectionné : " + selectedFile.getAbsolutePath());
                
                // Créer le dossier de stockage des photos s'il n'existe pas
                Path uploadDir = Paths.get("src/main/resources/uploads/profile_photos");
                Files.createDirectories(uploadDir);
                LOGGER.info("Dossier de stockage : " + uploadDir.toAbsolutePath());
                
                // Vérifier la taille du fichier (max 5MB)
                if (selectedFile.length() > 5 * 1024 * 1024) {
                    throw new IOException("La taille du fichier ne doit pas dépasser 5MB");
                }
                
                // Copier la photo dans le dossier avec un nom unique
                String fileName = "profile_" + user.getId() + "_" + System.currentTimeMillis() + 
                                selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                Path targetPath = uploadDir.resolve(fileName);
                LOGGER.info("Sauvegarde de la photo vers : " + targetPath.toAbsolutePath());
                
                // Copier le fichier
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Photo copiée avec succès");
                
                // Mettre à jour la base de données
                updateProfilePhoto(fileName);
                LOGGER.info("Base de données mise à jour");
                
                // Charger et afficher la photo
                try {
                    String imageUrl = targetPath.toUri().toString();
                    LOGGER.info("URL de l'image : " + imageUrl);
                    
                    Image image = new Image(imageUrl);
                    if (image.isError()) {
                        throw new IOException("Erreur lors du chargement de l'image : " + image.getException().getMessage());
                    }
                    
                    profileAvatar.setFill(new ImagePattern(image));
                    userAvatar.setFill(new ImagePattern(image));
                    LOGGER.info("Photo affichée avec succès");
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage de la photo", e);
                    throw new IOException("Impossible d'afficher la photo : " + e.getMessage());
                }
                
            } catch (IOException | SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du téléchargement de la photo", e);
                // Afficher un message d'erreur à l'utilisateur
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors du téléchargement de la photo");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    private void updateProfilePhoto(String fileName) throws SQLException {
        String sql = "UPDATE user_profile SET profile_photo = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fileName);
            pstmt.setInt(2, user.getId().intValue());
            pstmt.executeUpdate();
        }
    }

    private void initializeDashboard() {
        if (user != null) {
            welcomeLabel.setText("Bienvenue, " + (user.getName() != null ? user.getName() : user.getEmail()));
            emailLabel.setText(user.getEmail());
            nameLabel.setText(user.getName() != null ? user.getName() : "Non renseigné");
            phoneLabel.setText(user.getPhone() != null ? user.getPhone() : "Non renseigné");
            
            // Charger les données du profil depuis la base de données
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        try {
            String sql = "SELECT * FROM user_profile WHERE user_id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, user.getId().intValue());
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Charger la photo de profil si elle existe
                    String profilePhoto = rs.getString("profile_photo");
                    if (profilePhoto != null && !profilePhoto.isEmpty()) {
                        try {
                            String imageUrl = getClass().getResource("/uploads/profile_photos/" + profilePhoto).toExternalForm();
                            Image image = new Image(imageUrl);
                            profileAvatar.setFill(new ImagePattern(image));
                            userAvatar.setFill(new ImagePattern(image));
        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Impossible de charger la photo de profil", e);
                        }
                    }
                    
                    // Mettre à jour les labels avec les données du profil
                    nameLabel.setText(rs.getString("name"));
                    phoneLabel.setText(rs.getString("phone"));
                    
                    java.sql.Date birthDate = rs.getDate("birth_date");
                    if (birthDate != null) {
                        birthDateLabel.setText(birthDate.toString());
                        // Calculer l'âge
                        int age = calculateAge(birthDate.toLocalDate());
                        ageLabel.setText(age + " ans");
                    }
                    
                    bloodGroupLabel.setText(rs.getString("blood_group"));
                    allergiesLabel.setText(rs.getString("allergies"));
                    chronicDiseasesLabel.setText(rs.getString("chronic_diseases"));
                    emergencyContactNameLabel.setText(rs.getString("emergency_contact_name"));
                    emergencyContactRelationLabel.setText(rs.getString("emergency_contact_relation"));
                    emergencyContactPhoneLabel.setText(rs.getString("emergency_contact_phone"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du profil", e);
        }
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    @FXML
    private void handleLogout() {
        try {
            SessionManager.clearUserSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la déconnexion", e);
        }
    }
}