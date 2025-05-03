package com.example.rendez_vous.controllers.Front;

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
import com.example.rendez_vous.models.Rating;
import com.example.rendez_vous.services.ServiceUser;
import com.example.rendez_vous.services.ServiceRating;
import com.example.rendez_vous.controllers.Front.AppointmentCrudWindow;
import java.util.List;
import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.models.RendezVous;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.ArrayList;

public class PatientDashboardView extends BorderPane {

    private User selectedMedecin; // Track the currently selected doctor

    // --- Notifications dynamiques en mémoire ---
    private static class Notification {
        String titre, message, time, priority, action;
        boolean lu;
        Notification(String titre, String message, String time, String priority, String action, boolean lu) {
            this.titre = titre; this.message = message; this.time = time; this.priority = priority; this.action = action; this.lu = lu;
        }
    }
    private static final List<Notification> notifications = new ArrayList<>();
    private Label notifBadge;
    private StackPane bellWithBadge;

    // Pour accès depuis AppointmentCrudWindow
    public void addAppointmentNotification(String nomMedecin, String date, String heure) {
        notifications.add(0, new Notification(
                "Nouveau rendez-vous",
                "Vous avez un rendez-vous avec Dr. " + nomMedecin + " le " + date + " à " + heure,
                "Maintenant", "NORMAL", "Voir", false
        ));
        updateNotifBadge();
    }
    private void updateNotifBadge() {
        long nonLues = notifications.stream().filter(n -> !n.lu).count();
        notifBadge.setText(String.valueOf(nonLues));
        notifBadge.setVisible(nonLues > 0);
    }

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

        Label welcomeLabel = new Label("Bonjour, Chèr(e) patient(e)");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        Label summaryLabel = new Label("Voici le résumé de votre santé");
        summaryLabel.setFont(Font.font("System", 14));
        summaryLabel.setTextFill(Color.web("#8898aa"));

        // --- Prochain rendez-vous ---
        int patientId = getCurrentUserId();
        Servicerendez_vous serviceRdv = new Servicerendez_vous();
        RendezVous prochainRdv = serviceRdv.getNextAppointmentForPatient(patientId);
        VBox prochainRdvBox = null;
        if (prochainRdv != null) {
            String dateStr = prochainRdv.getDate().toLocalDate().toString();
            String heureStr = prochainRdv.getDate().toLocalTime().toString();
            String medecinStr = "Dr. " + getMedecinNameById(prochainRdv.getMedecinId());
            String motifStr = prochainRdv.getMotif();
            prochainRdvBox = createTile("Prochain RDV", "#5e72e4", dateStr, medecinStr, motifStr);
        } else {
            prochainRdvBox = createTile("Prochain RDV", "#5e72e4", "Aucun rendez-vous à venir", "", "");
        }
        prochainRdvBox.setOnMouseClicked(e -> {
            AppointmentCrudWindow crudWindow = new AppointmentCrudWindow();
            this.setCenter(crudWindow.getContentWithDateSelector());
        });

        // Correction : tiles doit être un HBox, pas un VBox
        HBox tiles = new HBox(20);
        tiles.getChildren().add(prochainRdvBox);
        // Ajout des autres tuiles (médicaments, notifications)
        VBox medicationTile = createTile("Médicaments", "#ff5e5e", "Amoxicilline - 2x/jour", "Doliprane - si besoin", "");
        ProgressBar progress = new ProgressBar();
        progress.setProgress(0.6);
        progress.setPrefWidth(140);
        progress.setStyle("-fx-accent: #ff5e5e;");
        medicationTile.getChildren().add(progress);
        VBox notificationTile = createTile("Notifications", "#11cdef", "Résultats disponibles", "Rappel vaccination", "");
        tiles.getChildren().addAll(medicationTile, notificationTile);

        tiles.setSpacing(20);

        VBox medecinsSection = createMedecinsSection();

        VBox tableauRdv = new VBox();

        mainContent.getChildren().addAll(welcomeLabel, summaryLabel, new VBox(5, tiles), medecinsSection, tableauRdv);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return new VBox(scrollPane);
    }

    // Helper pour obtenir le nom du médecin à partir de l'id (simple, à améliorer selon cache/service)
    private String getMedecinNameById(int medecinId) {
        ServiceUser serviceUser = new ServiceUser();
        List<User> medecins = serviceUser.getAllMedecins();
        for (User m : medecins) {
            if (m.getId() == medecinId) {
                return m.getName();
            }
        }
        return "[inconnu]";
    }

    // Correction du type de retour :
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
        // Chargement asynchrone des ratings AVANT de remplir la liste des médecins
        new Thread(() -> {
            List<User> medecins = new ServiceUser().getAllMedecins();
            Platform.runLater(() -> {
                medecinsList.getChildren().clear();
                for (User medecin : medecins) {
                    String imageUrl = medecin.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        imageUrl = new java.io.File(imageUrl).toURI().toString();
                    }
                    medecinsList.getChildren().add(createMedecinCard(medecin, imageUrl));
                }
            });
        }).start();
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

        // Calcul de la moyenne des notes depuis la base
        ServiceRating serviceRating = new ServiceRating();
        double moyenne = serviceRating.getMoyenneRatingMedecin(medecin.getId());

        Label nameLabel = new Label("Dr " + medecin.getName());
        nameLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1976d2;");

        Label specialiteLabel = new Label(medecin.getSpecialite() + (moyenne > 0 ? String.format(" | %.1f/5", moyenne) : " | Pas de note"));
        specialiteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #888;");

        // Ajout du rating visuel (étoiles)
        HBox starsBox = new HBox(3);
        int fullStars = (int) moyenne;
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < fullStars ? "★" : "☆");
            star.setStyle("-fx-font-size: 20px; -fx-text-fill: #ff9800;");
            starsBox.getChildren().add(star);
        }

        infos.getChildren().clear();
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
        ratingBtn.setOnAction(e -> {
            scrollToRatingSection(medecin);
            // Scroll automatique vers la partie rating (si ce n'est pas déjà fait dans scrollToRatingSection)
            Node centerNode = this.getCenter();
            if (centerNode instanceof VBox) {
                for (Node node : ((VBox) centerNode).getChildren()) {
                    if (node instanceof ScrollPane) {
                        ((ScrollPane) node).setVvalue(1.0);
                    }
                }
            }
        });

        card.getChildren().addAll(photo, infos, spacer, rdvBtn, ratingBtn);
        return card;
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

    // --- Panneau de notifications ---
    private void showNotificationPanel() {
        // Ancienne méthode, conservée pour compatibilité
        showNotificationPanelAt(0, 0);
    }

    private void showNotificationPanelAt(double screenX, double screenY) {
        VBox panel = new VBox(20);
        panel.setStyle("-fx-background-color: white; -fx-border-radius: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.12),16,0,0,4);");
        panel.setPrefWidth(420);
        panel.setPrefHeight(520);
        panel.setPadding(new Insets(24, 30, 24, 30));
        HBox topBar = new HBox();
        Label titre = new Label("Mes Notifications");
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // Nouveau bouton croix rouge (fermeture)
        Button btnFermer = new Button();
        btnFermer.setStyle("-fx-background-color: transparent; -fx-padding: 2; -fx-cursor: hand;");
        Label closeIcon = new Label("\u2716"); // Unicode X
        closeIcon.setStyle("-fx-text-fill: #ff4d4f; -fx-font-size: 20px; -fx-font-weight: bold;");
        btnFermer.setGraphic(closeIcon);
        btnFermer.setOnAction(ev -> ((Stage)panel.getScene().getWindow()).close());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(titre, spacer, btnFermer);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox filtres = new HBox(12);
        Button btnToutes = new Button("Toutes");
        Button btnNonLues = new Button("Non lues");
        Button btnMedicales = new Button("Médicales");
        Button btnAdmin = new Button("Administratives");
        filtres.getChildren().addAll(btnToutes, btnNonLues, btnMedicales, btnAdmin);
        VBox notifList = new VBox(16);
        notifList.setPrefHeight(400);
        notifList.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 8; -fx-padding: 10;");
        // Génère dynamiquement la liste des notifications
        for (Notification notif : notifications) {
            notifList.getChildren().add(createNotifItem(notif));
        }
        panel.getChildren().addAll(topBar, filtres, notifList);
        Stage popup = new Stage();
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);
        popup.setScene(new Scene(panel));
        // Positionnement sous la cloche
        if (screenX != 0 || screenY != 0) {
            popup.setX(screenX - 420 + 50); // Décale à droite de la cloche (ajuster selon votre design)
            popup.setY(screenY + 8); // Juste sous la cloche
        }
        popup.show();
    }

    // Corrigé : crée un seul item de notification
    private Node createNotifItem(Notification notif) {
        VBox itemBox = new VBox();
        itemBox.setStyle("-fx-background-color: #fffde7; -fx-border-color: #ffe082; -fx-padding: 8 12; -fx-background-radius: 8; -fx-font-size: 13px; -fx-margin: 4 0 4 0;");
        Label titre = new Label(notif.titre);
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label message = new Label(notif.message);
        message.setWrapText(true);
        itemBox.getChildren().addAll(titre, message);
        return itemBox;
    }

    private void scrollToRatingSection(User medecin) {
        // 1. Retrouver le ScrollPane et le VBox mainContent
        final VBox[] mainContentArr = new VBox[1];
        final ScrollPane[] scrollPaneArr = new ScrollPane[1];
        Node centerNode = this.getCenter();
        if (centerNode instanceof VBox) {
            for (Node node : ((VBox) centerNode).getChildren()) {
                if (node instanceof ScrollPane) {
                    scrollPaneArr[0] = (ScrollPane) node;
                    if (scrollPaneArr[0].getContent() instanceof VBox) {
                        mainContentArr[0] = (VBox) scrollPaneArr[0].getContent();
                    }
                }
            }
        }
        VBox foundMainContent = mainContentArr[0];
        ScrollPane foundScrollPane = scrollPaneArr[0];
        if (foundMainContent == null || foundScrollPane == null) return;

        // Supprimer toute ancienne zone de rating
        foundMainContent.getChildren().removeIf(child -> child.getId() != null && child.getId().equals("dynamicRatingBox"));

        // Créer dynamiquement la zone de rating
        VBox ratingBox = new VBox(18);
        ratingBox.setId("dynamicRatingBox");
        ratingBox.setPadding(new Insets(24));
        ratingBox.setStyle("-fx-background-color: #fff8e1; -fx-border-color: #ffe082; -fx-border-radius: 16; -fx-background-radius: 16;");
        ratingBox.setAlignment(Pos.CENTER);

        String imageUrl = medecin.getImageUrl();
        ImageView photo = null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            photo = new ImageView(new Image(imageUrl));
            photo.setFitWidth(80);
            photo.setFitHeight(80);
            photo.setStyle("-fx-background-radius: 50; -fx-border-radius: 50; -fx-border-color: #ff9800;");
        }

        Label titre = new Label("Noter le médecin : Dr " + medecin.getName());
        titre.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ff9800;");

        // Zone étoiles dynamique
        HBox stars = new HBox(8);
        stars.setAlignment(Pos.CENTER);
        final int[] ratingValue = {0};
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 32px; -fx-text-fill: #ff9800; -fx-cursor: hand;");
            final int val = i;
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    ((Label) stars.getChildren().get(j)).setText(j < val ? "★" : "☆");
                }
            });
            star.setOnMouseExited(e -> {
                for (int j = 0; j < 5; j++) {
                    ((Label) stars.getChildren().get(j)).setText(j < ratingValue[0] ? "★" : "☆");
                }
            });
            star.setOnMouseClicked(e -> {
                ratingValue[0] = val;
                for (int j = 0; j < 5; j++) {
                    ((Label) stars.getChildren().get(j)).setText(j < val ? "★" : "☆");
                }
            });
            stars.getChildren().add(star);
        }

        TextArea commentaireArea = new TextArea();
        commentaireArea.setPromptText("Votre commentaire (optionnel)");
        commentaireArea.setWrapText(true);
        commentaireArea.setPrefRowCount(3);
        commentaireArea.setMaxWidth(400);
        commentaireArea.setStyle("-fx-font-size: 15px;");

        Button submitBtn = new Button("Valider la note");
        submitBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 17px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 32;");
        submitBtn.setOnAction(ev -> {
            if (ratingValue[0] == 0) {
                showAlert("Erreur", "Veuillez sélectionner une note.");
                return;
            }
            int patientId = getCurrentUserId();
            ServiceRating serviceRating = new ServiceRating();
            String commentaire = commentaireArea.getText();
            Rating rating = new Rating(medecin.getId(), patientId, ratingValue[0], commentaire);
            serviceRating.addOrUpdateRating(rating);
            showAlert("Merci !", "Votre note a bien été prise en compte.");
            foundMainContent.getChildren().remove(ratingBox);
        });

        if (photo != null) {
            ratingBox.getChildren().add(photo);
        }
        ratingBox.getChildren().addAll(titre, stars, commentaireArea, submitBtn);
        foundMainContent.getChildren().add(ratingBox);

        // Scroll automatique vers le bas
        Platform.runLater(() -> foundScrollPane.setVvalue(1.0));
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

        // --- Ajout de la cloche de notifications (intégrée, fond transparent) ---
        ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/bell.png")));
        bellIcon.setFitHeight(28);
        bellIcon.setFitWidth(28);
        notifBadge = new Label();
        notifBadge.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-width: 0; -fx-font-size: 13;");
        notifBadge.setTranslateX(-15);
        notifBadge.setTranslateY(-10);
        notifBadge.setVisible(false);
        bellWithBadge = new StackPane(bellIcon, notifBadge);
        bellWithBadge.setCursor(javafx.scene.Cursor.HAND);
        bellWithBadge.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        // --- Positionnement du panneau de notifications sous la cloche ---
        bellWithBadge.setOnMouseClicked(e -> {
            // Calcul de la position de la cloche à l'écran
            javafx.scene.Node source = (javafx.scene.Node) e.getSource();
            javafx.geometry.Bounds bounds = source.localToScreen(source.getBoundsInLocal());
            showNotificationPanelAt(bounds.getMinX(), bounds.getMaxY());
        });
        // --- FIN cloche ---

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

        header.getChildren().addAll(logoCircle, appName, menu, spacer, bellWithBadge, returnButton, profileCircle);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        return header;
    }
}