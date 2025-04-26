package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.models.Rating;
import com.example.rendez_vous.services.ServiceRating;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class RatingController {
    @FXML private HBox starsContainer;
    @FXML private TextArea commentField;
    @FXML private Label averageRatingLabel;

    private int selectedRating = 0;
    private int medecinId;
    private int userId;
    private ServiceRating serviceRating;

    public void initialize(int medecinId, int userId) {
        this.medecinId = medecinId;
        this.userId = userId;
        this.serviceRating = new ServiceRating();

        // Créer les étoiles interactives
        createStarRatingUI();
        updateAverageRating();
    }

    private void createStarRatingUI() {
        starsContainer.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-font-size: 24px; -fx-cursor: hand;");
            final int ratingValue = i;

            star.setOnMouseEntered(e -> highlightStars(ratingValue));
            star.setOnMouseExited(e -> resetStars());
            star.setOnMouseClicked(e -> selectRating(ratingValue));

            starsContainer.getChildren().add(star);
        }
    }

    private void highlightStars(int upTo) {
        for (int i = 0; i < starsContainer.getChildren().size(); i++) {
            Label star = (Label) starsContainer.getChildren().get(i);
            star.setTextFill(i < upTo ? Color.GOLD : Color.GRAY);
        }
    }

    private void resetStars() {
        highlightStars(selectedRating);
    }

    private void selectRating(int rating) {
        selectedRating = rating;
        highlightStars(rating);
    }

    private void updateAverageRating() {
        try {
            double moyenne = serviceRating.getMoyenneRatingMedecin(medecinId);
            averageRatingLabel.setText(String.format("Note moyenne: %.1f/5", moyenne));
        } catch (SQLException e) {
            e.printStackTrace();
            averageRatingLabel.setText("Note moyenne: -");
        }
    }

    @FXML
    private void handleSubmitRating() {
        if (selectedRating == 0) {
            showAlert("Veuillez sélectionner une note");
            return;
        }

        Rating newRating = new Rating();
        newRating.setObjectId(medecinId);
        newRating.setObjectType("medecin");
        newRating.setRatingValue(selectedRating);
        newRating.setComment(commentField.getText());
        newRating.setUserId(userId);
        newRating.setTimestamp(LocalDateTime.now());

        try {
            if (serviceRating.userHasRated(medecinId, userId)) {
                showAlert("Vous avez déjà noté ce médecin");
                return;
            }

            if (serviceRating.ajouterRating(newRating)) {
                showAlert("Merci pour votre évaluation !");
                // Fermer la fenêtre après soumission
                ((Stage) starsContainer.getScene().getWindow()).close();
            } else {
                showAlert("Erreur lors de l'enregistrement");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}