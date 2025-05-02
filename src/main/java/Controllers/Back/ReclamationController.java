package Controllers.Back;

import models.Reclamation;
import models.Reponse;
import models.User;
import models.Conversation;
import services.ReclamationService;
import services.ReponseService;
import services.UserService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.stage.Popup;
import javafx.scene.layout.Pane;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import java.io.InputStream;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import services.GoogleApi;
import javafx.scene.control.SelectionMode;

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
    @FXML
    private Button floatingMessengerButton;
    @FXML
    private Label messengerBadgeLabel;
    @FXML
    private StackPane miniMessengersContainer;
    @FXML
    private StackPane conversationChooserContainer;
    @FXML
    private StackPane mainMessengerContainer;
    @FXML
    private StackPane messengerFloatingIconContainer;

    @FXML private Button btnLireSheets;
    @FXML private TableView<SheetRow> tableSheets;
    @FXML private TableColumn<SheetRow, String> colA;
    @FXML private TableColumn<SheetRow, String> colB;
    @FXML private TableColumn<SheetRow, String> colC;
    @FXML private TableColumn<SheetRow, String> colD;
    @FXML private TableColumn<SheetRow, String> colE;
    @FXML private TableColumn<SheetRow, String> colF;
    @FXML private Button btnExporterSheet;
    @FXML private TableView<Reclamation> reclamationsTableView;

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private UserService userService;
    private ObservableList<Reclamation> selectedReclamations = FXCollections.observableArrayList();
    private User currentUser;
    private final Map<Integer, Node> openMessengerNodesMap = new HashMap<>();
    private Popup conversationListPopup;

    // Ajout pour la gestion de toutes les réclamations affichées (filtrage, tri, etc.)
    private ObservableList<Reclamation> allReclamations = FXCollections.observableArrayList();

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
        }

        // Correction critique : s'assurer que le panneau principal n'est PAS désactivé
        if (mainContainer != null) {
            mainContainer.setDisable(false);
            mainContainer.setMouseTransparent(false);
            System.out.println("[DEBUG] mainContainer désactivé=" + mainContainer.isDisable() + ", mouseTransparent=" + mainContainer.isMouseTransparent());
        }

        // Correction critique : s'assurer que le scrollPane principal n'est PAS désactivé
        if (mainScrollPane != null) {
            mainScrollPane.setDisable(false);
            mainScrollPane.setMouseTransparent(false);
        }

        // Correction critique : s'assurer que le bouton Messenger flottant est activé
        if (floatingMessengerButton != null) {
            floatingMessengerButton.setDisable(false);
            floatingMessengerButton.setMouseTransparent(false);
        }

        // Correction critique : s'assurer que la surcouche de blocage n'existe pas
        if (miniMessengersContainer != null && miniMessengersContainer.isVisible() && miniMessengersContainer.getChildren().size() > 0) {
            System.out.println("[DEBUG] miniMessengersContainer visible et non vide, clear forcé !");
            miniMessengersContainer.getChildren().clear();
        }

        // Correction critique : rendre les overlays totalement transparents aux clics si cachés
        if (mainMessengerContainer != null) {
            mainMessengerContainer.setVisible(false);
            mainMessengerContainer.setManaged(false);
            mainMessengerContainer.setMouseTransparent(true); // Empêche de bloquer les clics
        }
        if (miniMessengersContainer != null) {
            miniMessengersContainer.setVisible(false);
            miniMessengersContainer.setManaged(false);
            miniMessengersContainer.setMouseTransparent(true); // Empêche de bloquer les clics
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
            if (selectedReclamations != null && !selectedReclamations.isEmpty() && statutComboBox.getValue() != null) {
                for (Reclamation rec : selectedReclamations) {
                    rec.setStatut(statutComboBox.getValue());
                    reclamationService.modifier(rec);
                }
                loadReclamations();
            }
        });

        // Ajoute le handler pour le bouton Messenger flottant
        if (floatingMessengerButton != null) {
            floatingMessengerButton.setOnAction(e -> {
                System.out.println("[DEBUG] Clic sur bouton Messenger flottant");
                toggleFloatingMessenger();
            });
        } else {
            System.err.println("[ERROR] floatingMessengerButton est null !");
        }

        // Google Sheets TableView columns
        if (colA != null && colB != null && colC != null && colD != null && colE != null && colF != null) {
            colA.setCellValueFactory(new PropertyValueFactory<>("a"));
            colB.setCellValueFactory(new PropertyValueFactory<>("b"));
            colC.setCellValueFactory(new PropertyValueFactory<>("c"));
            colD.setCellValueFactory(new PropertyValueFactory<>("d"));
            colE.setCellValueFactory(new PropertyValueFactory<>("e"));
            colF.setCellValueFactory(new PropertyValueFactory<>("f"));
            colA.setText("ID");
            colB.setText("Utilisateur");
            colC.setText("Sujet");
            colD.setText("Description");
            colE.setText("Date");
            colF.setText("Statut");
        }

        // Ajoute un listener pour la sélection multiple dans le TableView
        reclamationsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        reclamationsTableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Reclamation>) change -> {
            selectedReclamations.setAll(reclamationsTableView.getSelectionModel().getSelectedItems());
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
                // Nouvelle logique de sélection/désélection multiple sur double-clic
                card.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        // Double clic : toggle sélection
                        if (selectedReclamations.contains(rec)) {
                            selectedReclamations.remove(rec);
                            card.setStyle("");
                        } else {
                            selectedReclamations.add(rec);
                            card.setStyle("-fx-border-color: #6C5CE7; -fx-border-width: 3; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #6C5CE7, 10, 0.2, 0, 0);");
                        }
                    } else if (e.getClickCount() == 1) {
                        // Simple clic : sélectionne/désélectionne sans effacer les autres
                        if (selectedReclamations.contains(rec)) {
                            selectedReclamations.remove(rec);
                            card.setStyle("");
                        } else {
                            selectedReclamations.add(rec);
                            card.setStyle("-fx-border-color: #6C5CE7; -fx-border-width: 3; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #6C5CE7, 10, 0.2, 0, 0);");
                        }
                    }
                });
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
                // Nouvelle logique de sélection/désélection multiple sur double-clic
                card.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        // Double clic : toggle sélection
                        if (selectedReclamations.contains(rec)) {
                            selectedReclamations.remove(rec);
                            card.setStyle("");
                        } else {
                            selectedReclamations.add(rec);
                            card.setStyle("-fx-border-color: #6C5CE7; -fx-border-width: 3; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #6C5CE7, 10, 0.2, 0, 0);");
                        }
                    } else if (e.getClickCount() == 1) {
                        // Simple clic : sélectionne/désélectionne sans effacer les autres
                        if (selectedReclamations.contains(rec)) {
                            selectedReclamations.remove(rec);
                            card.setStyle("");
                        } else {
                            selectedReclamations.add(rec);
                            card.setStyle("-fx-border-color: #6C5CE7; -fx-border-width: 3; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, #6C5CE7, 10, 0.2, 0, 0);");
                        }
                    }
                });
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
        selectedReclamations.clear();
        selectedReclamations.add(rec);
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
        selectedReclamations.clear();
        selectedReclamations.add(rec);
        loadReponses();
    }

    private void loadReponses() {
        if (!selectedReclamations.isEmpty()) {
            ObservableList<Reponse> reponses = FXCollections.observableArrayList();
            for (Reclamation rec : selectedReclamations) {
                reponses.addAll(reponseService.getReponsesByReclamation(rec.getId()));
            }
            reponsesTableView.setItems(reponses);
        }
    }

    @FXML
    private void handleEnvoyerReponse() {
        if (!selectedReclamations.isEmpty() && reponseField.getText() != null && !reponseField.getText().trim().isEmpty()) {
            if (!selectedReclamations.isEmpty()) {
                // Mode ajout
                Reponse reponse = new Reponse();
                reponse.setContenu(reponseField.getText().trim());
                reponse.setDateReponse(LocalDate.now());
                reponse.setReclamation(selectedReclamations.get(0));
                reponseService.ajouter(reponse);
            }

            loadReponses();
            reponseField.clear();
        }
    }

    @FXML
    private void handleSupprimerReponse() {
        if (!selectedReclamations.isEmpty()) {
            reponseService.supprimer(selectedReclamations.get(0).getId());
            loadReponses();
            reponseField.clear();
        }
    }

    @FXML
    private void handleAnnulerModification() {
        selectedReclamations.clear();
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

    // --- IMPORTANT : Plus de Popup pour les mini-messageries, tout est intégré dans la page principale ---
    private void openMiniMessenger(models.Conversation conversation) {
        try {
            System.out.println("[DEBUG] openMiniMessenger appelé pour conversation: " + conversation.getId());
            // Remove the chooser if present BEFORE adding mini messenger
            miniMessengersContainer.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("compactListRoot"));
            if (openMessengerNodesMap.containsKey(conversation.getId())) {
                Node node = openMessengerNodesMap.get(conversation.getId());
                node.toFront();
                System.out.println("[DEBUG] miniMessengerPane déjà ouvert, toFront() appelé.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/MiniMessengerView.fxml"));
            Pane miniMessengerPane = loader.load();
            // Correction: s'assurer que le miniMessengerPane est bien visible et interactif
            miniMessengerPane.setVisible(true);
            miniMessengerPane.setManaged(true);
            miniMessengerPane.setMouseTransparent(false);
            miniMessengerPane.setPickOnBounds(false);
            
            Object controllerObj = loader.getController();
            if (controllerObj == null) {
                System.err.println("[ERROR] MiniMessengerController is null. Check fx:controller attribute in MiniMessengerView.fxml");
                return;
            }
            Controllers.MiniMessengerController messengerController = (Controllers.MiniMessengerController) controllerObj;
            messengerController.setConversation(conversation, currentUser, () -> closeMiniMessenger(conversation.getId()));
            int index = miniMessengersContainer.getChildren().stream()
                .filter(node -> node != null && node.getId() == null) // ignore la liste de choix
                .toArray().length;
            int horizontalOffset = 90 + (index * 360); // Décale plus à droite
            int bottomOffset = 90; // Décale plus en bas pour laisser passer le bouton
            StackPane.setAlignment(miniMessengerPane, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(miniMessengerPane, new Insets(0, horizontalOffset, bottomOffset, 0));
            miniMessengersContainer.getChildren().add(miniMessengerPane);
            miniMessengerPane.toFront();
            openMessengerNodesMap.put(conversation.getId(), miniMessengerPane);
            System.out.println("[DEBUG] miniMessengerPane ajouté au container pour conversation: " + conversation.getId());
            // Debug children and size
            System.out.println("[DEBUG] Children in miniMessengersContainer after add:");
            for (Node n : miniMessengersContainer.getChildren()) {
                System.out.println("  - " + n + " id=" + n.getId() + " visible=" + n.isVisible());
            }
            System.out.println("[DEBUG] miniMessengerPane size: width=" + miniMessengerPane.getWidth() + ", height=" + miniMessengerPane.getHeight());
            miniMessengerPane.widthProperty().addListener((obs, oldVal, newVal) -> System.out.println("[DEBUG] miniMessengerPane width changed: " + newVal));
            miniMessengerPane.heightProperty().addListener((obs, oldVal, newVal) -> System.out.println("[DEBUG] miniMessengerPane height changed: " + newVal));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeMiniMessenger(int conversationId) {
        Node node = openMessengerNodesMap.get(conversationId);
        if (node != null) {
            miniMessengersContainer.getChildren().remove(node);
            openMessengerNodesMap.remove(conversationId);
            int i = 0;
            for (Node n : miniMessengersContainer.getChildren()) {
                int offset = 40 + (i * 360);
                StackPane.setAlignment(n, Pos.BOTTOM_RIGHT);
                StackPane.setMargin(n, new Insets(0, offset, 40, 0));
                i++;
            }
        }
        // Correction : si plus aucune mini-messagerie ouverte, désactive le container
        if (miniMessengersContainer.getChildren().isEmpty()) {
            miniMessengersContainer.setVisible(false);
            miniMessengersContainer.setManaged(false);
            miniMessengersContainer.setMouseTransparent(true);
        }
    }

    private void openMainMessenger(models.Conversation conversation) {
        try {
            System.out.println("[DEBUG] openMainMessenger appelé pour conversation: " + conversation.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/MiniMessengerView.fxml"));
            Pane messengerPane = loader.load();
            Object controllerObj = loader.getController();
            if (controllerObj == null) {
                System.err.println("[ERROR] MiniMessengerController is null. Check fx:controller attribute in MiniMessengerView.fxml");
                return;
            }
            Controllers.MiniMessengerController messengerController = (Controllers.MiniMessengerController) controllerObj;
            messengerController.setConversation(conversation, currentUser, () -> closeMainMessenger());
            mainMessengerContainer.getChildren().setAll(messengerPane);
            mainMessengerContainer.setVisible(true);
            mainMessengerContainer.setManaged(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeMainMessenger() {
        mainMessengerContainer.setVisible(false);
        mainMessengerContainer.setManaged(false);
        mainMessengerContainer.getChildren().clear();
    }

    private void showFloatingMessengerChooser() {
        try {
            System.out.println("[DEBUG] showFloatingMessengerChooser appelé");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/CompactConversationList.fxml"));
            Pane compactListRoot = loader.load();
            Controllers.CompactConversationListController ctrl = loader.getController();
            ctrl.setCurrentUser(currentUser);
            ctrl.setOnConversationSelected(conversation -> {
                System.out.println("[DEBUG] Conversation sélectionnée: " + conversation.getId());
                miniMessengersContainer.getChildren().remove(compactListRoot); // Retire la liste, mais ne vide PAS tout le container
                openMiniMessenger(conversation);
            });
            // Avant d'afficher la liste, retire juste les anciennes listes de choix (préserve les mini-messageries ouvertes)
            miniMessengersContainer.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("compactListRoot"));
            compactListRoot.setId("compactListRoot");
            miniMessengersContainer.setVisible(true);
            miniMessengersContainer.setManaged(true);
            miniMessengersContainer.setMouseTransparent(false);
            miniMessengersContainer.getChildren().add(compactListRoot);
            StackPane.setAlignment(compactListRoot, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(compactListRoot, new Insets(0, 32, 100, 0));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Affiche ou masque la fenêtre flottante Messenger
    private void toggleFloatingMessenger() {
        // Si la liste de choix est déjà affichée, on la ferme (mais on ne ferme PAS les mini-messageries ouvertes)
        boolean choixAffiche = miniMessengersContainer.getChildren().stream()
            .anyMatch(node -> node.getId() != null && node.getId().equals("compactListRoot"));
        if (choixAffiche) {
            miniMessengersContainer.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("compactListRoot"));
            System.out.println("[DEBUG] Fermeture de la liste des choix Messenger");
        } else {
            System.out.println("[DEBUG] Ouverture fenêtre Messenger flottante");
            showFloatingMessengerChooser();
        }
    }

    @FXML
    private void onLireSheetsClicked() {
        try {
            System.out.println("[DEBUG] Bouton Lire Google Sheets (back) cliqué");
            final com.google.api.client.http.javanet.NetHttpTransport HTTP_TRANSPORT = com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport();
            com.google.api.services.sheets.v4.Sheets sheetsService = new com.google.api.services.sheets.v4.Sheets.Builder(
                HTTP_TRANSPORT,
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                GoogleApi.getCredentials(HTTP_TRANSPORT))
                .setApplicationName("GestionReclamationApp")
                .build();

            String spreadsheetId = "1TrWsvtqrDd2yja0fTHDxcDD-pjZ6QfIdHmkZcl18Dm0";
            String range = "messages_sheet!A1:F";
            com.google.api.services.sheets.v4.model.ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
            java.util.List<java.util.List<Object>> values = response.getValues();
            System.out.println("[DEBUG] Valeurs récupérées (back) : " + values);
            ObservableList<SheetRow> data = FXCollections.observableArrayList();
            if (values != null && values.size() > 1) {
                // Ignore la première ligne (en-tête)
                for (int i = 1; i < values.size(); i++) {
                    java.util.List<Object> row = values.get(i);
                    String id = row.size() > 0 ? row.get(0).toString() : "";
                    String utilisateur = row.size() > 1 ? row.get(1).toString() : "";
                    String sujet = row.size() > 2 ? row.get(2).toString() : "";
                    String description = row.size() > 3 ? row.get(3).toString() : "";
                    String date = row.size() > 4 ? row.get(4).toString() : "";
                    String statut = row.size() > 5 ? row.get(5).toString() : "";
                    data.add(new SheetRow(id, utilisateur, sujet, description, date, statut));
                }
            } else {
                System.out.println("[DEBUG] Aucune donnée trouvée dans Google Sheets (back).");
            }
            tableSheets.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ERROR] Erreur lors de la lecture Google Sheets (back) : " + e.getMessage());
        }
    }

    @FXML
    private void onExporterSheetClicked() {
        if (selectedReclamations == null || selectedReclamations.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner au moins une réclamation à exporter !");
            return;
        }
        try {
            for (Reclamation rec : selectedReclamations) {
                Object id = rec.getId();
                Object utilisateur = "";
                if (rec.getUserId() != null) {
                    User user = userService.getUserById(rec.getUserId());
                    utilisateur = (user != null) ? user.getName() : rec.getUserId();
                }
                Object sujet = rec.getSujet();
                Object description = rec.getDescription();
                Object date = rec.getDateReclamation() != null ? rec.getDateReclamation().toString() : "";
                Object statut = rec.getStatut();
                GoogleApi.ajouterReclamationDansSheet(
                    "1TrWsvtqrDd2yja0fTHDxcDD-pjZ6QfIdHmkZcl18Dm0",
                    "messages_sheet",
                    id, utilisateur, sujet, description, date, statut
                );
            }
            showAlert("Succès", "Export terminé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Export impossible : " + e.getMessage());
        }
    }

    public static class SheetRow {
        private String a, b, c, d, e, f;
        public SheetRow(String a, String b, String c, String d, String e, String f) {
            this.a = a; this.b = b; this.c = c; this.d = d; this.e = e; this.f = f;
        }
        public String getA() { return a; }
        public String getB() { return b; }
        public String getC() { return c; }
        public String getD() { return d; }
        public String getE() { return e; }
        public String getF() { return f; }
    }
}
