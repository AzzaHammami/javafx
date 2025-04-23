package com.example.rendez_vous.controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
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

    private ObservableList<User> medecins = FXCollections.observableArrayList();
    private ServiceUser serviceUser = new ServiceUser();

    // --- CHAMPS DU FORMULAIRE AU NIVEAU DE LA CLASSE ---
    private TextField nomField;
    private TextField emailField;
    private TextField specialiteField;
    private TextField imageField;
    private int medecinIdToEdit = -1;

    @FXML
    private void initialize() {
        // TODO: Load existing reclamations
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
        Parent content = FXMLLoader.load(getClass().getResource("/Views/AjouterRendezvous.fxml"));
        medecinContent.getChildren().setAll(content);
    }

    @FXML
    private void showDisponibiliteCrud() throws IOException {
        Parent content = FXMLLoader.load(getClass().getResource("/Views/GestionDisponibilite.fxml"));
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
}
