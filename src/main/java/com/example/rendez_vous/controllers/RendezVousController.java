package com.example.rendez_vous.controllers;

import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.models.User;
import com.example.rendez_vous.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RendezVousController {
    @FXML private TextField motifField;
    @FXML private TextField patientIdField;
    @FXML private TextField medecinIdField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private TableView<RendezVous> tableView;
    @FXML private TableColumn<RendezVous, Integer> idCol;
    @FXML private TableColumn<RendezVous, String> motifCol;
    @FXML private TableColumn<RendezVous, Integer> patientIdCol;
    @FXML private TableColumn<RendezVous, Integer> medecinIdCol;
    @FXML private TableColumn<RendezVous, String> dateCol; 
    @FXML private TableColumn<RendezVous, String> statutCol;
    @FXML private FlowPane creneauxPane;
    @FXML private HBox stepBar;
    @FXML private Circle circleMedecin, circleDateHeure, circleMotif, circleConfirmation;
    @FXML private Line line1, line2, line3;
    @FXML private VBox confirmationBox;
    @FXML private Label recapLabel;
    @FXML private Button btnConfirmer, btnAnnuler;
    @FXML private VBox ficheMedecinBox;
    @FXML private ImageView photoMedecin;
    @FXML private Label nomMedecinLabel;
    @FXML private Label specialiteMedecinLabel;
    @FXML private Label adresseMedecinLabel;
    private Button selectedCreneauButton;
    private LocalTime selectedCreneauTime;
    private int currentStep = 1; // 1: Medecin, 2: DateHeure, 3: Motif, 4: Confirmation

    private final Servicerendez_vous service = new Servicerendez_vous();
    private ServiceUser userService = new ServiceUser();

    @FXML
    public void initialize() {
        // Initialisation des ComboBox
        heureCombo.setItems(FXCollections.observableArrayList("08", "09", "10", "11", "14", "15", "16", "17"));
        minuteCombo.setItems(FXCollections.observableArrayList("00", "15", "30", "45"));
        statutComboBox.setItems(FXCollections.observableArrayList("En attente", "Confirmé", "Annulé"));

        // Initialisation des colonnes
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        motifCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMotif()));
        patientIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getPatientId()).asObject());
        medecinIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMedecinId()).asObject());
        // Affichage formaté de la date
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        statutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                RendezVous rv = tableView.getSelectionModel().getSelectedItem();
                motifField.setText(rv.getMotif());
                patientIdField.setText(String.valueOf(rv.getPatientId()));
                medecinIdField.setText(String.valueOf(rv.getMedecinId()));
                LocalDateTime dateTime = rv.getDate();
                datePicker.setValue(dateTime.toLocalDate());
                selectedCreneauTime = dateTime.toLocalTime();
            }
        });

        // Barre d'étapes dynamique
        updateStepBar();
        // Génération dynamique des créneaux à la sélection de la date
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                generateCreneaux(newDate);
                currentStep = 2;
                updateStepBar();
            }
        });

        // Chargement initial des données
        rafraichirListe();

        // Gestion de la confirmation
        btnConfirmer.setOnAction(e -> confirmerRendezVous());
        btnAnnuler.setOnAction(e -> retourAccueil());
        confirmationBox.setVisible(false);
        confirmationBox.setManaged(false);

        // Mise à jour fiche médecin dès qu'on saisit l'ID
        medecinIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int id = Integer.parseInt(newVal);
                updateFicheMedecin(id);
            } catch (Exception ignored) {}
        });
        // Affichage initial si champ pré-rempli
        try {
            int id = Integer.parseInt(medecinIdField.getText());
            updateFicheMedecin(id);
        } catch (Exception ignored) {}
    }

    @FXML
    public void ajouterRendezVous() {
        try {
            // Vérification des champs obligatoires
            if (motifField.getText() == null || motifField.getText().trim().isEmpty() ||
                patientIdField.getText() == null || patientIdField.getText().trim().isEmpty() ||
                medecinIdField.getText() == null || medecinIdField.getText().trim().isEmpty() ||
                datePicker.getValue() == null ||
                heureCombo.getValue() == null || heureCombo.getValue().trim().isEmpty() ||
                minuteCombo.getValue() == null || minuteCombo.getValue().trim().isEmpty() ||
                statutComboBox.getValue() == null || statutComboBox.getValue().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs obligatoires");
                alert.setHeaderText("Veuillez remplir tous les champs obligatoires.");
                alert.setContentText("Tous les champs du formulaire doivent être renseignés avant d'ajouter un rendez-vous.");
                alert.showAndWait();
                return;
            }

            RendezVous rv = new RendezVous();
            rv.setMotif(motifField.getText());
            rv.setPatientId(Integer.parseInt(patientIdField.getText()));
            rv.setMedecinId(Integer.parseInt(medecinIdField.getText()));
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.of(
                Integer.parseInt(heureCombo.getValue()),
                Integer.parseInt(minuteCombo.getValue())
            );
            rv.setDate(LocalDateTime.of(date, time));
            rv.setStatut(statutComboBox.getValue());
            service.ajouterRendezVous(rv);
            rafraichirListe();
            
            // Clear uniquement les champs texte
            motifField.clear();
            patientIdField.clear();
            medecinIdField.clear();
            // On ne touche pas aux ComboBox et DatePicker
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de saisie");
            alert.setContentText("Les IDs du patient et du médecin doivent être des nombres.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ajout");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void modifierRendezVous() {
        try {
            RendezVous selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.setMotif(motifField.getText());
                selected.setPatientId(Integer.parseInt(patientIdField.getText()));
                selected.setMedecinId(Integer.parseInt(medecinIdField.getText()));
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.of(
                    Integer.parseInt(heureCombo.getValue()),
                    Integer.parseInt(minuteCombo.getValue())
                );
                selected.setDate(LocalDateTime.of(date, time));
                selected.setStatut(statutComboBox.getValue());
                service.modifierRendezVous(selected);
                rafraichirListe();

                // Clear uniquement les champs texte
                motifField.clear();
                patientIdField.clear();
                medecinIdField.clear();
                // On ne touche pas aux ComboBox et DatePicker
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de saisie");
            alert.setContentText("Les IDs du patient et du médecin doivent être des nombres.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la modification");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void supprimerRendezVous() {
        RendezVous selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.supprimerRendezVous(selected.getId());
            rafraichirListe();
        }
    }

    @FXML
    public void rafraichirListe() {
        List<RendezVous> list = service.listerRendezVous();
        System.out.println("DEBUG - Rendez-vous récupérés : " + list.size());
        for (RendezVous rv : list) {
            System.out.println(rv);
        }
        ObservableList<RendezVous> observableList = FXCollections.observableArrayList(list);
        tableView.setItems(observableList);
    }

    private void updateStepBar() {
        // Reset
        circleMedecin.setFill(currentStep >= 1 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
        line1.setStroke(currentStep >= 2 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
        circleDateHeure.setFill(currentStep >= 2 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
        line2.setStroke(currentStep >= 3 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
        circleMotif.setFill(currentStep >= 3 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
        line3.setStroke(currentStep >= 4 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
        circleConfirmation.setFill(currentStep >= 4 ? Paint.valueOf("#ffd600") : Paint.valueOf("#e0e0e0"));
    }

    private void generateCreneaux(LocalDate date) {
        creneauxPane.getChildren().clear();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(12, 30);
        int medecinId = 0;
        try {
            medecinId = Integer.parseInt(medecinIdField.getText());
        } catch (Exception ignored) {}
        List<LocalTime> takenTimes = service.getTakenTimesForDate(date, medecinId);
        LocalTime current = start;
        while (!current.isAfter(end)) {
            final LocalTime creneau = current; // Doit être final ou effectif final pour la lambda
            Button btn = new Button(creneau.format(DateTimeFormatter.ofPattern("HH:mm")));
            btn.setPrefWidth(90);
            btn.setPrefHeight(36);
            btn.setStyle("-fx-background-radius: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #fff; -fx-border-color: #1976d2; -fx-border-width: 2; -fx-text-fill: #1976d2;");
            if (takenTimes.contains(creneau)) {
                btn.setStyle("-fx-background-radius: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ff5252; -fx-text-fill: white; -fx-border-color: #ff5252; -fx-border-width: 2;");
                btn.setDisable(true);
            } else {
                btn.setOnAction(e -> {
                    if (selectedCreneauButton != null) {
                        selectedCreneauButton.setStyle("-fx-background-radius: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #fff; -fx-border-color: #1976d2; -fx-border-width: 2; -fx-text-fill: #1976d2;");
                    }
                    btn.setStyle("-fx-background-radius: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #1976d2; -fx-text-fill: white; -fx-border-color: #1976d2; -fx-border-width: 2;");
                    selectedCreneauButton = btn;
                    selectedCreneauTime = creneau;
                    currentStep = 3;
                    updateStepBar();
                    showConfirmation();
                });
            }
            creneauxPane.getChildren().add(btn);
            current = current.plusMinutes(30);
        }
    }

    // Affiche la zone de confirmation avec le récapitulatif
    private void showConfirmation() {
        String recap = "Médecin ID : " + medecinIdField.getText()
                + "\nPatient ID : " + patientIdField.getText()
                + "\nDate : " + (datePicker.getValue() != null ? datePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "")
                + "\nHeure : " + (selectedCreneauTime != null ? selectedCreneauTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "")
                + "\nMotif : " + motifField.getText();
        recapLabel.setText(recap);
        confirmationBox.setVisible(true);
        confirmationBox.setManaged(true);
    }

    // À appeler lors du clic sur "Confirmer"
    private void confirmerRendezVous() {
        if (datePicker.getValue() != null && selectedCreneauTime != null) {
            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), selectedCreneauTime);
            RendezVous rv = new RendezVous();
            rv.setMotif(motifField.getText());
            rv.setPatientId(Integer.parseInt(patientIdField.getText()));
            rv.setMedecinId(Integer.parseInt(medecinIdField.getText()));
            rv.setDate(dateTime);
            rv.setStatut(statutComboBox.getValue());
            service.ajouterRendezVous(rv);
            currentStep = 4;
            updateStepBar();
            confirmationBox.setVisible(false);
            confirmationBox.setManaged(false);
            rafraichirListe();
            // Afficher une alerte de succès si besoin
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Le rendez-vous a bien été créé !");
            alert.showAndWait();
            // Reset du formulaire si besoin
        }
    }

    // Retour à l'accueil lors du clic sur Annuler
    private void retourAccueil() {
        // À adapter selon ta navigation : exemple simple avec fermeture de la fenêtre
        Stage stage = (Stage) confirmationBox.getScene().getWindow();
        stage.close();
        // Ou charger la scène d'accueil avec FXMLLoader si tu as une page d'accueil
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Accueil.fxml"));
        // Parent root = loader.load();
        // stage.setScene(new Scene(root));
    }

    private void updateFicheMedecin(int medecinId) {
        try {
            User medecin = userService.getAllMedecins().stream().filter(u -> u.getId() == medecinId).findFirst().orElse(null);
            if (medecin != null) {
                nomMedecinLabel.setText(medecin.getName());
                specialiteMedecinLabel.setText(medecin.getSpecialite());
                adresseMedecinLabel.setText(medecin.getAdresse() != null ? medecin.getAdresse() : "");
                if (medecin.getImageUrl() != null && !medecin.getImageUrl().isEmpty()) {
                    photoMedecin.setImage(new Image(medecin.getImageUrl(), true));
                } else {
                    photoMedecin.setImage(new Image(getClass().getResource("/images/default-doctor.png").toExternalForm()));
                }
            } else {
                nomMedecinLabel.setText("Médecin inconnu");
                specialiteMedecinLabel.setText("");
                adresseMedecinLabel.setText("");
                photoMedecin.setImage(new Image(getClass().getResource("/images/default-doctor.png").toExternalForm()));
            }
        } catch (Exception e) {
            nomMedecinLabel.setText("Erreur médecin");
            specialiteMedecinLabel.setText("");
            adresseMedecinLabel.setText("");
        }
    }
}
