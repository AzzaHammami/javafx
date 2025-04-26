package Controllers.Front;

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

public class ReclamationController implements Initializable {

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

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private Reclamation selectedReclamation;
    private UserService userService = new UserService();
    // Ajout pour gestion utilisateur courant
    private models.User currentUser;
    public void setCurrentUser(models.User user) {
        this.currentUser = user;
        // Recharge les réclamations dès que l'utilisateur courant est défini
        if (cardContainer != null) {
            loadReclamations();
        }
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        reponseService = new ReponseService();
        
        loadReclamations();
        
        // Initialize responses container
        reponsesContainer.setVisible(false);
        reponsesContainer.setManaged(false);
        hideForm();
    }

    private void loadReclamations() {
        cardContainer.getChildren().clear();
        if (currentUser == null) return;
        List<Reclamation> reclamations = reclamationService.getAll();
        for (Reclamation rec : reclamations) {
            if (rec.getUserId() != null && rec.getUserId().equals(currentUser.getId())) {
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
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void handleEnvoyerReclamation(ActionEvent event) {
        if (validateInputs()) {
            // Correction : inclure l'id utilisateur lors de la création de la réclamation
            Integer userId = (currentUser != null) ? currentUser.getId() : null;
            if (userId == null) {
                showAlert("Erreur", "Utilisateur non authentifié !");
                return;
            }
            Reclamation reclamation = new Reclamation(0, sujetField.getText(), descriptionField.getText(), "En attente", LocalDate.now(), userId);
            reclamationService.ajouter(reclamation);
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
                loadReclamations();
                clearForm();
                showAlert("Succès", "Réclamation supprimée avec succès");
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
}
