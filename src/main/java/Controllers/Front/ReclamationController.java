package Controllers.Front;

import models.Conversation;
import models.Reclamation;
import models.Reponse;
import models.User;
import services.ConversationService;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.binding.Bindings;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

public class ReclamationController implements Initializable {
    // Pagination
    private int currentPage = 1;
    private final int pageSize = 5;
    private int totalPages = 1;
    private List<Reclamation> userReclamations = FXCollections.observableArrayList();
    @FXML
    private Button btnPrevPage;
    @FXML
    private Button btnNextPage;
    @FXML
    private Label lblPageInfo;

    // Vérification des bad words via l’API Purgomalum (copie locale pour le contrôleur)
    private boolean contientBadWords(String texte) {
        try {
            String apiUrl = "https://www.purgomalum.com/service/containsprofanity?text=" + java.net.URLEncoder.encode(texte, "UTF-8");
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream()));
            String response = in.readLine();
            in.close();
            return Boolean.parseBoolean(response);
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des bad words : " + e.getMessage());
            return false;
        }
    }

    @FXML
    private BorderPane mainContainer;
    @FXML
    private TextField sujetField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private VBox cardContainer;
    @FXML
    private VBox reponsesContainer;
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private VBox formContainer;
    @FXML
    private Button ajouterButton;
    @FXML
    private Button modifierButton;
    @FXML
    private Button supprimerButton;
    @FXML
    private Button annulerButton;
    @FXML
    private StackPane miniMessengersContainer;
    @FXML
    private Button floatingMessengerButton;

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private Reclamation selectedReclamation;
    private UserService userService = new UserService();
    private ConversationService conversationService = new ConversationService();
    // Ajout pour gestion utilisateur courant
    private models.User currentUser;
    public void setCurrentUser(models.User user) {
        this.currentUser = user;
        // Recharge les réclamations dès que l'utilisateur courant est défini
        if (cardContainer != null) {
            currentPage = 1;
            loadReclamations();
        }
    }

    private final Map<Integer, Node> openMessengerNodesMap = new HashMap<>();

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        reponseService = new ReponseService();
        // Initialiser les boutons/labels de pagination si présents dans le FXML
        if (btnPrevPage != null) btnPrevPage.setOnAction(e -> handlePrevPage());
        if (btnNextPage != null) btnNextPage.setOnAction(e -> handleNextPage());
        loadReclamations();
        reponsesContainer.setVisible(false);
        reponsesContainer.setManaged(false);
        hideForm();
        if (floatingMessengerButton != null) {
            floatingMessengerButton.setOnAction(e -> showConversationListPopup());
        }
        miniMessengersContainer.setDisable(false);
        miniMessengersContainer.setFocusTraversable(true);
    }

    private void loadReclamations() {
        cardContainer.getChildren().clear();
        if (currentUser == null) return;
        // Filtrer les réclamations de l'utilisateur courant
        List<Reclamation> reclamations = reclamationService.getAll();
        userReclamations = reclamations.stream()
                .filter(rec -> rec.getUserId() != null && rec.getUserId().equals(currentUser.getId()))
                .toList();
        // Pagination
        totalPages = (int) Math.ceil((double) userReclamations.size() / pageSize);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, userReclamations.size());
        List<Reclamation> pageList = userReclamations.subList(fromIndex, toIndex);
        for (Reclamation rec : pageList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/CardReclamationFront.fxml"));
                Node card = loader.load();
                CardReclamationFrontController cardController = loader.getController();
                String userName = currentUser.getName();
                cardController.setData(rec, userName);
                cardController.setParentController(this);
                cardContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        updatePaginationControls();
    }

    private void updatePaginationControls() {
        if (lblPageInfo != null) {
            lblPageInfo.setText("Page " + currentPage + " sur " + totalPages);
        }
        if (btnPrevPage != null) {
            btnPrevPage.setDisable(currentPage == 1);
        }
        if (btnNextPage != null) {
            btnNextPage.setDisable(currentPage == totalPages);
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadReclamations();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadReclamations();
        }
    }

    private void clearForm() {
        sujetField.clear();
        descriptionField.clear();
        showAllFormButtons();
        hideForm();
        selectedReclamation = null;
    }

    private boolean validateInputs() {
        String sujet = sujetField.getText().trim();
        String description = descriptionField.getText().trim();

        if (sujet.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return false;
        }

        if (sujet.length() < 5) {
            showAlert("Erreur", "Le sujet doit contenir au moins 5 caractères");
            return false;
        }

        if (description.length() < 10) {
            showAlert("Erreur", "La description doit contenir au moins 10 caractères");
            return false;
        }

        return true;
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
            stage.sizeToScene();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void handleEnvoyerReclamation(ActionEvent event) {
        if (validateInputs()) {
            String sujet = sujetField.getText();
            String description = descriptionField.getText();
            boolean badSujet = contientBadWords(sujet);
            boolean badDesc = contientBadWords(description);

            // Réinitialiser le style
            sujetField.setStyle("");
            descriptionField.setStyle("");

            if (badSujet || badDesc) {
                // Surlignage rouge du champ concerné
                if (badSujet) {
                    sujetField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
                if (badDesc) {
                    descriptionField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                }
                // Focus automatique sur le premier champ à corriger
                if (badSujet) {
                    sujetField.requestFocus();
                } else {
                    descriptionField.requestFocus();
                }
                // Désactivation temporaire du bouton 'ajouterButton'
                ajouterButton.setDisable(true);
                // Alerte personnalisée
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Mots interdits détectés");
                alert.setHeaderText("Votre réclamation contient des mots interdits ");
                String msg = "Merci de corriger le(s) champ(s) en rouge avant de l'envoyer.";
                if (badSujet && badDesc) {
                    msg = "Le sujet ET la description contiennent des mots interdits.";
                } else if (badSujet) {
                    msg = "Le sujet contient des mots interdits.";
                } else if (badDesc) {
                    msg = "La description contient des mots interdits.";
                }
                alert.setContentText(msg);
                alert.showAndWait();
                // Réactivation du bouton après 1 seconde
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> ajouterButton.setDisable(false));
                }).start();
                return;
            }
            // Correction : inclure l'id utilisateur lors de la création de la réclamation
            Integer userId = (currentUser != null) ? currentUser.getId() : null;
            if (userId == null) {
                showAlert("Erreur", "Utilisateur non authentifié !");
                return;
            }
            Reclamation reclamation = new Reclamation(0, sujet, description, "En attente", LocalDate.now(), userId);
            reclamationService.ajouter(reclamation);
            currentPage = 1; // Réinitialiser à la première page après ajout
            loadReclamations();
            clearForm();
            showAlert("Succès", "Réclamation ajoutée avec succès");
        }
    }

    @FXML
    private void handleModifierReclamation(ActionEvent event) {
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation à modifier");
            return;
        }

        if (validateInputs()) {
            selectedReclamation.setSujet(sujetField.getText());
            selectedReclamation.setDescription(descriptionField.getText());

            reclamationService.modifier(selectedReclamation);
            loadReclamations();
            clearForm();
            showAlert("Succès", "Réclamation modifiée avec succès");
        }
    }

    @FXML
    private void handleSupprimerReclamation(ActionEvent event) {
        if (selectedReclamation == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reclamationService.supprimer(selectedReclamation.getId());
                if (userReclamations.size() % pageSize == 1 && currentPage > 1) currentPage--; // Si la page devient vide après suppression, reculer
                loadReclamations();
                clearForm();
                showAlert("Succès", "Réclamation supprimée avec succès!");
            }
        });
    }

    @FXML
    private void handleGoToMessenger(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MessengerView.fxml"));
            Parent messengerView = loader.load();
            // Optional: Pass current user if needed
            Controllers.MessengerController messengerController = loader.getController();
            if (this.currentUser != null) {
                messengerController.setCurrentUser(this.currentUser);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(messengerView);
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture de la messagerie: " + e.getMessage());
        }
    }

    @FXML
    private void handleScrollToForm() {
        showAllFormButtons();
        showForm();
        scrollToForm();
    }

    // Permet de scroller vers la zone du formulaire d'édition
    public void scrollToForm() {
        if (mainScrollPane != null && formContainer != null) {
            mainScrollPane.layout(); // Force le layout pour que les positions soient correctes
            double y = formContainer.getBoundsInParent().getMinY();
            double contentHeight = mainScrollPane.getContent().getBoundsInLocal().getHeight();
            double scrollValue = y / (contentHeight - mainScrollPane.getViewportBounds().getHeight());
            mainScrollPane.setVvalue(Math.max(0, Math.min(1, scrollValue)));
        }
    }

    // Pré-remplit le formulaire pour modification
    public void prefillFormForEdit(Reclamation reclamation) {
        this.selectedReclamation = reclamation;
        sujetField.setText(reclamation.getSujet());
        descriptionField.setText(reclamation.getDescription());
        if (ajouterButton != null) ajouterButton.setVisible(false);
        if (modifierButton != null) modifierButton.setVisible(true);
        if (supprimerButton != null) supprimerButton.setVisible(false);
        if (annulerButton != null) annulerButton.setVisible(true);
        showForm();
    }

    // Suppression depuis la card
    public void deleteReclamationFromCard(Reclamation reclamation) {
        if (reclamation != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation de suppression");
            confirmAlert.setHeaderText(null);
            confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    reclamationService.supprimer(reclamation.getId());
                    loadReclamations();
                    clearForm();
                    showAlert("Succès", "Réclamation supprimée avec succès!");
                }
            });
        }
    }

    // Affiche tous les boutons lors de l'ajout ou après modification/suppression
    private void showAllFormButtons() {
        if (ajouterButton != null) ajouterButton.setVisible(true);
        if (modifierButton != null) modifierButton.setVisible(false);
        if (supprimerButton != null) supprimerButton.setVisible(false);
        if (annulerButton != null) annulerButton.setVisible(true);
    }

    // Affiche et rend visible le formulaire
    private void showForm() {
        if (formContainer != null) {
            formContainer.setVisible(true);
            formContainer.setManaged(true);
        }
    }

    // Cache le formulaire
    private void hideForm() {
        if (formContainer != null) {
            formContainer.setVisible(false);
            formContainer.setManaged(false);
        }
    }

    @FXML
    private void handleAnnulerForm() {
        clearForm();
    }

    // Affiche une liste de conversations dans une VBox flottante
    private VBox conversationListPopup = null;

    private void showConversationListPopup() {
        // Avant d'afficher la popup, réactive le conteneur
        miniMessengersContainer.setMouseTransparent(false);
        miniMessengersContainer.setDisable(false);
        miniMessengersContainer.setFocusTraversable(true);
        if (conversationListPopup != null && miniMessengersContainer.getChildren().contains(conversationListPopup)) {
            miniMessengersContainer.getChildren().remove(conversationListPopup);
            conversationListPopup = null;
            return;
        }
        if (currentUser == null) return;
        java.util.List<models.Conversation> conversations = conversationService.getUserConversations(currentUser.getId());
        conversationListPopup = new VBox(10);
        conversationListPopup.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3);");
        conversationListPopup.setMaxWidth(260);
        conversationListPopup.setMinWidth(180);
        conversationListPopup.setMouseTransparent(false);
        conversationListPopup.setPickOnBounds(false);
        conversationListPopup.setDisable(false);
        conversationListPopup.setFocusTraversable(true);
        System.out.println("[DEBUG] Nb conversations trouvées : " + conversations.size());

        // Ajout d'un bouton de debug tout en haut de la popup Messenger
        Button debugBtn = new Button("DEBUG TEST");
        debugBtn.setStyle("-fx-background-color: #ffaaaa; -fx-font-size: 16px; -fx-font-weight: bold;");
        debugBtn.setOnAction(ev -> System.out.println("[DEBUG] BOUTON DEBUG CLICKED"));
        conversationListPopup.getChildren().add(0, debugBtn);

        for (models.Conversation conv : conversations) {
            System.out.println("[DEBUG] Conversation : " + conv.getId() + " - " + conv.getTitle());
            System.out.println("[DEBUG] Participants for conversation id=" + conv.getId() + " : " + (conv.getParticipants() != null ? conv.getParticipants().size() : "null"));
            String displayTitle = conv.getTitle();
            if ((displayTitle == null || displayTitle.trim().isEmpty()) && !conv.isGroup() && conv.getParticipants() != null) {
                for (models.User participant : conv.getParticipants()) {
                    if (participant.getId() != currentUser.getId()) {
                        displayTitle = participant.getName();
                        break;
                    }
                }
            }
            if (displayTitle == null || displayTitle.trim().isEmpty()) displayTitle = "Conversation";
            Button btn = new Button(displayTitle);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setDisable(false);
            btn.setFocusTraversable(true);
            btn.setMouseTransparent(false);
            btn.setPickOnBounds(false);
            btn.setStyle("-fx-background-color: #f1f0f0; -fx-background-radius: 6; -fx-font-size: 15px; -fx-text-fill: #222; -fx-padding: 8 12; -fx-alignment: CENTER_LEFT;");
            btn.setOnAction(e -> {
                System.out.println("[DEBUG] Clic sur le bouton conversation id=" + conv.getId());
                e.consume();
                miniMessengersContainer.getChildren().remove(conversationListPopup);
                conversationListPopup = null;
                openMiniMessenger(conv);
            });
            conversationListPopup.getChildren().add(btn);
        }
        // Si aucune conversation
        if (conversations.isEmpty()) {
            Label emptyLabel = new Label("Aucune conversation trouvée");
            emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
            conversationListPopup.getChildren().add(emptyLabel);
        }
        // Correction : forcer la popup et ses parents à être interactifs et au premier plan
        conversationListPopup.setMouseTransparent(false);
        conversationListPopup.setPickOnBounds(false);
        conversationListPopup.setDisable(false);
        conversationListPopup.setFocusTraversable(true);
        if (miniMessengersContainer != null) {
            miniMessengersContainer.setMouseTransparent(false);
            miniMessengersContainer.setPickOnBounds(false);
            miniMessengersContainer.setDisable(false);
            miniMessengersContainer.setFocusTraversable(true);
        }
        System.out.println("[DEBUG] miniMessengersContainer children count: " + miniMessengersContainer.getChildren().size());
        StackPane.setAlignment(conversationListPopup, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(conversationListPopup, new javafx.geometry.Insets(0, 80, 90, 0));
        if (!miniMessengersContainer.getChildren().contains(conversationListPopup)) {
            miniMessengersContainer.getChildren().add(conversationListPopup);
        }
        conversationListPopup.toFront();
        System.out.println("[DEBUG] conversationListPopup added. Children now: " + miniMessengersContainer.getChildren().size());
    }

    private void openMiniMessenger(models.Conversation conversation) {
        if (conversation == null) return;
        if (openMessengerNodesMap.containsKey(conversation.getId())) {
            Node node = openMessengerNodesMap.get(conversation.getId());
            node.toFront();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/MiniMessengerView.fxml"));
            VBox miniMessengerPane = loader.load();
            Controllers.Front.MiniMessengerController messengerController = loader.getController();
            messengerController.setConversation(conversation, currentUser, () -> closeMiniMessenger(conversation.getId()));
            miniMessengerPane.setVisible(true);
            miniMessengerPane.setManaged(true);
            miniMessengerPane.setMouseTransparent(false);
            miniMessengerPane.setPickOnBounds(false);
            int index = miniMessengersContainer.getChildren().size();
            int horizontalOffset = 90 + (index * 360);
            int bottomOffset = 90;
            StackPane.setAlignment(miniMessengerPane, javafx.geometry.Pos.BOTTOM_RIGHT);
            StackPane.setMargin(miniMessengerPane, new javafx.geometry.Insets(0, horizontalOffset, bottomOffset, 0));
            miniMessengersContainer.getChildren().add(miniMessengerPane);
            miniMessengerPane.toFront();
            openMessengerNodesMap.put(conversation.getId(), miniMessengerPane);
            System.out.println("[DEBUG] miniMessengersContainer children: " + miniMessengersContainer.getChildren().size());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeMiniMessenger(int conversationId) {
        Node node = openMessengerNodesMap.get(conversationId);
        if (node != null) {
            miniMessengersContainer.getChildren().remove(node);
            openMessengerNodesMap.remove(conversationId);
        }
        // Correction : si plus aucun mini-messenger n'est ouvert, s'assurer que le conteneur redevient interactif
        if (miniMessengersContainer.getChildren().isEmpty()) {
            miniMessengersContainer.setMouseTransparent(true);
            miniMessengersContainer.setDisable(true);
            miniMessengersContainer.setFocusTraversable(false);
        }
    }
}
