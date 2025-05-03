package com.example.rendez_vous.controllers.Back;

import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.models.User;
import com.example.rendez_vous.services.ServiceUser;
import com.example.rendez_vous.services.Servicerendez_vous;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.scene.image.ImageView;

public class RendezVousController {
    @FXML private TextField motifField;
    @FXML private ComboBox<User> patientComboBox;
    @FXML private ComboBox<User> medecinComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private ComboBox<String> minuteCombo;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private VBox cardContainer;
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

    // Thread pool for background tasks
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);

    // Pagination
    private int currentPage = 1;
    private final int itemsPerPage = 4;

    @FXML
    public void initialize() {
        cardContainer.getChildren().clear();
        cardContainer.setFillWidth(true); // Permet aux enfants de prendre toute la largeur

        // Remplir le ComboBox des patients par leur nom
        loadPatientsAsync();

        // Remplir le ComboBox des médecins par leur nom
        loadMedecinsAsync();

        // Listeners dynamiques
        if (searchField != null) searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        if (metierCombo != null) metierCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());
        if (sortCombo != null) sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplay());

        // Load initial data
        loadRendezVousAsync();
    }

    private void loadPatientsAsync() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllPatients();
            }
        };

        task.setOnSucceeded(e -> {
            List<User> patients = task.getValue();
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
        });

        task.setOnFailed(e -> {
            showAlert("Erreur lors du chargement des patients", task.getException().getMessage());
        });

        executorService.submit(task);
    }

    private void loadMedecinsAsync() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return userService.getAllMedecins();
            }
        };

        task.setOnSucceeded(e -> {
            List<User> medecins = task.getValue();
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

            // Setup specialties after loading medecins
            setupSpecialtiesComboBox(medecins);
        });

        task.setOnFailed(e -> {
            showAlert("Erreur lors du chargement des médecins", task.getException().getMessage());
        });

        executorService.submit(task);
    }

    private void setupSpecialtiesComboBox(List<User> medecins) {
        // Extract unique specialties
        java.util.Set<String> specialites = medecins.stream()
                .map(User::getSpecialite)
                .filter(s -> s != null && !s.isEmpty())
                .collect(java.util.stream.Collectors.toSet());

        metierCombo.getItems().clear();
        metierCombo.getItems().add("Tous les métiers");
        metierCombo.getItems().addAll(specialites);
        metierCombo.setValue("Tous les métiers");

        // Setup sort combo
        if (sortCombo != null) {
            sortCombo.getItems().clear();
            sortCombo.getItems().addAll("Motif", "Date");
            sortCombo.setValue("Motif");
        }
    }

    private void loadRendezVousAsync() {
        showLoading(true);

        Task<List<RendezVous>> task = new Task<>() {
            @Override
            protected List<RendezVous> call() throws Exception {
                return service.listerRendezVous();
            }
        };

        task.setOnSucceeded(e -> {
            allRdvList.setAll(task.getValue());
            filterAndDisplay();
            showLoading(false);
        });

        task.setOnFailed(e -> {
            showAlert("Erreur lors du chargement des rendez-vous", task.getException().getMessage());
            showLoading(false);
        });

        executorService.submit(task);
    }

    private void filterAndDisplay() {
        String search = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase() : "";
        String metier = (metierCombo != null && metierCombo.getValue() != null) ? metierCombo.getValue() : "Tous les métiers";
        String sort = (sortCombo != null && sortCombo.getValue() != null) ? sortCombo.getValue() : "Motif";

        Task<List<RendezVous>> filterTask = new Task<>() {
            @Override
            protected List<RendezVous> call() throws Exception {
                return allRdvList.stream()
                        .filter(rv -> {
                            // Filtre par métier
                            if (!"Tous les métiers".equals(metier)) {
                                User med = userService.getAllMedecins().stream()
                                        .filter(u -> u.getId() == rv.getMedecinId())
                                        .findFirst().orElse(null);
                                String spec = (med != null) ? med.getSpecialite() : "";
                                if (!metier.equals(spec)) return false;
                            }
                            // Filtre texte
                            if (!search.isEmpty()) {
                                User med = userService.getAllMedecins().stream()
                                        .filter(u -> u.getId() == rv.getMedecinId())
                                        .findFirst().orElse(null);
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
            }
        };

        filterTask.setOnSucceeded(e -> {
            List<RendezVous> filtered = filterTask.getValue();
            displayRendezVousList(filtered);
        });

        filterTask.setOnFailed(e -> {
            showAlert("Erreur de filtrage", filterTask.getException().getMessage());
        });

        executorService.submit(filterTask);
    }

    private java.util.Comparator<RendezVous> getComparator(String sort) {
        switch (sort) {
            case "Date":
                return java.util.Comparator.comparing(RendezVous::getDate);
            default: // "Motif"
                return java.util.Comparator.comparing(RendezVous::getMotif);
        }
    }

    private void displayRendezVousList(List<RendezVous> filtered) {
        // Pagination
        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (currentPage > totalPages) currentPage = totalPages == 0 ? 1 : totalPages;
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);
        List<RendezVous> pageItems = fromIndex < toIndex ? filtered.subList(fromIndex, toIndex) : List.of();

        cardContainer.getChildren().clear();
        for (RendezVous rv : pageItems) {
            VBox card = createRendezVousCard(rv);
            card.setMaxWidth(600); // Largeur fixe élégante centrée
            card.setPrefWidth(600);
            VBox.setVgrow(card, javafx.scene.layout.Priority.NEVER);
            cardContainer.getChildren().add(card);
        }

        // Pagination controls
        updatePaginationControls(totalPages);
    }

    private VBox createRendezVousCard(RendezVous rv) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: linear-gradient(to bottom right, #fff, #e3f2fd); -fx-border-color: #039be5; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 14 18 14 18; -fx-effect: dropshadow(three-pass-box, #b3e5fc, 6, 0, 0, 1);");
        card.setMaxWidth(600); // Largeur fixe élégante centrée
        card.setPrefWidth(600);
        VBox.setVgrow(card, javafx.scene.layout.Priority.NEVER);
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        User medecin = userService.getAllMedecins().stream()
                .filter(u -> u.getId() == rv.getMedecinId())
                .findFirst().orElse(null);

        String nomMedecin = medecin != null ? medecin.getName() : "Inconnu (ID: " + rv.getMedecinId() + ")";
        String specialiteMedecin = medecin != null ? medecin.getSpecialite() : "Inconnu";

        Label motif = new Label("\uD83D\uDCC5  Motif : " + rv.getMotif());
        motif.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #039be5;");

        Label patient = new Label("\uD83D\uDC64 Patient : " + rv.getPatientId());
        patient.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");

        Label medecinLabel = new Label("\uD83D\uDCBC Médecin : " + nomMedecin);
        medecinLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");

        Label metierLabel = new Label("\uD83D\uDD2C Métier : " + specialiteMedecin);
        metierLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #1976d2; -fx-font-style: italic;");

        Label date = new Label("\uD83D\uDCC5 Date : " + rv.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        date.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");

        Label statut = new Label("\u23F3 Statut : " + rv.getStatut());
        statut.setStyle("-fx-font-size: 15px; -fx-text-fill: #666;");

        // Actions buttons
        HBox actions = new HBox(12);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 0 0 0 0;");

        Button btnEdit = new Button("Modif...");
        btnEdit.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; -fx-cursor: hand; -fx-padding: 6 18 6 18;");
        btnEdit.setOnAction(e -> modifierCard(rv));

        Button btnDelete = new Button("Supprimer");
        btnDelete.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 7; -fx-cursor: hand; -fx-padding: 6 18 6 18;");
        btnDelete.setOnAction(e -> supprimerCard(rv));

        actions.getChildren().setAll(btnEdit, btnDelete);

        // Ajoute tous les éléments non nuls à la carte
        java.util.List<javafx.scene.Node> nodes = new java.util.ArrayList<>();
        if (motif != null) nodes.add(motif);
        if (patient != null) nodes.add(patient);
        if (medecinLabel != null) nodes.add(medecinLabel);
        if (metierLabel != null) nodes.add(metierLabel);
        if (date != null) nodes.add(date);
        if (statut != null) nodes.add(statut);
        nodes.add(actions);
        card.getChildren().setAll(nodes);
        card.setMaxWidth(600); // Largeur fixe élégante centrée
        VBox.setVgrow(card, javafx.scene.layout.Priority.NEVER);
        return card;
    }

    private void updatePaginationControls(int totalPages) {
        paginationBox.getChildren().clear();
        if (totalPages > 1) {
            for (int i = 1; i <= totalPages; i++) {
                Button pageBtn = new Button(String.valueOf(i));
                pageBtn.setStyle("-fx-background-radius: 6; -fx-padding: 5 12; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: " +
                        (i == currentPage ? "#1976d2; -fx-text-fill: white;" : "#e3f2fd; -fx-text-fill: #1976d2;"));
                final int pageNum = i;
                pageBtn.setOnAction(e -> {
                    currentPage = pageNum;
                    filterAndDisplay();
                });
                paginationBox.getChildren().add(pageBtn);
            }
        }
    }

    // Action pour modifier un rendez-vous
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
            // Use a background task to update the rendez-vous
            Task<Boolean> updateTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return service.modifierRendezVous(updatedRV);
                }
            };

            updateTask.setOnSucceeded(e -> {
                if (updateTask.getValue()) {
                    filterAndDisplay();
                } else {
                    showAlert("Erreur", "La modification n'a pas pu être effectuée");
                }
            });

            updateTask.setOnFailed(e -> {
                showAlert("Erreur", "La modification a échoué: " + updateTask.getException().getMessage());
            });

            executorService.submit(updateTask);
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
            // Use a background task to delete the rendez-vous
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return service.supprimerRendezVous(rv.getId());
                }
            };

            deleteTask.setOnSucceeded(e -> {
                if (deleteTask.getValue()) {
                    // Remove from local list and refresh display
                    allRdvList.removeIf(r -> r.getId() == rv.getId());
                    filterAndDisplay();
                } else {
                    showAlert("Erreur", "La suppression n'a pas pu être effectuée");
                }
            });

            deleteTask.setOnFailed(e -> {
                showAlert("Erreur", "La suppression a échoué: " + deleteTask.getException().getMessage());
            });

            executorService.submit(deleteTask);
        }
    }

    private void showLoading(boolean show) {
        // Implement loading indicator if needed
    }
    private void showAlert() {
        showAlert("Information", "", Alert.AlertType.INFORMATION);
    }
    private void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Clean up resources when controller is no longer needed
    public void cleanup() {
        executorService.shutdown();
        scheduledExecutor.shutdown();
    }
}