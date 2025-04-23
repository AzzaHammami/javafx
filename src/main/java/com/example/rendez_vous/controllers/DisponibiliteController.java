package com.example.rendez_vous.controllers;

import com.example.rendez_vous.services.Servicedisponibilite;
import com.example.rendez_vous.models.Disponibilite;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DisponibiliteController {

    @FXML private TextField medecinIdField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> statutField;

    @FXML private TableView<Disponibilite> tableView;
    @FXML private TableColumn<Disponibilite, Integer> idCol;
    @FXML private TableColumn<Disponibilite, Integer> medecinIdCol;
    @FXML private TableColumn<Disponibilite, LocalDateTime> dateDebutCol;
    @FXML private TableColumn<Disponibilite, LocalDateTime> dateFinCol;
    @FXML private TableColumn<Disponibilite, String> statutCol;

    @FXML private ComboBox<String> heureDebutField;
    @FXML private ComboBox<String> heureFinField;

    private final Servicedisponibilite service = new Servicedisponibilite();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        medecinIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMedecinId()).asObject());
        dateDebutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateDebut()));
        dateFinCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateFin()));
        statutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));

        // Initialize ComboBox values
        statutField.setItems(FXCollections.observableArrayList("Disponible", "Indisponible"));
        statutField.setValue("Disponible");

        // Initialize time ComboBoxes
        ObservableList<String> heures = FXCollections.observableArrayList(
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
        );
        heureDebutField.setItems(heures);
        heureFinField.setItems(heures);
        heureDebutField.setValue("08:00");
        heureFinField.setValue("18:00");

        // Empêcher la sélection de dates passées
        dateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
                if (date.compareTo(today) < 0) {
                    setStyle("-fx-background-color: #ffc0cb;"); // Couleur rose pour les dates passées
                }
            }
        });

        dateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
                if (date.compareTo(today) < 0) {
                    setStyle("-fx-background-color: #ffc0cb;"); // Couleur rose pour les dates passées
                }
            }
        });

        rafraichirListe();

        tableView.setOnMouseClicked(event -> {
            Disponibilite d = tableView.getSelectionModel().getSelectedItem();
            if (d != null) {
                medecinIdField.setText(String.valueOf(d.getMedecinId()));
                dateDebutPicker.setValue(d.getDateDebut().toLocalDate());
                dateFinPicker.setValue(d.getDateFin().toLocalDate());
                heureDebutField.setValue(d.getDateDebut().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                heureFinField.setValue(d.getDateFin().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                statutField.setValue(d.getStatut());
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.show();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(message);
        alert.show();
    }

    private boolean validateInput() {
        // Validation du medecinId
        if (medecinIdField.getText().trim().isEmpty()) {
            showError("L'ID du médecin est requis.");
            return false;
        }
        try {
            int medecinId = Integer.parseInt(medecinIdField.getText().trim());
            if (medecinId <= 0) {
                showError("L'ID du médecin doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("L'ID du médecin doit être un nombre valide.");
            return false;
        }

        // Validation des dates
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (dateDebut == null || heureDebutField.getValue() == null) {
            showError("La date et l'heure de début sont requises.");
            return false;
        }
        if (dateFin == null || heureFinField.getValue() == null) {
            showError("La date et l'heure de fin sont requises.");
            return false;
        }

        // Vérification des dates dans le passé
        if (dateDebut.isBefore(today)) {
            showError("La date de début ne peut pas être dans le passé.");
            return false;
        }
        if (dateFin.isBefore(today)) {
            showError("La date de fin ne peut pas être dans le passé.");
            return false;
        }

        // Si c'est aujourd'hui, vérifier l'heure
        if (dateDebut.isEqual(today)) {
            LocalTime heureDebut = LocalTime.parse(heureDebutField.getValue());
            if (heureDebut.isBefore(now)) {
                showError("L'heure de début ne peut pas être dans le passé.");
                return false;
            }
        }

        // Validation du statut
        if (statutField.getValue() == null || statutField.getValue().trim().isEmpty()) {
            showError("Le statut est requis.");
            return false;
        }

        // Convertir les heures en LocalTime
        LocalTime heureDebut = LocalTime.parse(heureDebutField.getValue());
        LocalTime heureFin = LocalTime.parse(heureFinField.getValue());

        // Créer les LocalDateTime pour la comparaison
        LocalDateTime dateTimeDebut = LocalDateTime.of(dateDebut, heureDebut);
        LocalDateTime dateTimeFin = LocalDateTime.of(dateFin, heureFin);

        // Vérifier si la date de fin est après la date de début
        if (dateTimeFin.isBefore(dateTimeDebut) || dateTimeFin.equals(dateTimeDebut)) {
            showError("La date et l'heure de fin doivent être après la date et l'heure de début.");
            return false;
        }

        // Vérifier si les heures sont dans les plages autorisées (8h-18h)
        if (heureDebut.isBefore(LocalTime.of(8, 0)) || heureDebut.isAfter(LocalTime.of(18, 0))) {
            showError("L'heure de début doit être entre 8h et 18h.");
            return false;
        }
        if (heureFin.isBefore(LocalTime.of(8, 0)) || heureFin.isAfter(LocalTime.of(18, 0))) {
            showError("L'heure de fin doit être entre 8h et 18h.");
            return false;
        }

        return true;
    }

    private boolean validateMedecinId(String medecinIdText) {
        if (medecinIdText == null || medecinIdText.trim().isEmpty()) {
            showError("L'ID du médecin est requis.");
            return false;
        }
        if (!medecinIdText.matches("\\d+")) {
            showError("L'ID du médecin doit être un nombre.");
            return false;
        }
        int medecinId = Integer.parseInt(medecinIdText);
        if (medecinId <= 0) {
            showError("L'ID du médecin doit être un nombre positif.");
            return false;
        }
        return true;
    }

    private boolean isJourFerie(LocalDate date) {
        int year = date.getYear();
        
        // Jours fériés fixes
        LocalDate jourAn = LocalDate.of(year, 1, 1);            // 1er janvier
        LocalDate feteTravail = LocalDate.of(year, 5, 1);       // 1er mai
        LocalDate victoire1945 = LocalDate.of(year, 5, 8);      // 8 mai
        LocalDate fetePriseBasille = LocalDate.of(year, 7, 14); // 14 juillet
        LocalDate assomption = LocalDate.of(year, 8, 15);       // 15 août
        LocalDate toussaint = LocalDate.of(year, 11, 1);        // 1er novembre
        LocalDate armistice = LocalDate.of(year, 11, 11);       // 11 novembre
        LocalDate noel = LocalDate.of(year, 12, 25);            // 25 décembre

        return date.equals(jourAn) || 
               date.equals(feteTravail) || 
               date.equals(victoire1945) || 
               date.equals(fetePriseBasille) || 
               date.equals(assomption) || 
               date.equals(toussaint) || 
               date.equals(armistice) || 
               date.equals(noel);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private boolean validateStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            showError("Le statut est requis.");
            return false;
        }
        String statutTrim = statut.trim().toLowerCase();
        if (!statutTrim.equals("disponible") && !statutTrim.equals("indisponible")) {
            showError("Le statut doit être 'disponible' ou 'indisponible'.");
            return false;
        }
        return true;
    }

    @FXML
    private void ajouterDisponibilite() {
        System.out.println("Début de la méthode ajouterDisponibilite()");
        
        if (!validateInput()) {
            System.out.println("Échec de la validation des entrées");
            return;
        }

        try {
            System.out.println("Lecture des champs du formulaire...");
            int medecinId = Integer.parseInt(medecinIdField.getText().trim());
            LocalDateTime dateDebut = LocalDateTime.of(
                dateDebutPicker.getValue(),
                LocalTime.parse(heureDebutField.getValue())
            );
            LocalDateTime dateFin = LocalDateTime.of(
                dateFinPicker.getValue(),
                LocalTime.parse(heureFinField.getValue())
            );
            String statut = statutField.getValue();

            System.out.println("Valeurs lues:\n" +
                             "- Médecin ID: " + medecinId + "\n" +
                             "- Date début: " + dateDebut + "\n" +
                             "- Date fin: " + dateFin + "\n" +
                             "- Statut: " + statut);

            Disponibilite d = new Disponibilite();
            d.setMedecinId(medecinId);
            d.setDateDebut(dateDebut);
            d.setDateFin(dateFin);
            d.setStatut(statut);

            System.out.println("Tentative d'ajout dans la base de données...");
            boolean success = service.ajouterDisponibilite(d);
            
            if (success) {
                System.out.println("Ajout réussi, rafraîchissement de la liste...");
                rafraichirListe();
                showSuccess("Disponibilité ajoutée avec succès.");

                // Clear fields after successful addition
                System.out.println("Réinitialisation des champs du formulaire...");
                medecinIdField.clear();
                dateDebutPicker.setValue(null);
                dateFinPicker.setValue(null);
                heureDebutField.setValue("08:00");
                heureFinField.setValue("18:00");
                statutField.setValue("Disponible");
            } else {
                System.out.println("Échec de l'ajout dans la base de données");
                showError("L'ajout de la disponibilité a échoué. Veuillez réessayer.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de l'ajout dans la base de données: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur inattendue: " + e.getMessage());
        }
    }

    @FXML
    private void modifierDisponibilite() {
        Disponibilite selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner une disponibilité à modifier.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            int medecinId = Integer.parseInt(medecinIdField.getText().trim());
            LocalDateTime dateDebut = LocalDateTime.of(
                dateDebutPicker.getValue(),
                LocalTime.parse(heureDebutField.getValue())
            );
            LocalDateTime dateFin = LocalDateTime.of(
                dateFinPicker.getValue(),
                LocalTime.parse(heureFinField.getValue())
            );
            String statut = statutField.getValue();

            selected.setMedecinId(medecinId);
            selected.setDateDebut(dateDebut);
            selected.setDateFin(dateFin);
            selected.setStatut(statut);
            
            service.modifierDisponibilite(selected);
            rafraichirListe();
            showSuccess("Disponibilité modifiée avec succès.");
        } catch (SQLException e) {
            showError("Erreur lors de la modification dans la base de données: " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur inattendue: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerDisponibilite() {
        Disponibilite d = tableView.getSelectionModel().getSelectedItem();
        if (d != null) {
            try {
                service.supprimerDisponibilite(d.getId());
                rafraichirListe();
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        }
    }

    @FXML
    public void rafraichirListe() {
        try {
            System.out.println("Début rafraichirListe()");
            List<Disponibilite> list = service.getAllDisponibilites();
            System.out.println("Nombre de disponibilités récupérées : " + list.size());
            ObservableList<Disponibilite> observableList = FXCollections.observableArrayList(list);
            System.out.println("ObservableList créée avec succès");
            tableView.setItems(observableList);
            System.out.println("TableView mise à jour");
            tableView.refresh();
            System.out.println("TableView rafraîchie");
        } catch (SQLException e) {
            System.err.println("Erreur SQL dans rafraichirListe(): " + e.getMessage());
            showError(e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue dans rafraichirListe(): " + e.getMessage());
            showError("Erreur lors du rafraîchissement de la liste: " + e.getMessage());
        }
    }
}
