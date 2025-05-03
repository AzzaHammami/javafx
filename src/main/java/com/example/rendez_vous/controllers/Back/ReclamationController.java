package com.example.rendez_vous.controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.io.File;
import com.example.rendez_vous.services.ServiceUser;
import com.example.rendez_vous.models.User;
import java.sql.SQLException;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.example.rendez_vous.services.Servicerendez_vous;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.LineChart;
import java.util.Map;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import java.time.LocalDate;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;

public class ReclamationController {
    @FXML
    private TableView<?> tableView;
    
    @FXML
    private TableView<?> reponsesTableView;
    
    @FXML
    private TextField reclamationField;
    
    @FXML
    private TextArea contenuField;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private VBox medecinContent;
    
    @FXML
    private Button rdvStatBtn;
    
    private ObservableList<User> medecins = FXCollections.observableArrayList();
    private ServiceUser serviceUser = new ServiceUser();
    private Servicerendez_vous serviceRdv = new Servicerendez_vous();

    // --- CHAMPS DU FORMULAIRE AU NIVEAU DE LA CLASSE ---
    private TextField nomField;
    private TextField emailField;
    private TextField specialiteField;
    private TextField imageField;
    private int medecinIdToEdit = -1;

    @FXML
    private void initialize() {
        // TODO: Load existing reclamations
        if (rdvStatBtn != null) {
            rdvStatBtn.setOnAction(e -> showRdvStatistiquePage());
        }
    }
    
    @FXML
    private void handleRetourHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Views/landing.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1024, 768);
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void ajouterReponse() {
        // TODO: Implement adding response
    }
    
    @FXML
    private void modifierReponse() {
        // TODO: Implement editing response
    }
    
    @FXML
    private void supprimerReponse() {
        // TODO: Implement deleting response
    }
    
    @FXML
    private void showAjouterMedecinForm() {
        medecinContent.getChildren().clear();
        medecinContent.setVisible(true);
        medecinContent.setManaged(true);

        Label titre = new Label("Ajouter un Médecin");
        titre.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222;");
        nomField = new TextField();
        nomField.setPromptText("Nom du médecin");
        emailField = new TextField();
        emailField.setPromptText("Email du médecin");
        specialiteField = new TextField();
        specialiteField.setPromptText("Spécialité du médecin");
        imageField = new TextField();
        imageField.setPromptText("Chemin de l'image du médecin");
        Button parcourirBtn = new Button("Parcourir...");
        HBox imageBox = new HBox(5, imageField, parcourirBtn);

        parcourirBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(parcourirBtn.getScene().getWindow());
            if (selectedFile != null) {
                imageField.setText(selectedFile.getAbsolutePath());
            }
        });

        Button ajouterBtn = new Button("Ajouter");
        ajouterBtn.setStyle("-fx-background-color: #5e72e4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 24; -fx-background-radius: 6;");

        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #2dce89; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 24;");
        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #f5365c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 24;");
        HBox actionsBox = new HBox(10, ajouterBtn, modifierBtn, supprimerBtn);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        modifierBtn.setOnAction(e -> {
            String nom = nomField.getText().trim();
            String email = emailField.getText().trim();
            String specialite = specialiteField.getText().trim();
            String imagePath = imageField.getText().trim();
            if (!nom.isEmpty() && !email.isEmpty() && medecinIdToEdit != -1) {
                User updatedMedecin = new User(medecinIdToEdit, email, nom, java.util.Collections.singletonList("medecin"), "", imagePath, specialite, null);
                try {
                    serviceUser.modifier(updatedMedecin);
                    for (int i = 0; i < medecins.size(); i++) {
                        if (medecins.get(i).getId() == medecinIdToEdit) {
                            medecins.set(i, updatedMedecin);
                            break;
                        }
                    }
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de la modification : " + ex.getMessage());
                }
                nomField.clear();
                emailField.clear();
                specialiteField.clear();
                imageField.clear();
                medecinIdToEdit = -1;
                updateMedecinList();
            }
        });

        supprimerBtn.setOnAction(e -> {
            if (medecinIdToEdit != -1) {
                try {
                    serviceUser.supprimer(medecinIdToEdit);
                    medecins.removeIf(m -> m.getId() == medecinIdToEdit);
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de la suppression : " + ex.getMessage());
                }
                nomField.clear();
                emailField.clear();
                specialiteField.clear();
                imageField.clear();
                medecinIdToEdit = -1;
                updateMedecinList();
            }
        });

        ajouterBtn.setOnAction(e -> {
            String nom = nomField.getText().trim();
            String email = emailField.getText().trim();
            String specialite = specialiteField.getText().trim();
            String imagePath = imageField.getText().trim();
            if (!nom.isEmpty() && !email.isEmpty()) {
                User nouveauMedecin = new User(0, email, nom, java.util.Collections.singletonList("medecin"), "", imagePath, specialite, null);
                try {
                    serviceUser.ajouter(nouveauMedecin);
                    medecins.add(nouveauMedecin);
                } catch (SQLException ex) {
                    System.err.println("Erreur lors de l'ajout dans la base : " + ex.getMessage());
                }
                nomField.clear();
                emailField.clear();
                specialiteField.clear();
                imageField.clear();
                medecinIdToEdit = -1;
                updateMedecinList();
            }
        });

        TableView<User> table = new TableView<>(medecins);
        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        TableColumn<User, ImageView> imageCol = new TableColumn<>("Photo");
        imageCol.setCellValueFactory(data -> {
            String url = data.getValue().getImageUrl();
            ImageView iv;
            if (url != null && !url.isEmpty()) {
                try {
                    Image img = url.startsWith("http") ? new Image(url) : new Image(new File(url).toURI().toString());
                    iv = new ImageView(img);
                    iv.setFitHeight(36);
                    iv.setFitWidth(36);
                    iv.setPreserveRatio(true);
                } catch (Exception e) {
                    iv = new ImageView();
                }
            } else {
                iv = new ImageView();
            }
            return new javafx.beans.property.SimpleObjectProperty<>(iv);
        });
        imageCol.setPrefWidth(50);
        TableColumn<User, String> specialiteCol = new TableColumn<>("Spécialité");
        specialiteCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialite()));

        table.getColumns().addAll(imageCol, nomCol, emailCol, specialiteCol);
        table.setPrefHeight(180);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    User medecin = row.getItem();
                    nomField.setText(medecin.getName());
                    emailField.setText(medecin.getEmail());
                    specialiteField.setText(medecin.getSpecialite());
                    imageField.setText(medecin.getImageUrl());
                    medecinIdToEdit = medecin.getId();
                }
            });
            return row;
        });

        VBox form = new VBox(10, titre, nomField, emailField, specialiteField, imageBox, actionsBox);
        form.setAlignment(javafx.geometry.Pos.CENTER);
        form.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 10; -fx-padding: 20;");

        medecinContent.getChildren().add(form);
        medecinContent.getChildren().add(table);
        updateMedecinList();
    }

    @FXML
    private void showRendezVousCrud() throws IOException {
        Parent content = FXMLLoader.load(getClass().getResource("/views/Back/AjouterRendezvous.fxml"));
        medecinContent.getChildren().setAll(content);
    }

    @FXML
    private void showDisponibiliteCrud() throws IOException {
        Parent content = FXMLLoader.load(getClass().getResource("/views/Back/GestionDisponibilite.fxml"));
        medecinContent.getChildren().setAll(content);
    }

    private void updateMedecinList() {
        medecinContent.getChildren().removeIf(node -> node instanceof TableView);
        medecins.setAll(serviceUser.getAllMedecins());
        TableView<User> table = new TableView<>(medecins);
        TableColumn<User, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        TableColumn<User, ImageView> imageCol = new TableColumn<>("Photo");
        imageCol.setCellValueFactory(data -> {
            String url = data.getValue().getImageUrl();
            ImageView iv;
            if (url != null && !url.isEmpty()) {
                try {
                    Image img = url.startsWith("http") ? new Image(url) : new Image(new File(url).toURI().toString());
                    iv = new ImageView(img);
                    iv.setFitHeight(36);
                    iv.setFitWidth(36);
                    iv.setPreserveRatio(true);
                } catch (Exception e) {
                    iv = new ImageView();
                }
            } else {
                iv = new ImageView();
            }
            return new javafx.beans.property.SimpleObjectProperty<>(iv);
        });
        imageCol.setPrefWidth(50);
        TableColumn<User, String> specialiteCol = new TableColumn<>("Spécialité");
        specialiteCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSpecialite()));

        table.getColumns().addAll(imageCol, nomCol, emailCol, specialiteCol);
        table.setPrefHeight(180);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    User medecin = row.getItem();
                    nomField.setText(medecin.getName());
                    emailField.setText(medecin.getEmail());
                    specialiteField.setText(medecin.getSpecialite());
                    imageField.setText(medecin.getImageUrl());
                    medecinIdToEdit = medecin.getId();
                }
            });
            return row;
        });

        medecinContent.getChildren().add(table);
    }

    private void showRdvStatistiquePage() {
        VBox statsCard = new VBox(32);
        statsCard.setAlignment(Pos.TOP_CENTER);
        statsCard.setStyle(
            "-fx-background-color: #fff;" +
            "-fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, #b0bec5, 12, 0.18, 0, 4);" +
            "-fx-padding: 32 48 32 48;"
        );

        Label mainTitle = new Label("Statistiques des Rendez-vous");
        mainTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #673ab7;");

        HBox filtersBox = new HBox(18);
        filtersBox.setAlignment(Pos.CENTER_LEFT);
        filtersBox.setStyle("-fx-background-color: #f9f9fb; -fx-background-radius: 12; -fx-padding: 12 24 12 24; -fx-spacing: 18; -fx-effect: dropshadow(gaussian, #e0e0e0, 4, 0.12, 0, 1);");
        ComboBox<Integer> yearCombo = new ComboBox<>();
        int currentYear = java.time.LocalDate.now().getYear();
        for (int y = currentYear - 5; y <= currentYear; y++) yearCombo.getItems().add(y);
        yearCombo.setValue(currentYear);
        ComboBox<String> monthCombo = new ComboBox<>();
        monthCombo.getItems().add("Tous");
        String[] months = {"01","02","03","04","05","06","07","08","09","10","11","12"};
        for (String m : months) monthCombo.getItems().add(m);
        monthCombo.setValue("Tous");
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        Button resetBtn = new Button("Réinitialiser");
        resetBtn.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #673ab7;");
        filtersBox.getChildren().addAll(
            new Label("Année :"), yearCombo,
            new Label("Mois :"), monthCombo,
            new Label("Du :"), startDatePicker,
            new Label("Au :"), endDatePicker,
            resetBtn
        );

        statsCard.getChildren().addAll(mainTitle, filtersBox);

        Runnable updateStats = () -> {
            statsCard.getChildren().removeIf(node -> (node instanceof Separator) || (node instanceof PieChart) || (node instanceof BarChart) || (node instanceof LineChart) || (node instanceof Label && node != mainTitle));
            Integer year = yearCombo.getValue();
            String month = monthCombo.getValue();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            try {
                int total = serviceRdv.countAllRendezVousFiltered(year, month, start, end);
                int confirmes = serviceRdv.countRendezVousByStatutFiltered("Confirmé", year, month, start, end);
                int annules = serviceRdv.countRendezVousByStatutFiltered("Annulé", year, month, start, end);
                int attente = serviceRdv.countRendezVousByStatutFiltered("En attente", year, month, start, end);
                int matin = serviceRdv.countRendezVousByPlageHoraireFiltered("Matin", year, month, start, end);
                int apresmidi = serviceRdv.countRendezVousByPlageHoraireFiltered("Après-midi", year, month, start, end);
                int soir = serviceRdv.countRendezVousByPlageHoraireFiltered("Soir", year, month, start, end);
                Label totalLabel = new Label("Nombre total de rendez-vous : " + total);
                totalLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 12 0;");
                statsCard.getChildren().add(totalLabel);
                Separator sep1 = new Separator();
                sep1.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 12 0 12 0;");
                statsCard.getChildren().add(sep1);
                Label pieTitle = new Label("Répartition des rendez-vous par statut");
                pieTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 8 0;");
                statsCard.getChildren().add(pieTitle);
                PieChart pieStatut = new PieChart();
                pieStatut.getData().add(new PieChart.Data("Confirmés", confirmes));
                pieStatut.getData().add(new PieChart.Data("Annulés", annules));
                pieStatut.getData().add(new PieChart.Data("En attente", attente));
                pieStatut.setTitle(null);
                pieStatut.setLabelsVisible(true);
                pieStatut.setLegendVisible(true);
                pieStatut.setStyle("-fx-font-size: 15px;");
                pieStatut.setPrefHeight(220);
                pieStatut.setPrefWidth(320);
                pieStatut.setMaxWidth(320);
                pieStatut.setMaxHeight(220);
                pieStatut.setAnimated(true);
                pieStatut.getStylesheets().add(getClass().getResource("/chart-style.css").toExternalForm());
                statsCard.getChildren().add(pieStatut);
                Separator sep2 = new Separator();
                sep2.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 12 0 12 0;");
                statsCard.getChildren().add(sep2);
                Label piePlageTitle = new Label("Répartition par plage horaire");
                piePlageTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 8 0;");
                statsCard.getChildren().add(piePlageTitle);
                PieChart piePlage = new PieChart();
                piePlage.getData().add(new PieChart.Data("Matin (6h-12h)", matin));
                piePlage.getData().add(new PieChart.Data("Après-midi (12h-18h)", apresmidi));
                piePlage.getData().add(new PieChart.Data("Soir (18h-6h)", soir));
                piePlage.setTitle(null);
                piePlage.setLabelsVisible(true);
                piePlage.setLegendVisible(true);
                piePlage.setStyle("-fx-font-size: 15px;");
                piePlage.setPrefHeight(220);
                piePlage.setPrefWidth(320);
                piePlage.setMaxWidth(320);
                piePlage.setMaxHeight(220);
                piePlage.setAnimated(true);
                piePlage.getStylesheets().add(getClass().getResource("/chart-style.css").toExternalForm());
                statsCard.getChildren().add(piePlage);
                Separator sep3 = new Separator();
                sep3.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 12 0 12 0;");
                statsCard.getChildren().add(sep3);
                Label barTitle = new Label("Histogramme des rendez-vous par statut");
                barTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 0 0 8 0;");
                statsCard.getChildren().add(barTitle);
                CategoryAxis xAxis = new CategoryAxis();
                xAxis.setLabel("Statut");
                NumberAxis yAxis = new NumberAxis();
                yAxis.setLabel("Nombre de rendez-vous");
                BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Statuts");
                series.getData().add(new XYChart.Data<>("Confirmés", confirmes));
                series.getData().add(new XYChart.Data<>("Annulés", annules));
                series.getData().add(new XYChart.Data<>("En attente", attente));
                barChart.getData().add(series);
                barChart.setTitle(null);
                barChart.setCategoryGap(20);
                barChart.setBarGap(8);
                barChart.setLegendVisible(false);
                barChart.setStyle("-fx-font-size: 15px;");
                barChart.setPrefHeight(220);
                barChart.setPrefWidth(400);
                barChart.setAnimated(true);
                barChart.getStylesheets().add(getClass().getResource("/chart-style.css").toExternalForm());
                statsCard.getChildren().add(barChart);
                // Ajoute les autres graphiques et séparateurs ici si besoin
            } catch (Exception ex) {
                statsCard.getChildren().add(new Label("Erreur lors du calcul des statistiques : " + ex.getMessage()));
            }
        };
        // Listeners dynamiques
        yearCombo.setOnAction(e -> updateStats.run());
        monthCombo.setOnAction(e -> updateStats.run());
        startDatePicker.setOnAction(e -> updateStats.run());
        endDatePicker.setOnAction(e -> updateStats.run());
        resetBtn.setOnAction(e -> {
            yearCombo.setValue(currentYear);
            monthCombo.setValue("Tous");
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
        });
        // Premier affichage
        updateStats.run();
        ScrollPane scrollPane = new ScrollPane(statsCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        medecinContent.getChildren().setAll(scrollPane);
    }
}
