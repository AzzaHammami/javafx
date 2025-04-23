package com.example.rendez_vous.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;
import com.example.rendez_vous.controllers.Front.PatientDashboardView;

public class LandingController {
    
    @FXML
    private void handleUserSpace(ActionEvent event) throws IOException {
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        PatientDashboardView dashboard = new PatientDashboardView();
        Scene scene = new Scene(dashboard, 1024, 768);
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void handleAdminSpace(ActionEvent event) throws IOException {
        // Navigate to admin dashboard
        Parent root = FXMLLoader.load(getClass().getResource("/Views/Back/reclamation.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.show();
    }
}
