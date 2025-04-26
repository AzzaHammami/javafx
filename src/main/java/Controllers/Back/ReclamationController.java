package Controllers.Back;

import models.Reclamation;
import models.Reponse;
import models.User;
import services.ReclamationService;
import services.ReponseService;
import services.UserService;
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
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReclamationController implements Initializable {

    @FXML
    private VBox cardContainer;
    @FXML
    private TableColumn<Reponse, String> reponseContenuColumn;
    @FXML
    private TableColumn<Reponse, LocalDate> reponseDateColumn;
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
    private ComboBox<String> statutComboBox;
    @FXML
    private Button modifierReponseButton;
    @FXML
    private Button supprimerReponseButton;
    @FXML
    private BorderPane mainContainer;
    @FXML
    private TextField sujetField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private DatePicker dateReclamationPicker;
    @FXML
    private VBox reponseZone;
    @FXML
    private VBox gestionReponseZone;
    @FXML
    private ScrollPane mainScrollPane;

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private UserService userService;
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
        userService = new UserService();

        // Correction : forcer la zone de réponse à être cachée au démarrage
        if (reponseZone != null) {
            reponseZone.setVisible(false);
            reponseZone.setManaged(false);
        } else {
            System.out.println("[DEBUG] reponseZone est null au démarrage !");
        }

        // DEBUG : afficher si la zone est bien cachée
        System.out.println("[DEBUG] reponseZone visible=" + (reponseZone != null ? reponseZone.isVisible() : "null") + ", managed=" + (reponseZone != null ? reponseZone.isManaged() : "null"));

        // Cacher toute la section Gestion des Réponses au démarrage
        if (gestionReponseZone != null) {
            gestionReponseZone.setVisible(false);
            gestionReponseZone.setManaged(false);
        } else {
            System.out.println("[DEBUG] gestionReponseZone est null au démarrage !");
        }

        // Initialize status ComboBox
        statutComboBox.getItems().addAll("En attente", "En cours", "Traitée", "Résolu");

        // Initialize search criteria ComboBox
        searchCriteriaBox.getItems().addAll("Tout", "Sujet", "Description", "Statut", "Date");
        searchCriteriaBox.setValue("Tout");

        // Initialize sort criteria and order
        sortCriteriaBox.getItems().addAll("Sujet", "Description", "Date", "Statut");
        sortOrderBox.getItems().addAll("Croissant", "Décroissant");
        sortOrderBox.setValue("Croissant");

        // TableView setup (si besoin)
        reponseContenuColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContenu()));
        reponseDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateReponse()));
        reponsesTableView.setItems(FXCollections.observableArrayList());

        // Désactiver les boutons modification/suppression de réponse si rien n'est sélectionné
        modifierReponseButton.setDisable(true);
        supprimerReponseButton.setDisable(true);

        // Listener pour activer/désactiver les boutons selon la sélection
        reponsesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            modifierReponseButton.setDisable(newSelection == null);
            supprimerReponseButton.setDisable(newSelection == null);
            if (newSelection != null) {
                reponseField.setText(newSelection.getContenu());
            }
        });

        setupSearch();
        setupSort();
        loadReclamations();

        // Plus de TableView pour les réclamations : pas de listener ici

        // Add status change listener
        statutComboBox.setOnAction(event -> {
            if (selectedReclamation != null && statutComboBox.getValue() != null) {
                selectedReclamation.setStatut(statutComboBox.getValue());
                reclamationService.modifier(selectedReclamation);
                loadReclamations();
            }
        });
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

        ObservableList<Reclamation> items = FXCollections.observableArrayList(allReclamations);

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
        displayReclamations(items);
    }

    private void displayReclamations(List<Reclamation> reclamations) {
        cardContainer.getChildren().clear();
        for (Reclamation rec : reclamations) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/CardReclamation.fxml"));
                Node card = loader.load();
                CardReclamationController cardController = loader.getController();
                String userName = "";
                if (rec.getUserId() != null) {
                    User user = userService.getUserById(rec.getUserId());
                    if (user != null) userName = user.getName();
                }
                cardController.setData(rec, userName);
                cardController.getBtnSelect().setOnAction(e -> handleRepondre(rec));
                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void filterReclamations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadReclamations();
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
                default:
                    return true;
            }
        });

        cardContainer.getChildren().clear();
        for (Reclamation rec : filteredList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/CardReclamation.fxml"));
                Node card = loader.load();
                CardReclamationController cardController = loader.getController();
                String userName = "";
                if (rec.getUserId() != null) {
                    User user = userService.getUserById(rec.getUserId());
                    if (user != null) userName = user.getName();
                }
                cardController.setData(rec, userName);
                cardController.getBtnSelect().setOnAction(e -> handleRepondre(rec));
                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadReclamations() {
        allReclamations = FXCollections.observableArrayList(reclamationService.getAll());
        displayReclamations(allReclamations);
    }

    private void handleRepondre(Reclamation rec) {
        selectedReclamation = rec;
        if (gestionReponseZone != null) {
            gestionReponseZone.setVisible(true);
            gestionReponseZone.setManaged(true);
        }
        if (reponseZone != null) {
            reponseZone.setVisible(true);
            reponseZone.setManaged(true);
        }
        if (reponseField != null) {
            reponseField.requestFocus();
        }
        // Scroll automatique vers la zone de réponse
        if (mainScrollPane != null && gestionReponseZone != null) {
            mainScrollPane.layout(); // force le layout pour un scroll correct
            mainScrollPane.setVvalue(gestionReponseZone.getBoundsInParent().getMinY() /
                    (mainScrollPane.getContent().getBoundsInLocal().getHeight() - mainScrollPane.getViewportBounds().getHeight()));
        }
    }

    private void onCardSelected(Reclamation rec) {
        if (rec != null) {
            sujetField.setText(rec.getSujet());
            descriptionField.setText(rec.getDescription());
            dateReclamationPicker.setValue(rec.getDateReclamation());
            statutComboBox.setValue(rec.getStatut());
        }
        selectedReclamation = rec;
        loadReponses();
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
        }
    }

    @FXML
    private void handleSupprimerReponse() {
        if (selectedReponse != null) {
            reponseService.supprimer(selectedReponse.getId());
            loadReponses();
            reponseField.clear();
            selectedReponse = null;
        }
    }

    @FXML
    private void handleAnnulerModification() {
        selectedReponse = null;
        reponseField.clear();
        if (gestionReponseZone != null) {
            gestionReponseZone.setVisible(false);
            gestionReponseZone.setManaged(false);
        }
        if (reponseZone != null) {
            reponseZone.setVisible(false);
            reponseZone.setManaged(false);
        }
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/MessengerView.fxml"));
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
