package com.example.rendez_vous.controllers.Front;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.example.rendez_vous.models.User;
import com.example.rendez_vous.services.ServiceUser;
import java.util.List;
import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import com.example.rendez_vous.models.Rating;
import com.example.rendez_vous.services.ServiceRating;
import java.time.LocalDateTime;

public class PatientDashboardView extends BorderPane {

    private User selectedMedecin; // Track the currently selected doctor
    private User selectedMedecinToRate = null;
    private VBox ratingSectionBox = null;
    private final ServiceRating ratingService = new ServiceRating();

    public PatientDashboardView() {
        // Configuration de l'en-tête
        HBox header = createHeader();
        this.setTop(header);

        // Zone principale
        this.setCenter(createMainContent());

        // Style global
        this.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa);");
    }

    public VBox createMainContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30, 40, 30, 40));

        Label welcomeLabel = new Label("Bonjour, Thomas");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label summaryLabel = new Label("Voici le résumé de votre santé");
        summaryLabel.setFont(Font.font("System", 14));
        summaryLabel.setTextFill(Color.web("#8898aa"));

        HBox tiles = createInfoTiles();
        tiles.setSpacing(20);

        VBox medecinsSection = createMedecinsSection();

        // Section rating dynamique en bas
        ratingSectionBox = createRatingSection();
        ratingSectionBox.setId("ratingSection");

        // Changer le titre par "Rating" en orange
        Label titreRating = new Label("Rating");
        titreRating.setFont(Font.font("System", FontWeight.BOLD, 20));
        titreRating.setStyle("-fx-text-fill: #ff9800;");
        VBox tableauRdv = new VBox();
        tableauRdv.getChildren().addAll(titreRating/*, appointmentCrudWindow.getContentWithDateSelector()*/);

        mainContent.getChildren().addAll(welcomeLabel, summaryLabel, new VBox(5, tiles), medecinsSection, tableauRdv, ratingSectionBox);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return new VBox(scrollPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(10);
        header.setPrefHeight(70);
        header.setStyle("-fx-background-color: linear-gradient(to right, #5e72e4, #825ee4);");
        header.setPadding(new Insets(0, 20, 0, 20));

        Circle logoCircle = new Circle(20, Color.WHITE);
        Label appName = new Label("MaSanté");
        appName.setFont(Font.font("System", FontWeight.BOLD, 18));
        appName.setTextFill(Color.WHITE);

        HBox menu = new HBox(20);
        menu.setTranslateX(40);
        menu.setTranslateY(25);

        Label homeLink = new Label("Accueil");
        homeLink.setTextFill(Color.WHITE);
        homeLink.setFont(Font.font("System", 14));
        homeLink.setOnMouseClicked(e -> {
            this.setCenter(createMainContent());
        });

        Label appointmentLink = new Label("Rendez-vous");
        appointmentLink.setTextFill(Color.web("rgba(255,255,255,0.7)"));
        appointmentLink.setFont(Font.font("System", 14));
        appointmentLink.setOnMouseClicked(e -> {
            AppointmentCrudWindow crudWindow = new AppointmentCrudWindow();
            this.setCenter(crudWindow.getContentWithDateSelector());
        });

        Label documentsLink = new Label("Documents");
        documentsLink.setTextFill(Color.web("rgba(255,255,255,0.7)"));
        documentsLink.setFont(Font.font("System", 14));

        Label messagesLink = new Label("Messages");
        messagesLink.setTextFill(Color.web("rgba(255,255,255,0.7)"));
        messagesLink.setFont(Font.font("System", 14));

        menu.getChildren().addAll(homeLink, appointmentLink, documentsLink, messagesLink);

        Circle profileCircle = new Circle(15, Color.WHITE);

        Button returnButton = new Button("Retour");
        returnButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 5;");
        returnButton.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Views/landing.fxml"));
                Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, 1024, 768);
                stage.setScene(scene);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(logoCircle, appName, menu, spacer, returnButton, profileCircle);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        return header;
    }

    private HBox createInfoTiles() {
        VBox appointmentTile = createTile("Prochain RDV", "#5e72e4",
                "Mercredi 23 Avril", "Dr. Marie Laurent", "Consultation générale");
        appointmentTile.setOnMouseClicked(e -> {
            AppointmentCrudWindow crudWindow = new AppointmentCrudWindow();
            this.setCenter(crudWindow.getContentWithDateSelector());
        });

        VBox medicationTile = createTile("Médicaments", "#ff5e5e",
                "Amoxicilline - 2x/jour", "Doliprane - si besoin", "");

        ProgressBar progress = new ProgressBar();
        progress.setProgress(0.6);
        progress.setPrefWidth(140);
        progress.setStyle("-fx-accent: #ff5e5e;");
        medicationTile.getChildren().add(progress);

        VBox notificationTile = createTile("Notifications", "#11cdef",
                "Résultats disponibles", "Rappel vaccination", "");

        HBox tiles = new HBox(20);
        tiles.getChildren().addAll(appointmentTile, medicationTile, notificationTile);

        return tiles;
    }

    private VBox createTile(String title, String color, String line1, String line2, String line3) {
        VBox tile = new VBox(5);
        tile.setPrefSize(220, 140);
        tile.setPadding(new Insets(20));
        tile.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        HBox titleBox = new HBox(10);
        Circle iconCircle = new Circle(20);
        iconCircle.setFill(Color.web(color, 0.1));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleBox.getChildren().addAll(iconCircle, titleLabel);

        Label contentLabel1 = new Label(line1);
        contentLabel1.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        contentLabel1.setTextFill(Color.web(color));

        Label contentLabel2 = new Label(line2);
        contentLabel2.setFont(Font.font("System", 13));
        contentLabel2.setTextFill(Color.web("#8898aa"));

        VBox contentBox = new VBox(5);
        contentBox.getChildren().addAll(contentLabel1, contentLabel2);

        if (!line3.isEmpty()) {
            Label contentLabel3 = new Label(line3);
            contentLabel3.setFont(Font.font("System", 13));
            contentLabel3.setTextFill(Color.web("#8898aa"));
            contentBox.getChildren().add(contentLabel3);
        }

        tile.getChildren().addAll(titleBox, contentBox);
        return tile;
    }

    private VBox medecinsList;
    private Timeline medecinsRefreshTimeline;

    private VBox createMedecinsSection() {
        VBox medecinsSection = new VBox(18);
        medecinsSection.setPadding(new Insets(30, 0, 0, 0));
        Label medecinsTitle = new Label("Médecins disponibles");
        medecinsTitle.setFont(Font.font("System", FontWeight.BOLD, 19));
        medecinsTitle.setStyle("-fx-text-fill: #222;");
        medecinsList = new VBox(15);
        medecinsList.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 12; -fx-padding: 18 18 18 18;");

        refreshMedecinsList();

        medecinsRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> refreshMedecinsList())
        );
        medecinsRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        medecinsRefreshTimeline.play();

        medecinsSection.getChildren().addAll(medecinsTitle, medecinsList);
        return medecinsSection;
    }

    private void refreshMedecinsList() {
        medecinsList.getChildren().clear();
        ServiceUser serviceUser = new ServiceUser();
        List<User> medecins = serviceUser.getAllMedecins();
        for (User medecin : medecins) {
            String imageUrl = medecin.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                imageUrl = new File(imageUrl).toURI().toString();
            }
            medecinsList.getChildren().add(
                    createMedecinCard(medecin, imageUrl)
            );
        }
    }

    private HBox createMedecinCard(User medecin, String imageUrl) {
        HBox card = new HBox(18);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #fff; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(90,120,255,0.06), 12, 0, 0, 2);");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ImageView photo = new ImageView(imageUrl != null ? new Image(imageUrl) : null);
        photo.setFitWidth(70);
        photo.setFitHeight(70);
        photo.setStyle("-fx-background-radius: 50; -fx-border-radius: 50;");

        VBox infos = new VBox(3);
        Label nameLabel = new Label("Dr " + medecin.getName());
        nameLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1976d2;");
        // Affichage de la spécialité et de la moyenne des notes
        ServiceRating ratingService = new ServiceRating();
        double moyenne = 0.0;
        try {
            moyenne = ratingService.getMoyenneRatingMedecin(medecin.getId());
        } catch (Exception e) {
            // Optionnel : log l'erreur
        }
        String specialiteEtNote = medecin.getSpecialite() + (moyenne > 0 ? String.format("  |  Note : %.1f/5", moyenne) : "  |  Pas de note");
        Label specialiteLabel = new Label(specialiteEtNote);
        specialiteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        // Affichage graphique des étoiles
        HBox starsBox = new HBox(2);
        int fullStars = (int) moyenne;
        boolean halfStar = (moyenne - fullStars) >= 0.5;
        for (int i = 0; i < fullStars; i++) {
            Label star = new Label("★");
            star.setStyle("-fx-text-fill: #FFD600; -fx-font-size: 18px;");
            starsBox.getChildren().add(star);
        }
        if (halfStar) {
            Label half = new Label("☆");
            half.setStyle("-fx-text-fill: #FFD600; -fx-font-size: 18px;");
            starsBox.getChildren().add(half);
        }
        for (int i = fullStars + (halfStar ? 1 : 0); i < 5; i++) {
            Label empty = new Label("☆");
            empty.setStyle("-fx-text-fill: #FFD600; -fx-font-size: 18px;");
            starsBox.getChildren().add(empty);
        }

        infos.getChildren().addAll(nameLabel, specialiteLabel, starsBox);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button rdvBtn = new Button("\uD83D\uDCC5 Prendre Rendez-vous");
        rdvBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 15px; -fx-padding: 8 18;");

        rdvBtn.setOnAction(e -> {
            this.selectedMedecin = medecin;
            BorderPane root = (BorderPane) card.getScene().getRoot();
            root.setCenter(new AppointmentCrudWindow(
                    medecin.getId(),
                    "Dr " + medecin.getName(),
                    medecin.getSpecialite(),
                    imageUrl
            ).getContentWithDateSelector());
        });

        // Ajout du bouton Rating
        Button ratingBtn = new Button("★ Noter");
        ratingBtn.setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ff9800; -fx-font-weight: bold; -fx-background-radius: 8; -fx-font-size: 15px; -fx-padding: 8 18; -fx-border-color: #ff9800; -fx-border-width: 2;");
        ratingBtn.setOnAction(e -> scrollToRatingSection(medecin));

        card.getChildren().addAll(photo, infos, spacer, rdvBtn, ratingBtn);
        return card;
    }

    // Méthode pour scroller jusqu'à la section rating
    private void scrollToRatingSection(User medecin) {
        updateRatingSection(medecin);
        // Trouver le ScrollPane contenant la page
        final VBox[] mainContent = {null};
        final ScrollPane[] scrollPane = {null};
        // On récupère le ScrollPane qui contient le ratingSectionBox
        if (this.getCenter() instanceof VBox) {
            VBox centerVBox = (VBox) this.getCenter();
            for (javafx.scene.Node node : centerVBox.getChildren()) {
                if (node instanceof ScrollPane) {
                    scrollPane[0] = (ScrollPane) node;
                    if (scrollPane[0].getContent() instanceof VBox) {
                        mainContent[0] = (VBox) scrollPane[0].getContent();
                    }
                    break;
                }
            }
        }
        if (scrollPane[0] != null && mainContent[0] != null && ratingSectionBox != null) {
            Platform.runLater(() -> {
                double nodeY = ratingSectionBox.getBoundsInParent().getMinY();
                double contentHeight = mainContent[0].getHeight();
                double viewportHeight = scrollPane[0].getViewportBounds().getHeight();
                double vValue = nodeY / (contentHeight - viewportHeight);
                scrollPane[0].setVvalue(Math.max(0, Math.min(1, vValue)));
                ratingSectionBox.requestFocus();
            });
        }
    }

    // Section rating dynamique
    private VBox createRatingSection() {
        VBox box = new VBox(16);
        box.setPadding(new Insets(40, 0, 30, 0));
        box.setStyle("-fx-background-color: #fffde7; -fx-background-radius: 12; -fx-border-color: #ffe082; -fx-border-radius: 12; -fx-border-width: 2; -fx-alignment: center;");
        box.setAlignment(Pos.CENTER);
        Label titre = new Label("Noter un médecin");
        titre.setFont(Font.font("System", FontWeight.BOLD, 19));
        titre.setStyle("-fx-text-fill: #ff9800;");
        box.getChildren().add(titre);
        updateRatingSection(null); // Affichage initial
        return box;
    }

    // Met à jour dynamiquement la section rating selon le médecin choisi
    private void updateRatingSection(User medecin) {
        this.selectedMedecinToRate = medecin;
        if (ratingSectionBox == null) return;
        ratingSectionBox.getChildren().removeIf(n -> n.getId() != null && n.getId().equals("dynamicRating"));
        if (medecin == null) {
            Label info = new Label("Clique sur le bouton ★ Noter d'un médecin pour donner une note.");
            info.setId("dynamicRating");
            info.setStyle("-fx-font-size: 15px; -fx-text-fill: #888;");
            ratingSectionBox.getChildren().add(info);
        } else {
            VBox dynamic = new VBox(14);
            dynamic.setId("dynamicRating");
            dynamic.setAlignment(Pos.CENTER);

            // PHOTO DU MEDECIN
            String imageUrl = medecin.getImageUrl();
            ImageView photoView = null;
            if (imageUrl != null && !imageUrl.isEmpty()) {
                if (!imageUrl.startsWith("http")) {
                    imageUrl = new java.io.File(imageUrl).toURI().toString();
                }
                photoView = new ImageView(new Image(imageUrl, 80, 80, true, true));
                photoView.setStyle("-fx-background-radius: 50; -fx-border-radius: 50; -fx-effect: dropshadow(gaussian, #ffd600, 8, 0, 0, 2);");
                photoView.setClip(new javafx.scene.shape.Circle(40, 40, 40));
            }

            Label medecinLabel = new Label("Noter le Dr " + medecin.getName() + " (" + medecin.getSpecialite() + ")");
            medecinLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #222;");

            // Sélecteur d'étoiles interactif SANS cercles radio, AVEC effet hover
            HBox starBox = new HBox(6);
            starBox.setAlignment(Pos.CENTER);
            Label[] stars = new Label[5];
            final int[] selectedValue = {0};
            for (int i = 0; i < 5; i++) {
                final int starValue = i + 1;
                Label star = new Label("★");
                star.setStyle("-fx-font-size: 32px; -fx-text-fill: #bdbdbd; -fx-cursor: hand;");
                // Effet hover
                star.setOnMouseEntered(e -> {
                    for (int j = 0; j < 5; j++) {
                        if (j < starValue) {
                            stars[j].setStyle("-fx-font-size: 32px; -fx-text-fill: #FFD600; -fx-cursor: hand;");
                        } else {
                            stars[j].setStyle("-fx-font-size: 32px; -fx-text-fill: #bdbdbd; -fx-cursor: hand;");
                        }
                    }
                });
                star.setOnMouseExited(e -> {
                    for (int j = 0; j < 5; j++) {
                        if (j < selectedValue[0]) {
                            stars[j].setStyle("-fx-font-size: 32px; -fx-text-fill: #FFD600; -fx-cursor: hand;");
                        } else {
                            stars[j].setStyle("-fx-font-size: 32px; -fx-text-fill: #bdbdbd; -fx-cursor: hand;");
                        }
                    }
                });
                star.setOnMouseClicked(e -> {
                    selectedValue[0] = starValue;
                    for (int j = 0; j < 5; j++) {
                        if (j < starValue) {
                            stars[j].setStyle("-fx-font-size: 32px; -fx-text-fill: #FFD600; -fx-cursor: hand;");
                        } else {
                            stars[j].setStyle("-fx-font-size: 32px; -fx-text-fill: #bdbdbd; -fx-cursor: hand;");
                        }
                    }
                    starBox.setUserData(starValue);
                });
                stars[i] = star;
                starBox.getChildren().add(star);
            }
            // Par défaut aucune étoile sélectionnée
            starBox.setUserData(null);

            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Laisse un commentaire (facultatif)");
            commentArea.setPrefRowCount(2);
            commentArea.setMaxWidth(340);

            Button envoyerBtn = new Button("Envoyer la note");
            envoyerBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 17px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 36;");
            envoyerBtn.setOnAction(e -> {
                Object selected = starBox.getUserData();
                if (selected == null) {
                    showAlert("Erreur", "Merci de sélectionner une note en étoiles.");
                    return;
                }
                int ratingValue = (int) selected;
                String comment = commentArea.getText();
                int userId = getCurrentUserId();
                Rating rating = new Rating(0, medecin.getId(), "medecin", ratingValue, comment, userId, java.time.LocalDateTime.now());
                try {
                    if (ratingService.userHasRated(medecin.getId(), userId)) {
                        showAlert("Déjà noté", "Vous avez déjà noté ce médecin.");
                        return;
                    }
                    ratingService.ajouterRating(rating);
                    showAlert("Merci !", "Votre note a bien été enregistrée pour Dr " + medecin.getName());
                } catch (Exception ex) {
                    showAlert("Erreur", "Impossible d'enregistrer la note : " + ex.getMessage());
                }
            });
            dynamic.getChildren().addAll(photoView, medecinLabel, starBox, commentArea, envoyerBtn);
            ratingSectionBox.getChildren().add(dynamic);
        }
    }

    // Méthode utilitaire pour récupérer l'id utilisateur courant (à adapter à ton système d'authentification)
    private int getCurrentUserId() {
        // Ex : récupérer depuis une session, un singleton, ou un champ User courant
        // Ici, retourne une valeur fictive pour le test
        return 1;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private class ProgressBar extends Region {
        private double progress = 0;

        public ProgressBar() {
            updateProgress();
        }

        private void updateProgress() {
            getChildren().clear();

            Region background = new Region();
            background.setStyle("-fx-background-color: #f6f9fc; -fx-background-radius: 5;");
            background.setPrefHeight(10);
            background.prefWidthProperty().bind(widthProperty());

            Region bar = new Region();
            bar.setStyle("-fx-background-color: #ff5e5e; -fx-background-radius: 5;");
            bar.setPrefHeight(10);
            bar.prefWidthProperty().bind(widthProperty().multiply(progress));

            getChildren().addAll(background, bar);
        }

        public void setProgress(double progress) {
            this.progress = progress;
            updateProgress();
        }
    }
}