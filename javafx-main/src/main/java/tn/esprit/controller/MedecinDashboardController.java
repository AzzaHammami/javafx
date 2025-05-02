package tn.esprit.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.model.User;
import tn.esprit.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedecinDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label todayAppointmentsLabel;
    @FXML
    private Label monthlyConsultationsLabel;
    @FXML
    private TableView<Appointment> appointmentsTable;
    @FXML
    private TableColumn<Appointment, String> patientColumn;
    @FXML
    private TableColumn<Appointment, String> dateColumn;
    @FXML
    private TableColumn<Appointment, String> timeColumn;
    @FXML
    private TableColumn<Appointment, String> typeColumn;
    @FXML
    private TableColumn<Appointment, String> statusColumn;

    private User currentUser;

    @FXML
    public void initialize() {
        // Initialiser les colonnes du tableau des rendez-vous
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void setUser(User user) {
        this.currentUser = user;
        
        // Extraire le nom d'utilisateur de l'email (partie avant @)
        String email = user.getEmail();
        String userName = email.substring(0, email.indexOf('@'));
        
        // Formater le nom d'utilisateur (première lettre en majuscule)
        userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        
        // Créer un message de bienvenue personnalisé
        String welcomeMessage = "Bonjour Dr. " + userName + ",\n" +
                               "Bienvenue dans votre espace personnel.\n" +
                               "Vous pouvez gérer vos rendez-vous et consulter vos patients ici.";
        
        welcomeLabel.setText(welcomeMessage);
        
        // Mettre à jour le titre de la fenêtre
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setTitle("Espace Médecin - Dr. " + userName);
        
        // Charger les données
        loadStatistics();
        loadAppointments();
    }
    
    private void loadStatistics() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Nombre total de patients
            String patientsQuery = "SELECT COUNT(*) as total FROM user WHERE roles = 'ROLE_PATIENT'";
            PreparedStatement patientsStmt = conn.prepareStatement(patientsQuery);
            ResultSet patientsRs = patientsStmt.executeQuery();
            if (patientsRs.next()) {
                totalPatientsLabel.setText(String.valueOf(patientsRs.getInt("total")));
            }
            
            // Rendez-vous du jour (simulation)
            todayAppointmentsLabel.setText("5");
            
            // Consultations du mois (simulation)
            monthlyConsultationsLabel.setText("42");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadAppointments() {
        // Simulation de données de rendez-vous
        // Dans une application réelle, ces données viendraient de la base de données
        appointmentsTable.getItems().clear();
        
        // Ajouter quelques rendez-vous de démonstration
        appointmentsTable.getItems().add(new Appointment("Jean Dupont", "15/06/2023", "09:00", "Consultation générale", "Confirmé"));
        appointmentsTable.getItems().add(new Appointment("Marie Martin", "15/06/2023", "10:30", "Suivi", "En attente"));
        appointmentsTable.getItems().add(new Appointment("Pierre Durand", "15/06/2023", "14:00", "Première consultation", "Confirmé"));
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
    
    // Classe interne pour représenter un rendez-vous
    public static class Appointment {
        private String patientName;
        private String date;
        private String time;
        private String type;
        private String status;
        
        public Appointment(String patientName, String date, String time, String type, String status) {
            this.patientName = patientName;
            this.date = date;
            this.time = time;
            this.type = type;
            this.status = status;
        }
        
        public String getPatientName() { return patientName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getType() { return type; }
        public String getStatus() { return status; }
    }
} 