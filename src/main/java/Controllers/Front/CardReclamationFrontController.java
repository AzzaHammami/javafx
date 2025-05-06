package Controllers.Front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.Parent;
import models.Reclamation;
import Controllers.Front.ReclamationController;

public class CardReclamationFrontController {
    @FXML private Text sujetLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label statutLabel;
    @FXML private Label userNameLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox reponsesContainer;
    @FXML private VBox rootCard;
    private Reclamation reclamation;
    private ReclamationController parentController;
    private final services.ReponseService reponseService = new services.ReponseService();
    private boolean reponsesVisible = false;

    public void setData(Reclamation reclamation, String userName) {
        this.reclamation = reclamation;
        sujetLabel.setText(reclamation.getSujet());
        descriptionLabel.setText(reclamation.getDescription());
        dateLabel.setText(reclamation.getDateReclamation() != null ? reclamation.getDateReclamation().toString() : "");
        userNameLabel.setText(userName);
        statutLabel.setText(reclamation.getStatut());
        // Couleur dynamique selon l'état
        String statut = reclamation.getStatut().toLowerCase();
        if (statut.contains("attente")) {
            statutLabel.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 14 3 14; -fx-background-radius: 8; -fx-font-size: 13;");
        } else if (statut.contains("cours")) {
            statutLabel.setStyle("-fx-background-color: #FFA726; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 14 3 14; -fx-background-radius: 8; -fx-font-size: 13;");
        } else if (statut.contains("traitée") || statut.contains("resolu") || statut.contains("résolu")) {
            statutLabel.setStyle("-fx-background-color: #43A047; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 14 3 14; -fx-background-radius: 8; -fx-font-size: 13;");
        } else {
            statutLabel.setStyle("-fx-background-color: #BDBDBD; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 14 3 14; -fx-background-radius: 8; -fx-font-size: 13;");
        }
        // Par défaut, masquer les réponses
        if (reponsesContainer != null) {
            reponsesContainer.setVisible(false);
            reponsesContainer.setManaged(false);
        }
        // Ajout du listener de clic (une seule fois)
        if (rootCard != null) {
            rootCard.setOnMouseClicked(e -> toggleReponses());
        }
    }

    private void toggleReponses() {
        if (reponsesContainer == null) return;
        if (!reponsesVisible) {
            reponsesContainer.getChildren().clear();
            java.util.List<models.Reponse> reponses = reponseService.getReponsesByReclamation(reclamation.getId());
            if (reponses.isEmpty()) {
                Label noReponse = new Label("Aucune réponse pour cette réclamation.");
                noReponse.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 7 0 7 0;");
                reponsesContainer.getChildren().add(noReponse);
            } else {
                Label titre = new Label("Réponses :");
                titre.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #4958c5; -fx-padding: 0 0 5 0;");
                reponsesContainer.getChildren().add(titre);
                for (models.Reponse rep : reponses) {
                    Label lbl = new Label("\u25CF " + rep.getDateReponse() + " : " + rep.getContenu());
                    lbl.setStyle("-fx-background-color: #e3eafd; -fx-padding: 10 16 10 16; -fx-background-radius: 8; -fx-font-size: 13; -fx-text-fill: #222; -fx-margin: 4 0 4 0;");
                    lbl.setWrapText(true);
                    reponsesContainer.getChildren().add(lbl);
                }
            }
            reponsesContainer.setVisible(true);
            reponsesContainer.setManaged(true);
        } else {
            reponsesContainer.setVisible(false);
            reponsesContainer.setManaged(false);
        }
        reponsesVisible = !reponsesVisible;
    }

    public void setParentController(ReclamationController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (parentController != null && reclamation != null) {
            parentController.prefillFormForEdit(reclamation);
            parentController.scrollToForm(); // Ajout : scroll automatique vers la zone de modification
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (parentController != null && reclamation != null) {
            parentController.deleteReclamationFromCard(reclamation);
        }
    }
}
