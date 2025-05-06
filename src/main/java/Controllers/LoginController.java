package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import Controllers.Front.ReclamationController;
import services.FirebaseListener;
import utils.UserContext;

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
        // Afficher uniquement le nom dans le ComboBox
        userComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        userComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
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
            // Activer l'écoute des notifications pour l'utilisateur connecté
            FirebaseListener.ecouterNotifications(String.valueOf(selectedUser.getId()));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            if (roleMode.equals("admin")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
                Controllers.MainLayoutController mainLayoutController = loader.getController();
                mainLayoutController.setCurrentUser(selectedUser);
                UserContext.getInstance().setCurrentUser(selectedUser);
                System.out.println("[LoginController] Après setCurrentUser: " + (selectedUser != null ? selectedUser.getName() : "null"));
                mainLayoutController.showDashboard(null); // Ajouté pour charger la vue dashboard après passage du user
                stage.setScene(scene);
                stage.sizeToScene();
                stage.setTitle("Espace Admin - " + selectedUser.getName());
                stage.setMinWidth(1200);
                stage.setMinHeight(800);
                stage.setResizable(true);
            } else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/ReclamationView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
                ReclamationController reclamationController = loader.getController();
                reclamationController.setCurrentUser(selectedUser);
                stage.setScene(scene);
                stage.sizeToScene();
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
