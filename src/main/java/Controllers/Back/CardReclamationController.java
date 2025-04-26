package Controllers.Back;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import models.Reclamation;

public class CardReclamationController {
    @FXML private Text sujetLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label statutLabel;
    @FXML private Label userNameLabel;
    @FXML private Button btnSelect;
    private Reclamation reclamation;

    public void setData(Reclamation reclamation, String userName) {
        this.reclamation = reclamation;
        sujetLabel.setText(reclamation.getSujet());
        descriptionLabel.setText(reclamation.getDescription());
        dateLabel.setText(reclamation.getDateReclamation() != null ? reclamation.getDateReclamation().toString() : "");
        if (userNameLabel != null) {
            userNameLabel.setText(userName);
        } else {
            System.err.println("[ERROR] userNameLabel is null in CardReclamationController. Check FXML binding.");
        }
        if (statutLabel != null) {
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
        }
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public Button getBtnSelect() {
        return btnSelect;
    }
}
