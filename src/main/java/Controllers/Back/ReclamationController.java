package Controllers.Back;

import models.Reclamation;
import models.Reponse;
import models.User;
import services.ReclamationService;
import services.ReponseService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReclamationController implements Initializable {

    @FXML
    private TableView<Reclamation> tableView;
    @FXML
    private TableColumn<Reclamation, String> sujetColumn;
    @FXML
    private TableColumn<Reclamation, String> descriptionColumn;
    @FXML
    private TableColumn<Reclamation, LocalDate> dateColumn;
    @FXML
    private TableColumn<Reclamation, String> statutColumn;
    @FXML
    private TextArea reponseField;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> searchCriteriaBox;
    @FXML
    private ComboBox<String> sortCriteriaBox;
    @FXML
    private ComboBox<String> sortOrderBox;
    @FXML
    private TableView<Reponse> reponsesTableView;
    @FXML
    private TableColumn<Reponse, String> reponseContenuColumn;
    @FXML
    private TableColumn<Reponse, LocalDate> reponseDateColumn;
    @FXML
    private ComboBox<String> statutComboBox;
    @FXML
    private Button modifierReponseButton;
    @FXML
    private Button supprimerReponseButton;
    @FXML
    private BorderPane mainContainer;

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private Reclamation selectedReclamation;
    private Reponse selectedReponse;
    private ObservableList<Reclamation> allReclamations;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        reponseService = new ReponseService();

        // Initialize status ComboBox
        statutComboBox.getItems().addAll("En attente", "En cours", "Traitée", "Résolu");

        // Initialize search criteria ComboBox
        searchCriteriaBox.getItems().addAll("Tout", "Sujet", "Description", "Statut", "Date");
        searchCriteriaBox.setValue("Tout");

        // Initialize sort criteria and order
        sortCriteriaBox.getItems().addAll("Sujet", "Description", "Date", "Statut");
        sortCriteriaBox.setValue("Date");

        sortOrderBox.getItems().addAll("Croissant", "Décroissant");
        sortOrderBox.setValue("Décroissant");

        setupTableColumns();
        setupReponseColumns();
        setupSearch();
        setupSort();
        loadReclamations();

        // Setup selection listeners
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedReclamation = newSelection;
            if (newSelection != null) {
                loadReponses();
                statutComboBox.setValue(newSelection.getStatut());
            }
        });

        reponsesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedReponse = newSelection;
            modifierReponseButton.setDisable(newSelection == null);
            supprimerReponseButton.setDisable(newSelection == null);
            if (newSelection != null) {
                reponseField.setText(newSelection.getContenu());
            }
        });

        // Add status change listener
        statutComboBox.setOnAction(event -> {
            if (selectedReclamation != null) {
                selectedReclamation.setStatut(statutComboBox.getValue());
                reclamationService.modifier(selectedReclamation);
                loadReclamations();
            }
        });
    }

    private void setupTableColumns() {
        sujetColumn.setCellValueFactory(new PropertyValueFactory<>("sujet"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReclamation"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Set column widths
        sujetColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
        descriptionColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.4));
        dateColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        statutColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
    }

    private void setupReponseColumns() {
        reponseContenuColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getContenu()));
        reponseDateColumn.setCellValueFactory(cellData ->
            new SimpleObjectProperty<>(cellData.getValue().getDateReponse()));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterReclamations(newValue);
        });
    }

    private void setupSort() {
        // Add listeners for sort criteria and order changes
        sortCriteriaBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applySort();
            }
        });

        sortOrderBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applySort();
            }
        });
    }

    private void applySort() {
        String criteria = sortCriteriaBox.getValue();
        boolean isAscending = "Croissant".equals(sortOrderBox.getValue());

        if (criteria == null) return;

        ObservableList<Reclamation> items = tableView.getItems();

        items.sort((r1, r2) -> {
            int result = 0;
            switch (criteria) {
                case "Sujet":
                    result = r1.getSujet().compareToIgnoreCase(r2.getSujet());
                    break;
                case "Description":
                    result = r1.getDescription().compareToIgnoreCase(r2.getDescription());
                    break;
                case "Date":
                    result = r1.getDateReclamation().compareTo(r2.getDateReclamation());
                    break;
                case "Statut":
                    result = r1.getStatut().compareToIgnoreCase(r2.getStatut());
                    break;
            }
            return isAscending ? result : -result;
        });
    }

    private void filterReclamations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tableView.setItems(allReclamations);
            updateStatistics(allReclamations);
            applySort(); // Apply sort after filtering
            return;
        }

        String criteria = searchCriteriaBox.getValue();
        ObservableList<Reclamation> filteredList = allReclamations.filtered(reclamation -> {
            String searchLower = searchText.toLowerCase();

            switch (criteria) {
                case "Sujet":
                    return reclamation.getSujet().toLowerCase().contains(searchLower);
                case "Description":
                    return reclamation.getDescription().toLowerCase().contains(searchLower);
                case "Statut":
                    return reclamation.getStatut().toLowerCase().contains(searchLower);
                case "Date":
                    return reclamation.getDateReclamation().toString().contains(searchLower);
                case "Tout":
                default:
                    return reclamation.getSujet().toLowerCase().contains(searchLower) ||
                           reclamation.getDescription().toLowerCase().contains(searchLower) ||
                           reclamation.getStatut().toLowerCase().contains(searchLower) ||
                           reclamation.getDateReclamation().toString().contains(searchLower);
            }
        });

        tableView.setItems(filteredList);
        updateStatistics(filteredList);
        applySort(); // Apply sort after filtering
    }

    private void loadReclamations() {
        allReclamations = FXCollections.observableArrayList();
        allReclamations.addAll(reclamationService.getAll());
        tableView.setItems(allReclamations);

        // Update statistics
        updateStatistics(allReclamations);
    }

    private void updateStatistics(List<Reclamation> reclamations) {
        long total = reclamations.size();
        long enAttente = reclamations.stream().filter(r -> "En attente".equals(r.getStatut())).count();
        long enCours = reclamations.stream().filter(r -> "En cours".equals(r.getStatut())).count();
        long traitees = reclamations.stream().filter(r -> "Traitée".equals(r.getStatut())).count();

        // Find and update the statistics labels
        Scene scene = tableView.getScene();
        if (scene != null) {
            updateStatLabel(scene, "totalValue", String.valueOf(total));
            updateStatLabel(scene, "enAttenteValue", String.valueOf(enAttente));
            updateStatLabel(scene, "enCoursValue", String.valueOf(enCours));
            updateStatLabel(scene, "traiteesValue", String.valueOf(traitees));
        }
    }

    private void updateStatLabel(Scene scene, String labelId, String value) {
        Label label = (Label) scene.lookup("#" + labelId);
        if (label != null) {
            label.setText(value);
        }
    }

    private void loadReponses() {
        if (selectedReclamation != null) {
            ObservableList<Reponse> reponses = FXCollections.observableArrayList();
            reponses.addAll(reponseService.getReponsesByReclamation(selectedReclamation.getId()));
            reponsesTableView.setItems(reponses);
        }
    }

    @FXML
    private void handleEnvoyerReponse() {
        if (selectedReclamation != null && reponseField.getText() != null && !reponseField.getText().trim().isEmpty()) {
            if (selectedReponse != null) {
                // Mode modification
                selectedReponse.setContenu(reponseField.getText().trim());
                selectedReponse.setDateReponse(LocalDate.now());
                reponseService.modifier(selectedReponse);
                selectedReponse = null;
            } else {
                // Mode ajout
                Reponse reponse = new Reponse();
                reponse.setContenu(reponseField.getText().trim());
                reponse.setDateReponse(LocalDate.now());
                reponse.setReclamation(selectedReclamation);
                reponseService.ajouter(reponse);
            }

            loadReponses();
            reponseField.clear();
            showAlert("Succès", "Réponse enregistrée avec succès");
        } else {
            showAlert("Erreur", "Veuillez sélectionner une réclamation et écrire une réponse");
        }
    }

    @FXML
    private void handleSupprimerReponse() {
        if (selectedReponse != null) {
            reponseService.supprimer(selectedReponse.getId());
            loadReponses();
            reponseField.clear();
            selectedReponse = null;
            showAlert("Succès", "Réponse supprimée avec succès");
        }
    }

    @FXML
    private void handleAnnulerModification() {
        selectedReponse = null;
        reponseField.clear();
        modifierReponseButton.setDisable(true);
        supprimerReponseButton.setDisable(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleRetourHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            // Ajout explicite du CSS pour Home
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStatistiques(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/StatistiquesView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            // Ajout explicite du CSS
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    @FXML
    private void showMessagerie(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        StackPane contentPane = (StackPane) scene.lookup("#contentPane");
        if (contentPane != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MessengerView.fxml"));
                Parent messengerView = loader.load();
                Controllers.MessengerController messengerController = loader.getController();
                messengerController.setCurrentUser(currentUser);
                contentPane.getChildren().setAll(messengerView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Impossible de trouver contentPane dans la scène !");
        }
    }
}
