package com.example.rendez_vous.controllers.Back;

import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.models.User;
import com.example.rendez_vous.services.ServiceUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.*;
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
    @FXML private ComboBox<User> patientComboBox;
    // Utilisation de User pour les médecins (pas Medecin)
    @FXML private ComboBox<User> medecinComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private FlowPane cardContainer;
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
    @FXML private TextField searchField;
    @FXML private ComboBox<String> metierCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private HBox paginationBox;
    private Button selectedCreneauButton;
    private LocalTime selectedCreneauTime;
    private int currentStep = 1; // 1: Medecin, 2: DateHeure, 3: Motif, 4: Confirmation
    private ObservableList<RendezVous> allRdvList = FXCollections.observableArrayList();

    private final Servicerendez_vous service = new Servicerendez_vous();
    private ServiceUser userService = new ServiceUser();

    // Pagination
    private int currentPage = 1;
    private final int itemsPerPage = 4;

    @FXML
    public void initialize() {
        cardContainer.getChildren().clear();
        // Remplir le ComboBox des patients par leur nom
        List<User> patients = userService.getAllPatients();
        patientComboBox.setItems(FXCollections.observableArrayList(patients));
        patientComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        patientComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        // Remplir le ComboBox des médecins par leur nom
        List<User> medecins = userService.getAllMedecins();
        medecinComboBox.setItems(FXCollections.observableArrayList(medecins));
        medecinComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        medecinComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        // Remplir la liste des rendez-vous
        allRdvList.setAll(service.listerRendezVous());
        // Alimenter le ComboBox des métiers (spécialités)
        java.util.Set<String> specialites = allRdvList.stream()
            .map(rv -> {
                User med = userService.getAllMedecins().stream().filter(u -> u.getId() == rv.getMedecinId()).findFirst().orElse(null);
                return (med != null) ? med.getSpecialite() : "";
            })
            .filter(s -> s != null && !s.isEmpty())
            .collect(java.util.stream.Collectors.toSet());
        metierCombo.getItems().clear();
        metierCombo.getItems().add("Tous les métiers");
        metierCombo.getItems().addAll(specialites);
        metierCombo.setValue("Tous les métiers");
        // Initialiser le tri
        if (sortCombo != null) {
            sortCombo.getItems().clear();
            sortCombo.getItems().addAll("Motif", "Date");
            sortCombo.setValue("Motif");
        }
        // Listeners dynamiques
        if (searchField != null) searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        if (metierCombo != null) metierCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        if (sortCombo != null) sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String search = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
        String metier = (metierCombo != null && metierCombo.getValue() != null) ? metierCombo.getValue() : "Tous les métiers";
        String sort = (sortCombo != null && sortCombo.getValue() != null) ? sortCombo.getValue() : "Motif";

        List<RendezVous> filtered = allRdvList.stream()
            .filter(rv -> {
                // Filtre par métier
                if (!"Tous les métiers".equals(metier)) {
                    User med = userService.getAllMedecins().stream().filter(u -> u.getId() == rv.getMedecinId()).findFirst().orElse(null);
                    String spec = (med != null) ? med.getSpecialite() : "";
                    if (!metier.equals(spec)) return false;
                }
                // Filtre texte
                if (!search.isEmpty()) {
                    User med = userService.getAllMedecins().stream().filter(u -> u.getId() == rv.getMedecinId()).findFirst().orElse(null);
                    String nomMed = (med != null) ? med.getName().toLowerCase() : "";
                    if (!(rv.getMotif().toLowerCase().contains(search)
                        || nomMed.contains(search)
                        || String.valueOf(rv.getPatientId()).contains(search))) {
                        return false;
                    }
                }
                return true;
            })
            .sorted(getComparator(sort))
            .collect(java.util.stream.Collectors.toList());

        // Pagination
        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (currentPage > totalPages) currentPage = totalPages == 0 ? 1 : totalPages;
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);
        List<RendezVous> pageItems = filtered.subList(fromIndex, toIndex);

        cardContainer.getChildren().clear();
        for (RendezVous rv : pageItems) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: linear-gradient(to bottom right, #e3f2fd, #fff); -fx-border-color: #0288d1; -fx-border-width: 2; -fx-border-radius: 18; -fx-background-radius: 18; -fx-padding: 18 32 18 32; -fx-effect: dropshadow(gaussian, #b0bec5, 8, 0.18, 0, 2); -fx-spacing: 10;");
            card.setPrefWidth(350);
            card.setMinHeight(140);
            card.setMaxWidth(400);
            card.setAlignment(javafx.geometry.Pos.TOP_LEFT);

            Label motif = new Label("\uD83D\uDCC5  Motif : " + rv.getMotif());
            motif.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0288d1;");
            Label patient = new Label("\uD83D\uDC64 Patient : " + rv.getPatientId());
            patient.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
            String nomMedecin = "";
            String specialiteMedecin = "";
            User medecin = userService.getAllMedecins().stream().filter(u -> u.getId() == rv.getMedecinId()).findFirst().orElse(null);
            if (medecin != null) {
                nomMedecin = medecin.getName();
                specialiteMedecin = medecin.getSpecialite();
            } else {
                nomMedecin = "Inconnu (ID: " + rv.getMedecinId() + ")";
                specialiteMedecin = "Inconnu";
            }
            Label medecinLabel = new Label("\uD83D\uDCBC Médecin : " + nomMedecin);
            medecinLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
            Label metierLabel = new Label("\uD83D\uDD2C Métier : " + specialiteMedecin);
            metierLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #1976d2; -fx-font-style: italic;");
            Label date = new Label("\uD83D\uDCC5 Date : " + rv.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            date.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
            Label statut = new Label("\u23F3 Statut : " + rv.getStatut());
            statut.setStyle("-fx-font-size: 15px; -fx-text-fill: #666;");

            HBox actions = new HBox(12);
            actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Button btnEdit = new Button("Modifier");
            btnEdit.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> modifierCard(rv));
            Button btnDelete = new Button("Supprimer");
            btnDelete.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> supprimerCard(rv));
            actions.getChildren().addAll(btnEdit, btnDelete);

            card.getChildren().addAll(motif, patient, medecinLabel, metierLabel, date, statut, actions);
            cardContainer.getChildren().add(card);
        }
        // Pagination controls
        paginationBox.getChildren().clear();
        if (totalPages > 1) {
            for (int i = 1; i <= totalPages; i++) {
                Button pageBtn = new Button(String.valueOf(i));
                pageBtn.setStyle("-fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: " + (i == currentPage ? "#1976d2; -fx-text-fill: white;" : "#e3f2fd; -fx-text-fill: #1976d2;"));
                final int pageNum = i;
                pageBtn.setOnAction(e -> {
                    currentPage = pageNum;
                    filterAndDisplay();
                });
                paginationBox.getChildren().add(pageBtn);
            }
        }
    }

    private java.util.Comparator<RendezVous> getComparator(String sort) {
        if ("Date".equals(sort)) {
            return java.util.Comparator.comparing(RendezVous::getDate);
        }
        // Ajouter d'autres tris si besoin
        return java.util.Comparator.comparing(RendezVous::getMotif);
    }

    // Action pour modifier un rendez-vous (boîte de dialogue rapide)
    private void modifierCard(RendezVous rv) {
        javafx.scene.control.Dialog<RendezVous> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Modifier le rendez-vous");
        dialog.setHeaderText("Modification du rendez-vous ID : " + rv.getId());

        // Boutons OK/Annuler
        javafx.scene.control.ButtonType okButtonType = new javafx.scene.control.ButtonType("Enregistrer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, javafx.scene.control.ButtonType.CANCEL);

        // Champs de modification
        javafx.scene.control.TextField motifField = new javafx.scene.control.TextField(rv.getMotif());
        motifField.setPromptText("Motif");
        javafx.scene.control.TextField statutField = new javafx.scene.control.TextField(rv.getStatut());
        statutField.setPromptText("Statut");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        grid.add(new javafx.scene.control.Label("Motif :"), 0, 0);
        grid.add(motifField, 1, 0);
        grid.add(new javafx.scene.control.Label("Statut :"), 0, 1);
        grid.add(statutField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                rv.setMotif(motifField.getText());
                rv.setStatut(statutField.getText());
                return rv;
            }
            return null;
        });

        java.util.Optional<RendezVous> result = dialog.showAndWait();
        result.ifPresent(updatedRV -> {
            service.modifierRendezVous(updatedRV); // À adapter selon ta méthode service
            filterAndDisplay();
        });
    }

    // Action pour supprimer un rendez-vous
    private void supprimerCard(RendezVous rv) {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer ce rendez-vous ?");
        confirm.setContentText("Es-tu sûr de vouloir supprimer le rendez-vous ID : " + rv.getId() + " ?");
        java.util.Optional<javafx.scene.control.ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            service.supprimerRendezVous(rv.getId());
            filterAndDisplay();
        }
    }
}
