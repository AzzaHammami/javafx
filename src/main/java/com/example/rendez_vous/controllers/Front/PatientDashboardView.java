package com.example.rendez_vous.controllers.Front;

import javafx.application.Application;
import javafx.geometry.Insets;
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

public class PatientDashboardView extends BorderPane {

    private User selectedMedecin; // Track the currently selected doctor

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
        VBox quickActions = createQuickActions();

        AppointmentCrudWindow appointmentCrudWindow = new AppointmentCrudWindow();
        VBox tableauRdv = new VBox(18);
        Label titreTableau = new Label("Liste des Rendez-vous");
        titreTableau.setFont(Font.font("System", FontWeight.BOLD, 20));
        titreTableau.setStyle("-fx-text-fill: #1c2c46;");
        tableauRdv.getChildren().addAll(titreTableau, appointmentCrudWindow.getContentWithDateSelector());

        mainContent.getChildren().addAll(welcomeLabel, summaryLabel, new VBox(5, tiles), medecinsSection, quickActions, tableauRdv);

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
        Label nomLabel = new Label("Dr " + medecin.getName());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 17));
        nomLabel.setStyle("-fx-text-fill: #1565c0;");
        Label specialiteLabel = new Label(medecin.getSpecialite());
        specialiteLabel.setFont(Font.font("System", 14));
        specialiteLabel.setStyle("-fx-text-fill: #555;");
        infos.getChildren().addAll(nomLabel, specialiteLabel);

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

        card.getChildren().addAll(photo, infos, spacer, rdvBtn);
        return card;
    }

    private VBox createQuickActions() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(10, 0, 0, 0));

        Label sectionTitle = new Label("Accès rapides");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));

        HBox actions = new HBox(20);

        Button appointmentBtn = new Button("Prendre rendez-vous");
        appointmentBtn.setPrefSize(220, 60);
        appointmentBtn.setStyle("-fx-background-color: #5e72e4; -fx-text-fill: white; -fx-background-radius: 10;");
        appointmentBtn.setOnAction(e -> {
            if (selectedMedecin == null) {
                showAlert("Aucun médecin sélectionné", "Veuillez d'abord sélectionner un médecin");
                return;
            }

            String imageUrl = selectedMedecin.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                imageUrl = new File(imageUrl).toURI().toString();
            }

            this.setCenter(new AppointmentCrudWindow(
                    selectedMedecin.getId(),
                    "Dr " + selectedMedecin.getName(),
                    selectedMedecin.getSpecialite(),
                    imageUrl
            ).getContentWithDateSelector());
        });

        Button documentsBtn = new Button("Télécharger documents");
        documentsBtn.setPrefSize(220, 60);
        documentsBtn.setStyle("-fx-background-color: white; -fx-text-fill: #5e72e4; -fx-border-color: #e9ecef; -fx-border-radius: 10; -fx-background-radius: 10;");

        Button contactBtn = new Button("Contacter mon médecin");
        contactBtn.setPrefSize(220, 60);
        contactBtn.setStyle("-fx-background-color: white; -fx-text-fill: #5e72e4; -fx-border-color: #e9ecef; -fx-border-radius: 10; -fx-background-radius: 10;");

        Button disponibiliteBtn = new Button("Gérer disponibilités");
        disponibiliteBtn.setPrefSize(220, 60);
        disponibiliteBtn.setStyle("-fx-background-color: white; -fx-text-fill: #5e72e4; -fx-border-color: #e9ecef; -fx-border-radius: 10; -fx-background-radius: 10;");
        disponibiliteBtn.setOnAction(e -> {
            DisponibiliteCrudWindow crudWindow = new DisponibiliteCrudWindow();
            this.setCenter(crudWindow.getContent());
        });

        actions.getChildren().addAll(appointmentBtn, documentsBtn, contactBtn, disponibiliteBtn);
        section.getChildren().addAll(sectionTitle, actions);
        return section;
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