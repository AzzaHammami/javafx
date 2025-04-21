package tn.esprit.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.config.DatabaseConfig;
import tn.esprit.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserFormController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> rolesComboBox;

    private User user;
    private AdminDashboardController adminController;

    @FXML
    public void initialize() {
        rolesComboBox.setItems(FXCollections.observableArrayList(
            "ROLE_ADMIN",
            "ROLE_MEDECIN",
            "ROLE_PATIENT"
        ));
    }

    public void setUser(User user) {
        this.user = user;
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());
        rolesComboBox.setValue(user.getRoles());
    }

    public void setAdminController(AdminDashboardController controller) {
        this.adminController = controller;
    }

    @FXML
    private void handleSave() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String roles = rolesComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || roles == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs", null);
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (user == null) {
                // Ajout d'un nouvel utilisateur
                String sql = "INSERT INTO user (email, password, roles) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                pstmt.setString(3, roles);
                pstmt.executeUpdate();
            } else {
                // Modification d'un utilisateur existant
                String sql = "UPDATE user SET email = ?, password = ?, roles = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                pstmt.setString(3, roles);
                pstmt.setLong(4, user.getId());
                pstmt.executeUpdate();
            }

            if (adminController != null) {
                adminController.loadData();
                adminController.updateStatistics();
            }

            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 