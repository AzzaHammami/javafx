package Controllers.Back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Reponse;
import models.Reclamation;
import services.ReponseService;
import services.ReclamationService;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReponseController implements Initializable {
    @FXML
    private TableView<Reponse> tableView;
    @FXML
    private TableColumn<Reponse, String> contenuColumn;
    @FXML
    private TableColumn<Reponse, LocalDate> dateColumn;
    @FXML
    private TableColumn<Reponse, String> reclamationColumn;

    @FXML
    private javafx.scene.control.ComboBox<Reclamation> reclamationComboBox;
    @FXML
    private javafx.scene.control.TextArea contenuField;
    @FXML
    private javafx.scene.control.DatePicker datePicker;

    private final ReponseService reponseService = new ReponseService();
    private final ReclamationService reclamationService = new ReclamationService();
    private final ObservableList<Reponse> data = FXCollections.observableArrayList();
    private Reponse selectedReponse;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadReclamations();
        loadReponses();
        setupListeners();
    }

    private void setupColumns() {
        contenuColumn.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateReponse"));
        reclamationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getReclamation().getSujet())
        );
    }

    private void loadReclamations() {
        reclamationComboBox.setItems(FXCollections.observableArrayList(reclamationService.getAll()));
        reclamationComboBox.setConverter(new javafx.util.StringConverter<Reclamation>() {
            @Override
            public String toString(Reclamation reclamation) {
                return reclamation != null ? reclamation.getSujet() : "";
            }

            @Override
            public Reclamation fromString(String string) {
                return null;
            }
        });
    }

    private void loadReponses() {
        data.clear();
        data.addAll(reponseService.getAll());
        tableView.setItems(data);
    }

    private void setupListeners() {
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedReponse = newVal;
            if (newVal != null) {
                contenuField.setText(newVal.getContenu());
                datePicker.setValue(newVal.getDateReponse());
                reclamationComboBox.setValue(newVal.getReclamation());
            }
        });
    }

    private void clearForm() {
        contenuField.clear();
        datePicker.setValue(LocalDate.now());
        reclamationComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleRetourHome(javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/Home.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            // Ajout explicite du CSS pour Home
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddReponse() {
        if (reclamationComboBox.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une réclamation");
            return;
        }

        try {
            Reponse reponse = new Reponse();
            reponse.setContenu(contenuField.getText());
            reponse.setDateReponse(datePicker.getValue());
            reponse.setReclamation(reclamationComboBox.getValue());

            reponseService.ajouter(reponse);
            showAlert("Succès", "Réponse ajoutée avec succès");
            clearForm();
            loadReponses();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout de la réponse: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateReponse() {
        if (selectedReponse == null) {
            showAlert("Erreur", "Veuillez sélectionner une réponse à modifier");
            return;
        }

        try {
            selectedReponse.setContenu(contenuField.getText());
            selectedReponse.setDateReponse(datePicker.getValue());
            selectedReponse.setReclamation(reclamationComboBox.getValue());

            reponseService.modifier(selectedReponse);
            showAlert("Succès", "Réponse modifiée avec succès");
            loadReponses();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification de la réponse: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteReponse() {
        if (selectedReponse == null) {
            showAlert("Erreur", "Veuillez sélectionner une réponse à supprimer");
            return;
        }

        try {
            reponseService.supprimer(selectedReponse.getId());
            showAlert("Succès", "Réponse supprimée avec succès");
            clearForm();
            loadReponses();
            selectedReponse = null;
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la suppression de la réponse: " + e.getMessage());
        }
    }
}
