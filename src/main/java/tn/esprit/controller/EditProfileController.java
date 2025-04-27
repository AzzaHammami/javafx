package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditProfileController {
    private static final Logger LOGGER = Logger.getLogger(EditProfileController.class.getName());

    @FXML
    private TextField nameField;

    @FXML
    private TextField specialityField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField rppsField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneField;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT * FROM doctor WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, currentUser.getId().intValue());
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        // Charger les données existantes
                        nameField.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
                        specialityField.setText(rs.getString("speciality"));
                        emailField.setText(currentUser.getEmail());
                        rppsField.setText(rs.getString("license_number"));
                        addressField.setText(rs.getString("office_address"));
                        phoneField.setText(rs.getString("phone_number"));
                    } else {
                        // Si aucun profil n'existe encore, initialiser avec les données de base
                        emailField.setText(currentUser.getEmail());
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du chargement des données du profil", e);
                showError("Erreur de chargement", "Impossible de charger les données du profil : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            
            // Vérifier si un profil existe déjà
            boolean profileExists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT id FROM doctor WHERE user_id = ?")) {
                checkStmt.setInt(1, currentUser.getId().intValue());
                ResultSet rs = checkStmt.executeQuery();
                profileExists = rs.next();
            }
            
            // Séparer le nom complet en prénom et nom
            String[] nameParts = nameField.getText().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            String sql;
            if (profileExists) {
                sql = "UPDATE doctor SET first_name = ?, last_name = ?, speciality = ?, " +
                      "phone_number = ?, office_address = ?, license_number = ? WHERE user_id = ?";
            } else {
                sql = "INSERT INTO doctor (user_id, first_name, last_name, speciality, " +
                      "phone_number, office_address, license_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                
                if (!profileExists) {
                    pstmt.setInt(paramIndex++, currentUser.getId().intValue());
                }
                
                pstmt.setString(paramIndex++, firstName);
                pstmt.setString(paramIndex++, lastName);
                pstmt.setString(paramIndex++, specialityField.getText().trim());
                pstmt.setString(paramIndex++, phoneField.getText().trim());
                pstmt.setString(paramIndex++, addressField.getText().trim());
                pstmt.setString(paramIndex++, rppsField.getText().trim());
                
                if (profileExists) {
                    pstmt.setInt(paramIndex, currentUser.getId().intValue());
                }
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    showSuccess("Succès", "Les modifications ont été enregistrées avec succès.");
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.close();
                } else {
                    conn.rollback();
                    showError("Erreur", "Aucune modification n'a été enregistrée");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la sauvegarde du profil", e);
            showError("Erreur de sauvegarde", "Impossible de sauvegarder les modifications : " + e.getMessage());
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("Le nom est obligatoire\n");
        }

        if (specialityField.getText().trim().isEmpty()) {
            errors.append("La spécialité est obligatoire\n");
        }

        if (rppsField.getText().trim().isEmpty()) {
            errors.append("Le numéro RPPS est obligatoire\n");
        }

        if (phoneField.getText().trim().isEmpty()) {
            errors.append("Le numéro de téléphone est obligatoire\n");
        }

        if (errors.length() > 0) {
            showError("Validation des champs", errors.toString());
            return false;
        }

        return true;
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
} 