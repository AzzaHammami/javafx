package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.services.ExternalNotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.stage.Stage;

public class PatientDashboardController {
    @FXML
    private VBox notificationBox;
    @FXML
    private Label notifBadge;
    @FXML
    private Button closeNotifBtn;
    @FXML
    private ToggleButton toutesBtn;
    @FXML
    private ToggleButton nonLuesBtn;
    @FXML
    private ToggleButton medicalesBtn;
    @FXML
    private ToggleButton administrativesBtn;

    private NotificationController notificationController;
    private int userId = 1; // À remplacer par l'ID de l'utilisateur connecté

    @FXML
    public void initialize() {
        notificationController = new NotificationController(notificationBox, userId, notifBadge);
        
        closeNotifBtn.setOnAction(e -> {
            Node source = (Node) e.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        });

        // Configuration des filtres
        toutesBtn.setOnAction(e -> filterNotifications("toutes"));
        nonLuesBtn.setOnAction(e -> filterNotifications("non_lues"));
        medicalesBtn.setOnAction(e -> filterNotifications("medicales"));
        administrativesBtn.setOnAction(e -> filterNotifications("administratives"));
    }

    private void filterNotifications(String filter) {
        // Mise à jour du style des boutons
        toutesBtn.setStyle("-fx-background-color: " + (filter.equals("toutes") ? "#e3f2fd" : "#e0e0e0"));
        nonLuesBtn.setStyle("-fx-background-color: " + (filter.equals("non_lues") ? "#e3f2fd" : "#e0e0e0"));
        medicalesBtn.setStyle("-fx-background-color: " + (filter.equals("medicales") ? "#e3f2fd" : "#e0e0e0"));
        administrativesBtn.setStyle("-fx-background-color: " + (filter.equals("administratives") ? "#e3f2fd" : "#e0e0e0"));

        // Recharger les notifications avec le filtre
        notificationController.loadNotifications(filter);
    }
}
