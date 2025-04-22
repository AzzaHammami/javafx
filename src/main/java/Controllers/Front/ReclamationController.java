package Controllers.Front;

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
    private TableView<Reponse> reponsesTableView;
    @FXML
    private TableColumn<Reponse, String> reponseContenuColumn;
    @FXML
    private TableColumn<Reponse, LocalDate> reponseDateColumn;
    @FXML
    private VBox reponsesContainer;

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private Reclamation selectedReclamation;
    // Ajout pour gestion utilisateur courant
    private models.User currentUser;
    public void setCurrentUser(models.User user) {
        this.currentUser = user;
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        reponseService = new ReponseService();
        
        setupTableColumns();
        setupReponseColumns();
        loadReclamations();
        
        // Initialize responses container
        reponsesContainer.setVisible(false);
        reponsesContainer.setManaged(false);
        
        // Setup selection listener
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedReclamation = newSelection;
            if (newSelection != null) {
                sujetField.setText(newSelection.getSujet());
                descriptionField.setText(newSelection.getDescription());
                reponsesContainer.setVisible(true);
                reponsesContainer.setManaged(true);
                loadReponses();
            } else {
                sujetField.clear();
                descriptionField.clear();
                reponsesContainer.setVisible(false);
                reponsesContainer.setManaged(false);
            }
        });
    }

    private void setupTableColumns() {
        // Set column widths as percentages
        sujetColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
        descriptionColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.4));
        dateColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        statutColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        
        // Set cell value factories
        sujetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSujet()));
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDateReclamation()));
        statutColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));
    }

    private void setupReponseColumns() {
        reponseContenuColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getContenu()));
        reponseDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getDateReponse()));
    }

    private void loadReclamations() {
        ObservableList<Reclamation> data = FXCollections.observableArrayList();
        data.addAll(reclamationService.getAll());
        tableView.setItems(data);
    }

    private void loadReponses() {
        if (selectedReclamation != null) {
            ObservableList<Reponse> reponses = FXCollections.observableArrayList();
            reponses.addAll(reponseService.getReponsesByReclamation(selectedReclamation.getId()));
            reponsesTableView.setItems(reponses);
        }
    }

    private void clearForm() {
        sujetField.clear();
        descriptionField.clear();
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
            Reclamation reclamation = new Reclamation();
            reclamation.setSujet(sujetField.getText());
            reclamation.setDescription(descriptionField.getText());
            reclamation.setDateReclamation(LocalDate.now());
            reclamation.setStatut("En attente");
            
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
            tableView.getSelectionModel().clearSelection();
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
                tableView.getSelectionModel().clearSelection();
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
}
