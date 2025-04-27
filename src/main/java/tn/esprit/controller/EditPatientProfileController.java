package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditPatientProfileController {
    private static final Logger LOGGER = Logger.getLogger(EditPatientProfileController.class.getName());

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private ComboBox<String> bloodGroupCombo;
    @FXML
    private TextArea allergiesArea;
    @FXML
    private TextArea chronicDiseasesArea;
    @FXML
    private TextField emergencyContactNameField;
    @FXML
    private TextField emergencyContactRelationField;
    @FXML
    private TextField emergencyContactPhoneField;
    @FXML
    private Button saveButton;

    private User user;
    private PatientDashboardController parentController;

    @FXML
    public void initialize() {
        // Initialiser la ComboBox avec les groupes sanguins
        bloodGroupCombo.setItems(FXCollections.observableArrayList(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        ));
    }

    public void setUser(User user) {
        this.user = user;
        loadUserData();
    }

    public void setParentController(PatientDashboardController controller) {
        this.parentController = controller;
    }

    private void loadUserData() {
        if (user != null) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                String sql = "SELECT * FROM user_profile WHERE user_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, user.getId());
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        // Charger les données existantes
                        nameField.setText(rs.getString("name"));
                        emailField.setText(user.getEmail());
                        phoneField.setText(rs.getString("phone"));
                        
                        java.sql.Date birthDate = rs.getDate("birth_date");
                        if (birthDate != null) {
                            birthDatePicker.setValue(birthDate.toLocalDate());
                        }
                        
                        bloodGroupCombo.setValue(rs.getString("blood_group"));
                        allergiesArea.setText(rs.getString("allergies"));
                        chronicDiseasesArea.setText(rs.getString("chronic_diseases"));
                        emergencyContactNameField.setText(rs.getString("emergency_contact_name"));
                        emergencyContactRelationField.setText(rs.getString("emergency_contact_relation"));
                        emergencyContactPhoneField.setText(rs.getString("emergency_contact_phone"));
                    } else {
                        // Si aucun profil n'existe encore, initialiser avec les données de base
                        emailField.setText(user.getEmail());
                        nameField.setText(user.getFirstName() + " " + user.getLastName());
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Erreur lors du chargement des données du profil", e);
                showError("Erreur", "Impossible de charger les données du profil : " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        if (user == null) {
            LOGGER.severe("Erreur: utilisateur null");
            showError("Erreur", "Aucun utilisateur connecté");
            return;
        }

        // Valider les champs avant la sauvegarde
        if (!validateFields()) {
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Vérifier si un profil existe déjà
            String checkSql = "SELECT id FROM user_profile WHERE user_id = ?";
            boolean profileExists = false;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, user.getId().intValue());
                ResultSet rs = checkStmt.executeQuery();
                profileExists = rs.next();
            }
            
            String sql;
            if (profileExists) {
                sql = "UPDATE user_profile SET name = ?, phone = ?, birth_date = ?, " +
                      "blood_group = ?, allergies = ?, chronic_diseases = ?, " +
                      "emergency_contact_name = ?, emergency_contact_relation = ?, " +
                      "emergency_contact_phone = ?, updated_at = CURRENT_TIMESTAMP " +
                      "WHERE user_id = ?";
            } else {
                sql = "INSERT INTO user_profile (user_id, name, phone, birth_date, " +
                      "blood_group, allergies, chronic_diseases, emergency_contact_name, " +
                      "emergency_contact_relation, emergency_contact_phone, created_at, updated_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                
                // Récupérer toutes les valeurs
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();
                LocalDate birthDate = birthDatePicker.getValue();
                String bloodGroup = bloodGroupCombo.getValue();
                String allergies = allergiesArea.getText().trim();
                String chronicDiseases = chronicDiseasesArea.getText().trim();
                String emergencyName = emergencyContactNameField.getText().trim();
                String emergencyRelation = emergencyContactRelationField.getText().trim();
                String emergencyPhone = emergencyContactPhoneField.getText().trim();

                if (!profileExists) {
                    pstmt.setInt(paramIndex++, user.getId().intValue());
                }
                
                pstmt.setString(paramIndex++, name);
                pstmt.setString(paramIndex++, phone);
                
                if (birthDate != null) {
                    pstmt.setDate(paramIndex++, java.sql.Date.valueOf(birthDate));
                } else {
                    pstmt.setNull(paramIndex++, java.sql.Types.DATE);
                }
                
                pstmt.setString(paramIndex++, bloodGroup);
                pstmt.setString(paramIndex++, allergies);
                pstmt.setString(paramIndex++, chronicDiseases);
                pstmt.setString(paramIndex++, emergencyName);
                pstmt.setString(paramIndex++, emergencyRelation);
                pstmt.setString(paramIndex++, emergencyPhone);
                
                if (profileExists) {
                    pstmt.setInt(paramIndex, user.getId().intValue());
                }
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    conn.commit();
                    
                    // Mettre à jour l'objet User
                    user.setName(name);
                    user.setPhone(phone);
                    
                    // Mettre à jour le dashboard parent
                    if (parentController != null) {
                        parentController.updateUserInfo(user);
                    }
                    
                    showSuccess("Succès", "Les modifications ont été enregistrées avec succès.");
                    
                    // Fermer la fenêtre
                    Stage stage = (Stage) saveButton.getScene().getWindow();
                    stage.close();
                } else {
                    conn.rollback();
                    showError("Erreur", "Aucune modification n'a été enregistrée");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur SQL: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                LOGGER.severe("Erreur lors du rollback: " + ex.getMessage());
            }
            showError("Erreur", "Impossible de sauvegarder les modifications : " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.severe("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        // Required fields validation
        if (nameField.getText().trim().isEmpty()) {
            errors.append("Le nom est obligatoire\n");
        } else if (nameField.getText().trim().length() > 255) {
            errors.append("Le nom ne doit pas dépasser 255 caractères\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("L'email est obligatoire\n");
        }

        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            errors.append("Le téléphone est obligatoire\n");
        } else if (phone.length() > 20) {
            errors.append("Le numéro de téléphone ne doit pas dépasser 20 caractères\n");
        }

        if (birthDatePicker.getValue() == null) {
            errors.append("La date de naissance est obligatoire\n");
        }

        if (bloodGroupCombo.getValue() == null) {
            errors.append("Le groupe sanguin est obligatoire\n");
        } else if (bloodGroupCombo.getValue().length() > 5) {
            errors.append("Le groupe sanguin n'est pas valide\n");
        }

        String emergencyName = emergencyContactNameField.getText().trim();
        if (emergencyName.isEmpty()) {
            errors.append("Le nom du contact d'urgence est obligatoire\n");
        } else if (emergencyName.length() > 255) {
            errors.append("Le nom du contact d'urgence ne doit pas dépasser 255 caractères\n");
        }

        String emergencyRelation = emergencyContactRelationField.getText().trim();
        if (!emergencyRelation.isEmpty() && emergencyRelation.length() > 100) {
            errors.append("La relation avec le contact d'urgence ne doit pas dépasser 100 caractères\n");
        }

        String emergencyPhone = emergencyContactPhoneField.getText().trim();
        if (emergencyPhone.isEmpty()) {
            errors.append("Le téléphone du contact d'urgence est obligatoire\n");
        } else if (emergencyPhone.length() > 20) {
            errors.append("Le numéro de téléphone du contact d'urgence ne doit pas dépasser 20 caractères\n");
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

    private void goBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/patient-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 