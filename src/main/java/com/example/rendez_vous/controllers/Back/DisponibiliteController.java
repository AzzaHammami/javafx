package com.example.rendez_vous.controllers.Back;

import com.example.rendez_vous.services.Servicedisponibilite;
import com.example.rendez_vous.models.Disponibilite;
import com.example.rendez_vous.services.ServiceUser;
import com.example.rendez_vous.models.User;
import com.example.rendez_vous.services.ServiceRating;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class DisponibiliteController {

    @FXML private FlowPane cardContainer;
    @FXML private Button addDisponibiliteBtn;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> orderCombo;
    @FXML private ComboBox<String> statutCombo;

    private final Servicedisponibilite service = new Servicedisponibilite();
    private final ServiceUser userService = new ServiceUser();
    private final ServiceRating ratingService = new ServiceRating();

    @FXML
    public void initialize() {
        if (addDisponibiliteBtn != null) {
            addDisponibiliteBtn.setOnAction(e -> showAjoutDisponibiliteDialog());
        }
        // Initialisation des ComboBox de tri/filtre
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList("Date", "Médecin"));
            sortCombo.setValue("Date");
            sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirListe());
        }
        if (orderCombo != null) {
            orderCombo.setItems(FXCollections.observableArrayList("Ascendant", "Descendant"));
            orderCombo.setValue("Ascendant");
            orderCombo.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirListe());
        }
        if (statutCombo != null) {
            statutCombo.setItems(FXCollections.observableArrayList("Tous", "Disponible", "Indisponible"));
            statutCombo.setValue("Tous");
            statutCombo.valueProperty().addListener((obs, oldVal, newVal) -> rafraichirListe());
        }
        rafraichirListe();
    }

    private void showAjoutDisponibiliteDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une Disponibilité");
        dialog.setHeaderText("Formulaire d'ajout de disponibilité");
        ButtonType ajouterBtn = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ajouterBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Champ Médecin ID (ComboBox avec les IDs des médecins disponibles)
        ComboBox<String> medecinIdCombo = new ComboBox<>();
        medecinIdCombo.setPromptText("ID Médecin");
        ServiceUser serviceUser = new ServiceUser();
        for (User medecin : serviceUser.getAllMedecins()) {
            medecinIdCombo.getItems().add(String.valueOf(medecin.getId()));
        }
        grid.add(new Label("Médecin ID:"), 0, 0);
        grid.add(medecinIdCombo, 1, 0);

        DatePicker dateDebutPicker = new DatePicker();
        ComboBox<String> heureDebutField = new ComboBox<>();
        heureDebutField.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00");
        DatePicker dateFinPicker = new DatePicker();
        ComboBox<String> heureFinField = new ComboBox<>();
        heureFinField.getItems().addAll("08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00");
        ComboBox<String> statutField = new ComboBox<>();
        statutField.getItems().addAll("Disponible", "Indisponible");

        grid.add(new Label("Date Début:"), 0, 1);
        grid.add(dateDebutPicker, 1, 1);
        grid.add(new Label("Heure Début:"), 0, 2);
        grid.add(heureDebutField, 1, 2);
        grid.add(new Label("Date Fin:"), 0, 3);
        grid.add(dateFinPicker, 1, 3);
        grid.add(new Label("Heure Fin:"), 0, 4);
        grid.add(heureFinField, 1, 4);
        grid.add(new Label("Statut:"), 0, 5);
        grid.add(statutField, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ajouterBtn) {
                try {
                    int medecinId = Integer.parseInt(medecinIdCombo.getValue());
                    LocalDateTime dateDebut = LocalDateTime.of(dateDebutPicker.getValue(), LocalTime.parse(heureDebutField.getValue()));
                    LocalDateTime dateFin = LocalDateTime.of(dateFinPicker.getValue(), LocalTime.parse(heureFinField.getValue()));
                    String statut = statutField.getValue();
                    Disponibilite disp = new Disponibilite();
                    disp.setMedecinId(medecinId);
                    disp.setDateDebut(dateDebut);
                    disp.setDateFin(dateFin);
                    disp.setStatut(statut);
                    service.ajouterDisponibilite(disp);
                    rafraichirListe();
                } catch (Exception ex) {
                    showError("Erreur lors de l'ajout : " + ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
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

    public void rafraichirListe() {
        try {
            List<Disponibilite> list = service.getAllDisponibilites();
            // Filtrage par statut
            String statutFilter = (statutCombo != null && statutCombo.getValue() != null) ? statutCombo.getValue() : "Tous";
            if (!"Tous".equals(statutFilter)) {
                list = list.stream().filter(d -> statutFilter.equals(d.getStatut())).toList();
            }
            // Tri
            String sortBy = (sortCombo != null && sortCombo.getValue() != null) ? sortCombo.getValue() : "Date";
            String order = (orderCombo != null && orderCombo.getValue() != null) ? orderCombo.getValue() : "Ascendant";
            if ("Date".equals(sortBy)) {
                list = list.stream().sorted((a, b) -> {
                    int cmp = a.getDateDebut().compareTo(b.getDateDebut());
                    return "Ascendant".equals(order) ? cmp : -cmp;
                }).toList();
            } else if ("Médecin".equals(sortBy)) {
                list = list.stream().sorted((a, b) -> {
                    int cmp = Integer.compare(a.getMedecinId(), b.getMedecinId());
                    return "Ascendant".equals(order) ? cmp : -cmp;
                }).toList();
            }
            cardContainer.getChildren().clear();
            for (Disponibilite disp : list) {
                VBox card = new VBox(10);
                card.setStyle("-fx-background-color: linear-gradient(to bottom right, #e3f2fd, #fff); -fx-border-color: #0288d1; -fx-border-width: 2; -fx-border-radius: 18; -fx-background-radius: 18; -fx-padding: 18 32 18 32; -fx-effect: dropshadow(gaussian, #b0bec5, 8, 0.18, 0, 2); -fx-spacing: 10;");
                card.setPrefWidth(350);
                card.setMinHeight(120);
                card.setAlignment(Pos.TOP_LEFT);
                Label idLabel = new Label("#" + disp.getId());
                idLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1976d2;");
                Label medecinLabel = new Label("Médecin : Dr. " + disp.getMedecinId());
                Label debutLabel = new Label("Début : " + disp.getDateDebut());
                Label finLabel = new Label("Fin : " + disp.getDateFin());
                Label statutLabel = new Label("Statut : " + disp.getStatut());
                // Affichage de la moyenne de note du médecin
                double moyenne = 0.0;
                try {
                    moyenne = ratingService.getMoyenneRatingMedecin(disp.getMedecinId());
                } catch (Exception ex) {
                    // Affiche rien si erreur
                }
                String stars = "";
                int fullStars = (int) Math.round(moyenne);
                for (int i = 0; i < fullStars; i++) stars += "★";
                for (int i = fullStars; i < 5; i++) stars += "☆";
                Label ratingLabel = new Label("Note : " + stars + (moyenne > 0 ? String.format(" (%.1f/5)", moyenne) : " (Aucune note)"));
                ratingLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
                HBox actions = new HBox(12);
                actions.setAlignment(Pos.CENTER_LEFT);
                Button btnEdit = new Button("Modifier");
                btnEdit.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; -fx-cursor: hand;");
                btnEdit.setOnAction(e -> modifierCard(disp));
                Button btnDelete = new Button("Supprimer");
                btnDelete.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; -fx-cursor: hand;");
                btnDelete.setOnAction(e -> supprimerCard(disp));
                actions.getChildren().addAll(btnEdit, btnDelete);
                card.getChildren().addAll(idLabel, medecinLabel, debutLabel, finLabel, statutLabel, ratingLabel, actions);
                cardContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            showError("Erreur lors du rafraîchissement de la liste: " + e.getMessage());
        }
    }

    private void modifierCard(Disponibilite disp) {
        javafx.scene.control.Dialog<Disponibilite> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Modifier la disponibilité");
        dialog.setHeaderText("Modification de la disponibilité ID : " + disp.getId());

        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, javafx.scene.control.ButtonType.CANCEL);

        javafx.scene.control.TextField medecinField = new javafx.scene.control.TextField(String.valueOf(disp.getMedecinId()));
        medecinField.setPromptText("ID Médecin");
        javafx.scene.control.DatePicker dateDebutPicker = new javafx.scene.control.DatePicker(disp.getDateDebut().toLocalDate());
        javafx.scene.control.ComboBox<String> heureDebutCombo = new javafx.scene.control.ComboBox<>();
        heureDebutCombo.getItems().addAll("08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00");
        heureDebutCombo.setValue(disp.getDateDebut().toLocalTime().toString());
        javafx.scene.control.DatePicker dateFinPicker = new javafx.scene.control.DatePicker(disp.getDateFin().toLocalDate());
        javafx.scene.control.ComboBox<String> heureFinCombo = new javafx.scene.control.ComboBox<>();
        heureFinCombo.getItems().addAll("08:00","09:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00");
        heureFinCombo.setValue(disp.getDateFin().toLocalTime().toString());
        javafx.scene.control.ComboBox<String> statutCombo = new javafx.scene.control.ComboBox<>();
        statutCombo.getItems().addAll("Disponible", "Indisponible");
        statutCombo.setValue(disp.getStatut());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 120, 10, 10));
        grid.add(new javafx.scene.control.Label("ID Médecin :"), 0, 0);
        grid.add(medecinField, 1, 0);
        grid.add(new javafx.scene.control.Label("Date Début :"), 0, 1);
        grid.add(dateDebutPicker, 1, 1);
        grid.add(new javafx.scene.control.Label("Heure Début :"), 0, 2);
        grid.add(heureDebutCombo, 1, 2);
        grid.add(new javafx.scene.control.Label("Date Fin :"), 0, 3);
        grid.add(dateFinPicker, 1, 3);
        grid.add(new javafx.scene.control.Label("Heure Fin :"), 0, 4);
        grid.add(heureFinCombo, 1, 4);
        grid.add(new javafx.scene.control.Label("Statut :"), 0, 5);
        grid.add(statutCombo, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    int medecinId = Integer.parseInt(medecinField.getText().trim());
                    java.time.LocalDateTime dateDebut = java.time.LocalDateTime.of(
                        dateDebutPicker.getValue(),
                        java.time.LocalTime.parse(heureDebutCombo.getValue())
                    );
                    java.time.LocalDateTime dateFin = java.time.LocalDateTime.of(
                        dateFinPicker.getValue(),
                        java.time.LocalTime.parse(heureFinCombo.getValue())
                    );
                    String statut = statutCombo.getValue();
                    disp.setMedecinId(medecinId);
                    disp.setDateDebut(dateDebut);
                    disp.setDateFin(dateFin);
                    disp.setStatut(statut);
                    return disp;
                } catch (Exception ex) {
                    showError("Erreur dans la saisie : " + ex.getMessage());
                }
            }
            return null;
        });

        java.util.Optional<Disponibilite> result = dialog.showAndWait();
        result.ifPresent(updatedDisp -> {
            try {
                service.modifierDisponibilite(updatedDisp);
                rafraichirListe();
                showSuccess("Disponibilité modifiée avec succès.");
            } catch (Exception e) {
                showError("Erreur lors de la modification : " + e.getMessage());
            }
        });
    }

    private void supprimerCard(Disponibilite disp) {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer cette disponibilité ?");
        confirm.setContentText("Es-tu sûr de vouloir supprimer la disponibilité ID : " + disp.getId() + " ?");
        java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                service.supprimerDisponibilite(disp.getId());
                rafraichirListe();
            } catch (Exception e) {
                showError("Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }
}
