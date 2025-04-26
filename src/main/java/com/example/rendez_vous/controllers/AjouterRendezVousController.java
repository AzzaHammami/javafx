package com.example.rendez_vous.controllers;

import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.models.RendezVous;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AjouterRendezVousController extends Application {
    @FXML private TextField patientIdField;
    @FXML private TextField motifField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> hourComboBox;
    @FXML private ComboBox<String> minuteComboBox;
    @FXML private Button ajouterButton;

    private static final Set<LocalDate> FERIES = new HashSet<>();
    private Servicerendez_vous rendezVousService = new Servicerendez_vous();

    @Override
    public void start(Stage primaryStage) throws Exception {
        initializeHolidays();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterRendezvous.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Ajouter un Rendez-vous");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @FXML
    public void initialize() {
        // Initialize time selection
        for (int i = 8; i <= 18; i++) {
            hourComboBox.getItems().add(String.format("%02d", i));
        }
        minuteComboBox.getItems().addAll("00", "15", "30", "45");

        // Set default values
        hourComboBox.setValue("09");
        minuteComboBox.setValue("00");

        // Configure date picker restrictions
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                // Disable past dates and dates more than 6 months in future
                setDisable(date.isBefore(today) || date.isAfter(today.plusMonths(6)));

                // Highlight holidays in red
                if (FERIES.contains(date)) {
                    setStyle("-fx-background-color: #ffc0cb;");
                }

                // Disable weekends
                DayOfWeek day = date.getDayOfWeek();
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        // Set minimum date to today
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    void ajouterRendezVousAction(ActionEvent event) {
        try {
            // Validate patient ID
            int patientId = validateId(patientIdField.getText(), "patient");

            // Validate motif
            String motif = validateMotif(motifField.getText());

            // Validate date and time
            LocalDateTime appointmentDateTime = validateDateTime();

            // Create and save appointment
            RendezVous rv = new RendezVous();
            rv.setPatientId(patientId);
            rv.setMotif(motif);
            rv.setDate(appointmentDateTime);
            rv.setDateCreation(LocalDateTime.now());
            rv.setStatut("En attente");

            rendezVousService.ajouterRendezVous(rv);

            // Show success and navigate to details
            showSuccess("Rendez-vous ajouté", "Le rendez-vous a été ajouté avec succès !");
            navigateToDetails(rv);

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (IOException e) {
            showError("Erreur de navigation : " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur inattendue : " + e.getMessage());
        }
    }

    private int validateId(String idText, String fieldName) throws ValidationException {
        if (idText == null || idText.trim().isEmpty()) {
            throw new ValidationException("L'ID du " + fieldName + " est requis.");
        }

        try {
            int id = Integer.parseInt(idText.trim());
            if (id <= 0) {
                throw new ValidationException("L'ID du " + fieldName + " doit être un nombre positif.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ValidationException("L'ID du " + fieldName + " doit être un nombre valide.");
        }
    }

    private String validateMotif(String motif) throws ValidationException {
        if (motif == null || motif.trim().isEmpty()) {
            throw new ValidationException("Le motif est requis.");
        }

        String trimmed = motif.trim();

        if (trimmed.length() < 3 || trimmed.length() > 100) {
            throw new ValidationException("Le motif doit contenir entre 3 et 100 caractères.");
        }

        if (!trimmed.matches("[a-zA-Z0-9\\s\\-\\'\\.\\,\\(\\)\\é\\è\\ê\\ë\\à\\â\\ä\\î\\ï\\ô\\ö\\ù\\û\\ü\\ç]*")) {
            throw new ValidationException("Le motif contient des caractères non autorisés.");
        }

        if (!Character.isLetter(trimmed.charAt(0))) {
            throw new ValidationException("Le motif doit commencer par une lettre.");
        }

        return trimmed;
    }

    private LocalDateTime validateDateTime() throws ValidationException {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            throw new ValidationException("La date est requise.");
        }

        if (hourComboBox.getValue() == null || minuteComboBox.getValue() == null) {
            throw new ValidationException("L'heure est requise.");
        }

        try {
            int hour = Integer.parseInt(hourComboBox.getValue());
            int minute = Integer.parseInt(minuteComboBox.getValue());

            LocalTime selectedTime = LocalTime.of(hour, minute);
            LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
            LocalDateTime now = LocalDateTime.now();

            // Validate appointment is in the future
            if (appointmentDateTime.isBefore(now)) {
                throw new ValidationException("Le rendez-vous doit être dans le futur.");
            }

            // Validate minimum 24h notice
            if (appointmentDateTime.isBefore(now.plusHours(24))) {
                throw new ValidationException("Le rendez-vous doit être pris au moins 24 heures à l'avance.");
            }

            // Validate working hours (8h-18h)
            if (hour < 8 || hour >= 18) {
                throw new ValidationException("Les rendez-vous sont disponibles entre 8h et 18h.");
            }

            // Validate not during lunch break (12h-14h)
            if (hour == 12 || (hour == 13 && minute < 30)) {
                throw new ValidationException("Les rendez-vous ne sont pas disponibles entre 12h et 14h.");
            }

            return appointmentDateTime;
        } catch (NumberFormatException e) {
            throw new ValidationException("Heure invalide.");
        }
    }

    private void navigateToDetails(RendezVous rv) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DetailRendezVous.fxml"));
        Parent root = loader.load();
        DetailRendezVousController controller = loader.getController();
        controller.setDetails(rv);
        ajouterButton.getScene().setRoot(root);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.show();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.show();
    }

    private void initializeHolidays() {
        int year = Year.now().getValue();
        FERIES.add(LocalDate.of(year, 1, 1));    // New Year
        FERIES.add(LocalDate.of(year, 5, 1));    // Labor Day
        FERIES.add(LocalDate.of(year, 5, 8));    // Victory 1945
        FERIES.add(LocalDate.of(year, 7, 14));   // Bastille Day
        FERIES.add(LocalDate.of(year, 8, 15));   // Assumption
        FERIES.add(LocalDate.of(year, 11, 1));   // All Saints
        FERIES.add(LocalDate.of(year, 11, 11));  // Armistice
        FERIES.add(LocalDate.of(year, 12, 25)); // Christmas

        // Add Easter Monday (variable date)
        FERIES.add(calculateEasterMonday(year));
    }

    private LocalDate calculateEasterMonday(int year) {
        // Gauss algorithm for Easter date calculation
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        return LocalDate.of(year, month, day).plusDays(1); // Easter Monday
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}
