package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import java.io.IOException;
import models.User;

public class MainLayoutController {
    @FXML
    private StackPane contentPane;
    @FXML
    private Label adminNameLabel;
    private User currentUser;

    @FXML
    public void initialize() {
        showDashboard(null); // Affiche le dashboard par défaut
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadView("/views/back/ReclamationView.fxml");
    }

    @FXML
    private void showReclamations(ActionEvent event) {
        loadView("/views/back/ReclamationView.fxml");
    }

    @FXML
    private void showReponses(ActionEvent event) {
        loadView("/views/back/ReponseView.fxml");
    }

    @FXML
    private void showMessagerie(ActionEvent event) {
        System.out.println("[MainLayoutController] showMessagerie: currentUser = " + (currentUser != null ? currentUser.getName() : "null"));
        loadView("/views/front/MessengerView.fxml");
    }

    @FXML
    private void showStats(ActionEvent event) {
        loadView("/views/back/StatistiquesView.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        // À adapter selon ta logique de déconnexion
        System.exit(0);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("[MainLayoutController] setCurrentUser: " + (user != null ? user.getName() : "null"));
        if (adminNameLabel != null && user != null) {
            adminNameLabel.setText(user.getName() + " (" + user.getEmail() + ")");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            Parent view;
            System.out.println("[MainLayoutController] loadView: trying to load " + fxmlPath);
            if ("/views/MessengerView.fxml".equals(fxmlPath)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                view = loader.load();
                Controllers.MessengerController messengerController = loader.getController();
                System.out.println("[MainLayoutController] loadView: currentUser = " + (currentUser != null ? currentUser.getName() : "null"));
                if (currentUser == null) {
                    // Try to get user from somewhere else or show an error
                    System.err.println("[MainLayoutController] WARNING: currentUser is null when loading MessengerView");
                }
                messengerController.setCurrentUser(currentUser);
            } else {
                view = FXMLLoader.load(getClass().getResource(fxmlPath));
            }
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("[MainLayoutController] ERROR loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            System.err.println("[MainLayoutController] UNEXPECTED ERROR loading " + fxmlPath + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
