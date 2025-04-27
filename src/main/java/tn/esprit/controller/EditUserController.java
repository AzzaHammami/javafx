package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.collections.FXCollections;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditUserController {
    private static final Logger LOGGER = Logger.getLogger(EditUserController.class.getName());

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> roleComboBox;

    private User user;
    private UserListController parentController;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
            "ROLE_ADMIN",
            "ROLE_MEDECIN",
            "ROLE_PATIENT"
        ));
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            emailField.setText(user.getEmail());
            roleComboBox.setValue(user.getRoles());
        }
    }

    public void setParentController(UserListController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "UPDATE user SET email = ?, roles = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, emailField.getText().trim());
                pstmt.setString(2, roleComboBox.getValue());
                pstmt.setLong(3, user.getId());
                
                pstmt.executeUpdate();
                
                if (parentController != null) {
                    parentController.loadUsers();
                }
                
                closeWindow();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            showError("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateFields() {
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || role == null) {
            showError("Tous les champs sont obligatoires");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }
} 