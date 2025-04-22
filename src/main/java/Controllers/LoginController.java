package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import models.User;
import services.UserService;

import java.io.IOException;
import java.util.List;

public class LoginController {
    @FXML
    private ComboBox<User> userComboBox;
    @FXML
    private Button loginButton;

    private final UserService userService = new UserService();
    private String roleMode = "user";

    public void setRoleMode(String mode) {
        this.roleMode = mode;
        List<User> users = userService.getAllUsers();
        if (roleMode.equals("admin")) {
            users.removeIf(u -> !u.getRolesList().contains("admin"));
        } else {
            users.removeIf(u -> u.getRolesList().contains("admin"));
        }
        userComboBox.getItems().setAll(users);
    }

    @FXML
    public void initialize() {
        // Ne rien faire ici pour éviter un mauvais remplissage
    }

    @FXML
    private void handleLogin() {
        User selectedUser = userComboBox.getValue();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Veuillez choisir un utilisateur.");
            alert.showAndWait();
            return;
        }
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            if (roleMode.equals("admin")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
                Controllers.MainLayoutController adminController = loader.getController();
                adminController.setCurrentUser(selectedUser);
                adminController.setCurrentUser(selectedUser);
                stage.setScene(scene);
                stage.setTitle("Espace Admin - " + selectedUser.getName());
                stage.setMinWidth(1200);
                stage.setMinHeight(800);
                stage.setResizable(true);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/ReclamationView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
                Controllers.Front.ReclamationController reclamationController = loader.getController();
                reclamationController.setCurrentUser(selectedUser);
                stage.setScene(scene);
                stage.setTitle("Espace Utilisateur - " + selectedUser.getName());
                stage.setMinWidth(1200);
                stage.setMinHeight(800);
                stage.setResizable(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors du chargement de l'espace.\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}
