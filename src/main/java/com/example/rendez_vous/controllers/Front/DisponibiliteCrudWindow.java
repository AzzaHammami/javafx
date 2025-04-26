package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.models.Disponibilite;
import com.example.rendez_vous.services.ServiceUser;
import com.example.rendez_vous.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DisponibiliteCrudWindow {
    private TableView<Disponibilite> table;
    private DatePicker datePicker;
    private ComboBox<String> heureDebutCombo;
    private ComboBox<String> heureFinCombo;
    private ComboBox<String> medecinCombo;
    private VBox layout;

    public DisponibiliteCrudWindow() {
        // Create main layout
        layout = new VBox(20);
        layout.setPadding(new Insets(20));
        
        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        
        // Date
        Label dateLabel = new Label("Date:");
        datePicker = new DatePicker();
        datePicker.setPromptText("Sélectionnez une date");
        form.add(dateLabel, 0, 0);
        form.add(datePicker, 1, 0);
        
        // Heure début
        Label heureDebutLabel = new Label("Heure début:");
        heureDebutCombo = new ComboBox<>();
        ObservableList<String> heureDebutItems = FXCollections.observableArrayList(
            "08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"
        );
        heureDebutCombo.setItems(heureDebutItems);
        heureDebutCombo.setPromptText("Sélectionnez l'heure de début");
        form.add(heureDebutLabel, 0, 1);
        form.add(heureDebutCombo, 1, 1);
        
        // Heure fin
        Label heureFinLabel = new Label("Heure fin:");
        heureFinCombo = new ComboBox<>();
        ObservableList<String> heureFinItems = FXCollections.observableArrayList(
            "09:00", "10:00", "11:00", "12:00", "15:00", "16:00", "17:00", "18:00"
        );
        heureFinCombo.setItems(heureFinItems);
        heureFinCombo.setPromptText("Sélectionnez l'heure de fin");
        form.add(heureFinLabel, 0, 2);
        form.add(heureFinCombo, 1, 2);
        
        // Médecin (par ID)
        Label medecinLabel = new Label("Médecin ID:");
        medecinCombo = new ComboBox<>();
        ServiceUser serviceUser = new ServiceUser();
        ObservableList<String> medecinIdItems = FXCollections.observableArrayList();
        for (User medecin : serviceUser.getAllMedecins()) {
            medecinIdItems.add(String.valueOf(medecin.getId()));
        }
        medecinCombo.setItems(medecinIdItems);
        medecinCombo.setPromptText("ID Médecin");
        form.add(medecinLabel, 0, 3);
        form.add(medecinCombo, 1, 3);
        
        // Buttons
        HBox buttons = new HBox(10);
        Button addButton = new Button("Ajouter");
        addButton.setStyle("-fx-background-color: #5e72e4; -fx-text-fill: white;");
        addButton.setOnAction(e -> ajouterDisponibilite());
        
        Button updateButton = new Button("Modifier");
        updateButton.setStyle("-fx-background-color: #2dce89; -fx-text-fill: white;");
        updateButton.setOnAction(e -> modifierDisponibilite());
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #f5365c; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> supprimerDisponibilite());
        
        buttons.getChildren().addAll(addButton, updateButton, deleteButton);
        
        // Table
        table = new TableView<>();
        
        TableColumn<Disponibilite, LocalDateTime> dateDebutCol = new TableColumn<>("Date début");
        dateDebutCol.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutCol.setCellFactory(column -> {
            return new TableCell<>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });

        TableColumn<Disponibilite, LocalDateTime> dateFinCol = new TableColumn<>("Date fin");
        dateFinCol.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinCol.setCellFactory(column -> {
            return new TableCell<>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });

        TableColumn<Disponibilite, String> medecinCol = new TableColumn<>("Médecin");
        medecinCol.setCellValueFactory(new PropertyValueFactory<>("medecinName"));

        TableColumn<Disponibilite, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        
        table.getColumns().addAll(dateDebutCol, dateFinCol, medecinCol, statutCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Initialize table data
        ObservableList<Disponibilite> data = FXCollections.observableArrayList();
        table.setItems(data);
        
        // Add all to layout
        layout.getChildren().addAll(form, buttons, table);
    }
    
    public VBox getContent() {
        return layout;
    }
    
    private void ajouterDisponibilite() {
        if (validateInput()) {
            LocalDate date = datePicker.getValue();
            LocalTime heureDebut = LocalTime.parse(heureDebutCombo.getValue());
            LocalTime heureFin = LocalTime.parse(heureFinCombo.getValue());
            String medecinName = medecinCombo.getValue();

            Disponibilite disponibilite = new Disponibilite();
            disponibilite.setMedecinId(Integer.parseInt(medecinCombo.getValue()));
            disponibilite.setDateDebut(LocalDateTime.of(date, heureDebut));
            disponibilite.setDateFin(LocalDateTime.of(date, heureFin));
            // Si tu veux stocker le nom du médecin, ajoute un champ ou une propriété dédiée dans Disponibilite

            // TODO: Implement API call to save disponibilite
            table.getItems().add(disponibilite);
            showAlert("Disponibilité ajoutée avec succès!", Alert.AlertType.INFORMATION);
            clearFields();
        }
    }
    
    private void modifierDisponibilite() {
        Disponibilite selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Veuillez sélectionner une disponibilité à modifier", Alert.AlertType.WARNING);
            return;
        }

        if (validateInput()) {
            LocalDate date = datePicker.getValue();
            LocalTime heureDebut = LocalTime.parse(heureDebutCombo.getValue());
            LocalTime heureFin = LocalTime.parse(heureFinCombo.getValue());
            String medecinName = medecinCombo.getValue();

            selected.setDateDebut(LocalDateTime.of(date, heureDebut));
            selected.setDateFin(LocalDateTime.of(date, heureFin));
            // selected.setMedecinName(medecinName); // Méthode inexistante, à commenter ou supprimer

            // TODO: Implement API call to update disponibilite
            table.refresh();
            showAlert("Disponibilité modifiée avec succès!", Alert.AlertType.INFORMATION);
            clearFields();
        }
    }
    
    private void supprimerDisponibilite() {
        Disponibilite selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Implement API call to delete disponibilite
            table.getItems().remove(selected);
            showAlert("Disponibilité supprimée avec succès!", Alert.AlertType.INFORMATION);
            clearFields();
        } else {
            showAlert("Veuillez sélectionner une disponibilité à supprimer", Alert.AlertType.WARNING);
        }
    }
    
    private boolean validateInput() {
        if (datePicker.getValue() == null) {
            showAlert("Veuillez sélectionner une date", Alert.AlertType.WARNING);
            return false;
        }
        if (heureDebutCombo.getValue() == null) {
            showAlert("Veuillez sélectionner une heure de début", Alert.AlertType.WARNING);
            return false;
        }
        if (heureFinCombo.getValue() == null) {
            showAlert("Veuillez sélectionner une heure de fin", Alert.AlertType.WARNING);
            return false;
        }
        if (medecinCombo.getValue() == null) {
            showAlert("Veuillez sélectionner un médecin", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Gestion des Disponibilités");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void clearFields() {
        datePicker.setValue(null);
        heureDebutCombo.setValue(null);
        heureFinCombo.setValue(null);
        medecinCombo.setValue(null);
    }
    
    private void refreshTable() {
        // TODO: Implement refresh logic with API call
        table.refresh();
    }
}
