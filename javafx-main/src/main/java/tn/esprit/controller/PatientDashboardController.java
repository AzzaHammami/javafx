package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import tn.esprit.model.User;

public class PatientDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label nextAppointmentDate;
    @FXML
    private Label doctorName;
    @FXML
    private Label appointmentType;
    @FXML
    private LineChart<String, Number> activityChart;

    private User currentUser;

    @FXML
    public void initialize() {
        // Initialiser le graphique d'activité avec des données de démonstration
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Activité physique");
        
        // Ajouter des données de démonstration
        series.getData().add(new XYChart.Data<>("1 Avr", 30));
        series.getData().add(new XYChart.Data<>("5 Avr", 45));
        series.getData().add(new XYChart.Data<>("10 Avr", 35));
        series.getData().add(new XYChart.Data<>("15 Avr", 50));
        series.getData().add(new XYChart.Data<>("20 Avr", 60));
        series.getData().add(new XYChart.Data<>("25 Avr", 40));
        series.getData().add(new XYChart.Data<>("30 Avr", 30));

        activityChart.getData().add(series);
    }

    public void setUser(User user) {
        this.currentUser = user;
        // Mettre à jour le message de bienvenue avec le nom de l'utilisateur
        welcomeLabel.setText("Bonjour, " + user.getFirstName());
        
        // Charger les données réelles de l'utilisateur
        loadUserData();
    }

    private void loadUserData() {
        // TODO: Charger les données réelles depuis la base de données
        // Pour l'instant, nous utilisons des données de démonstration
        nextAppointmentDate.setText("Mercredi 23 Avril");
        doctorName.setText("Dr. Marie Laurent");
        appointmentType.setText("Consultation générale");
    }

    @FXML
    private void handleAppointments() {
        // TODO: Implémenter la navigation vers la page des rendez-vous
    }

    @FXML
    private void handleDocuments() {
        // TODO: Implémenter la navigation vers la page des documents
    }

    @FXML
    private void handleMessages() {
        // TODO: Implémenter la navigation vers la page des messages
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 