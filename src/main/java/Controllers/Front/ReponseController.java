package Controllers.Front;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Reponse;
import models.Reclamation;
import services.ReponseService;
import services.ReclamationService;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReponseController implements Initializable {
    @FXML
    private TableView<Reponse> tableView;


    @FXML
    private TextArea contenuField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<Reclamation> reclamationComboBox;



    @FXML
    private TableColumn<Reponse, String> contenuColumn;

    @FXML
    private TableColumn<Reponse, LocalDate> dateColumn;

    @FXML
    private TableColumn<Reponse, Reclamation> reclamationColumn;

    private final ReponseService reponseService = new ReponseService();
    private final ReclamationService reclamationService = new ReclamationService();

    private ObservableList<Reponse> reponseList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contenuColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContenu()));
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateReponse()));
        reclamationColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getReclamation()));


        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {

                contenuField.setText(newSelection.getContenu());
                datePicker.setValue(newSelection.getDateReponse());
                reclamationComboBox.setValue(newSelection.getReclamation());
            }
        });


        datePicker.setValue(LocalDate.now());

        chargerReponses();
        chargerReclamations();
    }

    private void chargerReponses() {
        reponseList.setAll(reponseService.getAll());
        tableView.setItems(reponseList);
    }

    private void chargerReclamations() {
        List<Reclamation> reclamations = reclamationService.getAll();

        reclamationComboBox.setItems(FXCollections.observableArrayList(reclamations));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void ajouterReponse() {
        String contenu = contenuField.getText();
        LocalDate date = datePicker.getValue();
        Reclamation selectedReclamation = reclamationComboBox.getValue();

        if (contenu.isEmpty() || date == null || selectedReclamation == null) {
            showAlert("Information", "Veuillez remplir tous les champs.");
            return;
        }

        Reponse reponse = new Reponse(0, contenu, date, selectedReclamation);
        reponseService.ajouter(reponse);
        chargerReponses();
        clearFields();
    }

    private void clearFields() {
        contenuField.clear();
        datePicker.setValue(LocalDate.now());
        reclamationComboBox.setValue(null);
    }

    @FXML
    private void modifierReponse() {
        Reponse selectedReponse = tableView.getSelectionModel().getSelectedItem();
        if (selectedReponse == null) {
            showAlert("Information", "Veuillez sélectionner une réponse à modifier.");
            return;
        }

        String contenu = contenuField.getText();
        LocalDate date = datePicker.getValue();
        Reclamation selectedReclamation = reclamationComboBox.getValue();

        if (contenu.isEmpty() || date == null || selectedReclamation == null) {
            showAlert("Information", "Veuillez remplir tous les champs.");
            return;
        }

        selectedReponse.setContenu(contenu);
        selectedReponse.setDateReponse(date);
        selectedReponse.setReclamation(selectedReclamation);

        reponseService.modifier(selectedReponse);
        chargerReponses();
        clearFields();
        showAlert("Succès", "Réponse modifiée avec succès!");
    }

    @FXML
    private void supprimerReponse() {
        Reponse selectedReponse = tableView.getSelectionModel().getSelectedItem();
        if (selectedReponse == null) {
            showAlert("Information", "Veuillez sélectionner une réponse à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reponseService.supprimer(selectedReponse.getId());
                chargerReponses();
                clearFields();
                showAlert("Succès", "Réponse supprimée avec succès!");
            }
        });
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
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du retour à l'accueil: " + e.getMessage());
        }
    }
}
