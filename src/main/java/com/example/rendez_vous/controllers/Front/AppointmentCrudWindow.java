package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.services.SmsService;
import com.example.rendez_vous.models.RendezVous;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

public class AppointmentCrudWindow {
    private static final Logger logger = Logger.getLogger(AppointmentCrudWindow.class.getName());
    private final Servicerendez_vous service;
    private TableView<RendezVous> tableView;
    private TextField motifField;
    private TextField patientIdField;
    private TextField phoneNumberField;
    private TextField medecinIdField;
    private DatePicker datePicker;
    private Button selectedHourBtn = new Button();
    private String medecinNom;
    private String specialiteMedecin;
    private String medecinImageUrl;
    private String selectedTime = "";
    private int medecinId;
    // Ajout du champ SmsService
    private SmsService smsService = new SmsService();

    public AppointmentCrudWindow(int medecinId, String medecinNom, String specialiteMedecin, String medecinImageUrl) {
        logger.log(Level.INFO, "Creating AppointmentCrudWindow with doctor: ID={0}, Name={1}, Specialty={2}",
                new Object[]{medecinId, medecinNom, specialiteMedecin});

        service = new Servicerendez_vous();
        this.medecinId = medecinId;
        this.medecinNom = medecinNom;
        this.specialiteMedecin = specialiteMedecin;
        this.medecinImageUrl = medecinImageUrl;
        this.selectedHourBtn = new Button();
        this.motifField = new TextField();
        this.patientIdField = new TextField();
        this.phoneNumberField = new TextField();
        this.medecinIdField = new TextField(String.valueOf(medecinId));

        logger.log(Level.FINE, "Initialized fields - MedecinIDField: {0}", medecinIdField.getText());
    }

    public AppointmentCrudWindow() {
        logger.info("Creating empty AppointmentCrudWindow");
        service = new Servicerendez_vous();
        this.selectedHourBtn = new Button();
        this.motifField = new TextField();
        this.patientIdField = new TextField();
        this.phoneNumberField = new TextField();
        this.medecinIdField = new TextField();
    }

    public void setMedecin(String nom, String specialite, String imageUrl) {
        logger.log(Level.INFO, "Setting doctor: Name={0}, Specialty={1}", new Object[]{nom, specialite});
        this.medecinNom = nom;
        this.specialiteMedecin = specialite;
        this.medecinImageUrl = imageUrl;
    }

    public VBox getContentWithDateSelector() {
        logger.info("Building date selector view");
        VBox root = new VBox();
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(0, 0, 0, 0));

        VBox scrollContent = new VBox(18);
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setPadding(new Insets(0, 0, 0, 0));
        scrollContent.setStyle("-fx-background-color: transparent;");

        HBox progressBar = createProgressBar(1);
        VBox medCard = createDoctorCard();
        VBox calendarSection = createCalendarSection();

        scrollContent.getChildren().addAll(progressBar, medCard, calendarSection);
        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        root.getChildren().add(scrollPane);

        HBox actions = createActionButtons(root);
        root.getChildren().add(actions);

        return root;
    }

    private HBox createProgressBar(int currentStep) {
        logger.log(Level.FINE, "Creating progress bar at step {0}", currentStep);
        String[] steps = {"Médecin", "Date/Heure", "Motif", "Confirmation"};
        HBox progressBar = new HBox();
        progressBar.setAlignment(Pos.CENTER);
        progressBar.setSpacing(0);
        progressBar.setPadding(new Insets(22, 0, 0, 0));

        for (int i = 0; i < steps.length; i++) {
            VBox stepBox = new VBox(6);
            stepBox.setAlignment(Pos.CENTER);
            StackPane circlePane = new StackPane();
            Circle circle = new Circle(18);

            Label iconLabel;
            if (i < currentStep) {
                circle.setFill(Color.web("#FFD600"));
                iconLabel = new Label("✓");
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            } else if (i == currentStep) {
                circle.setFill(Color.web("#FFD600"));
                iconLabel = new Label("●");
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            } else {
                circle.setFill(Color.web("#e0e0e0"));
                iconLabel = new Label(String.valueOf(i + 1));
                iconLabel.setStyle("-fx-text-fill: #bdbdbd; -fx-font-size: 18px; -fx-font-weight: bold;");
            }
            circlePane.getChildren().addAll(circle, iconLabel);

            Label label = new Label(steps[i]);
            label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            if (i <= currentStep) {
                label.setTextFill(Color.web("#FFD600"));
            } else {
                label.setTextFill(Color.web("#bdbdbd"));
            }

            stepBox.getChildren().addAll(circlePane, label);
            progressBar.getChildren().add(stepBox);

            // Ligne de progression sauf après la dernière étape
            if (i < steps.length - 1) {
                VBox lineBox = new VBox();
                lineBox.setAlignment(Pos.CENTER);
                Rectangle line = new Rectangle(60, 4);
                line.setArcWidth(4);
                line.setArcHeight(4);
                if (i < currentStep) {
                    line.setFill(Color.web("#FFD600"));
                } else {
                    line.setFill(Color.web("#e0e0e0"));
                }
                lineBox.getChildren().add(line);
                progressBar.getChildren().add(lineBox);
            }
        }
        return progressBar;
    }

    private VBox createDoctorCard() {
        logger.info("Creating doctor card component");
        VBox medCard = new VBox();
        medCard.setAlignment(Pos.TOP_LEFT);
        medCard.setStyle("-fx-background-color: #0288d1; -fx-background-radius: 10; -fx-padding: 18 24 10 24; -fx-min-width: 600; -fx-max-width: 700;");

        if (medecinNom == null || medecinNom.trim().isEmpty()) {
            logger.fine("No doctor selected - showing empty card");
            VBox noMedecinBox = new VBox(12);
            noMedecinBox.setAlignment(Pos.CENTER);
            Label aucunLabel = new Label("Aucun médecin choisi");
            aucunLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
            Button choisirBtn = new Button("Choisir un médecin");
            choisirBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #0288d1; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-padding: 10 25;");
            choisirBtn.setOnAction(e -> {
                try {
                    logger.info("User clicked to choose a doctor");
                    PatientDashboardView dashboard = new PatientDashboardView();
                    BorderPane borderPane = (BorderPane) choisirBtn.getScene().getRoot();
                    borderPane.setCenter(dashboard.createMainContent());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error navigating to doctor selection", ex);
                    ex.printStackTrace();
                }
            });
            noMedecinBox.getChildren().addAll(aucunLabel, choisirBtn);
            medCard.getChildren().add(noMedecinBox);
        } else {
            logger.log(Level.FINE, "Showing card for doctor: {0}", medecinNom);
            HBox medInfo = new HBox(16);
            medInfo.setAlignment(Pos.CENTER_LEFT);
            ImageView avatar = createDoctorAvatar();
            VBox medLabels = createDoctorLabels();
            medInfo.getChildren().addAll(avatar, medLabels);
            medCard.getChildren().add(medInfo);
        }
        return medCard;
    }

    private ImageView createDoctorAvatar() {
        logger.log(Level.FINE, "Creating avatar with image: {0}", medecinImageUrl);
        ImageView avatar;
        if (medecinImageUrl != null && !medecinImageUrl.isEmpty()) {
            avatar = new ImageView(new Image(medecinImageUrl, 80, 80, true, true));
        } else {
            logger.fine("Using default avatar");
            avatar = new ImageView();
            avatar.setFitWidth(80);
            avatar.setFitHeight(80);
            avatar.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12;");
        }
        Rectangle avatarClip = new Rectangle(80, 80);
        avatarClip.setArcWidth(12);
        avatarClip.setArcHeight(12);
        avatar.setClip(avatarClip);
        return avatar;
    }

    private VBox createDoctorLabels() {
        logger.fine("Creating doctor labels");
        VBox medLabels = new VBox(2);
        Label nomMed = new Label(medecinNom != null ? medecinNom : "Nom Médecin");
        nomMed.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label specMed = new Label(specialiteMedecin != null ? specialiteMedecin : "Spécialité");
        specMed.setStyle("-fx-font-size: 16px; -fx-text-fill: #e3f2fd;");
        medLabels.getChildren().addAll(nomMed, specMed);
        return medLabels;
    }

    private VBox createCalendarSection() {
        logger.info("Creating calendar section");
        VBox calendarSection = new VBox(18);
        calendarSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 32 32 32 32; -fx-min-width: 600; -fx-max-width: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label sectionTitle = new Label("Sélectionnez une date");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #222; -fx-padding: 0 0 18 0; -fx-alignment: center;");
        calendarSection.getChildren().add(sectionTitle);

        HBox dateRow = new HBox();
        dateRow.setAlignment(Pos.CENTER);
        dateRow.setPadding(new Insets(0,0,18,0));
        datePicker = new DatePicker(LocalDate.now());

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable past dates
                setDisable(date.isBefore(LocalDate.now()));

                // Check if date is fully booked (all 12 time slots taken)
                List<LocalTime> takenTimes = service.getTakenTimesForDate(date, medecinId);
                if (takenTimes.size() >= 12) { // 12 is the total number of time slots
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;"); // Light red for fully booked dates
                    setTooltip(new Tooltip("Cette date est complètement réservée"));
                }
            }
        });

        datePicker.setStyle("-fx-font-size: 18px; -fx-pref-width: 220; -fx-background-radius: 8; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 8; -fx-padding: 8 18;");
        dateRow.getChildren().add(datePicker);
        calendarSection.getChildren().add(dateRow);

        Label horairesTitle = new Label("Choisissez un horaire");
        horairesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #222; -fx-padding: 0 0 15 0; -fx-alignment: center;");
        calendarSection.getChildren().add(horairesTitle);

        // Create a container for the slots grid
        VBox slotsContainer = new VBox();
        slotsContainer.getChildren().add(createTimeSlotsGrid());
        calendarSection.getChildren().add(slotsContainer);

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            logger.log(Level.INFO, "Date changed from {0} to {1}", new Object[]{oldDate, newDate});
            selectedTime = "";
            // Instead of trying to replace the grid in the children list,
            // just clear and rebuild the container
            slotsContainer.getChildren().clear();
            slotsContainer.getChildren().add(createTimeSlotsGrid());
        });

        return calendarSection;
    }

    private GridPane createTimeSlotsGrid() {
        logger.info("Creating time slots grid");
        GridPane slotsGrid = new GridPane();
        slotsGrid.setHgap(14);
        slotsGrid.setVgap(14);

        int medecinId = 0;
        try {
            if (medecinIdField != null && medecinIdField.getText() != null && !medecinIdField.getText().isEmpty()) {
                medecinId = Integer.parseInt(medecinIdField.getText());
            }
        } catch (Exception ex) {
            medecinId = 0;
            logger.log(Level.WARNING, "Error parsing doctor ID", ex);
        }

        LocalDate selectedDate = datePicker.getValue();
        logger.log(Level.INFO, "Getting taken times for date: {0}, doctor ID: {1}",
                new Object[]{selectedDate, medecinId});

        List<LocalTime> takenTimes = service.getTakenTimesForDate(selectedDate, medecinId);
        logger.log(Level.FINE, "Found {0} taken times", takenTimes.size());

        String[] horaires = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "14:00", "14:30", "15:00", "15:30"};

        for (int i = 0; i < horaires.length; i++) {
            Button slot = new Button(horaires[i]);
            slot.setPrefWidth(120);
            slot.setPrefHeight(44);

            // Default style for available slots
            slot.setStyle("-fx-font-size: 17px; -fx-font-weight: 500; -fx-background-radius: 8; " +
                    "-fx-background-color: #f8f9fa; -fx-text-fill: #0288d1; " +
                    "-fx-border-color: #e9ecef; -fx-border-width: 1;");

            LocalTime slotTime = LocalTime.parse(horaires[i]);
            if (takenTimes.contains(slotTime)) {
                logger.log(Level.FINE, "Time slot {0} is already taken", slotTime);
                // Style for taken slots - red background with white text
                slot.setStyle("-fx-font-size: 17px; -fx-font-weight: 500; -fx-background-radius: 8; " +
                        "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-border-color: #c0392b; -fx-border-width: 1; -fx-opacity: 0.9;");
                slot.setDisable(true);
                slot.setTooltip(new Tooltip("Ce créneau est déjà réservé"));
            }

            slot.setOnAction(e -> {
                logger.log(Level.INFO, "Time slot selected: {0}", slot.getText());
                // Reset all buttons to default style (except disabled ones)
                slotsGrid.getChildren().forEach(node -> {
                    if (node instanceof Button) {
                        Button b = (Button) node;
                        if (!b.isDisabled()) {
                            b.setStyle("-fx-font-size: 17px; -fx-font-weight: 500; -fx-background-radius: 8; " +
                                    "-fx-background-color: #f8f9fa; -fx-text-fill: #0288d1; " +
                                    "-fx-border-color: #e9ecef; -fx-border-width: 1;");
                        }
                    }
                });

                // Style for selected slot (green)
                slot.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                        "-fx-border-color: #27ae60; -fx-border-width: 2;");
                selectedTime = slot.getText();
                selectedHourBtn.setText(selectedTime);
            });

            slotsGrid.add(slot, i % 4, i / 4);
        }

        return slotsGrid;
    }

    private HBox createActionButtons(VBox root) {
        logger.info("Creating action buttons");
        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(20, 30, 30, 0));

        Button ajouterBtn = new Button("Ajouter");
        ajouterBtn.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-padding: 12 25;");

        Button suivantBtn = new Button("Suivant");
        suivantBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-padding: 12 25;");

        suivantBtn.setOnAction(e -> {
            if (medecinNom == null || medecinNom.trim().isEmpty()) {
                logger.warning("No doctor selected when trying to proceed");
                showError("Erreur", "Veuillez d'abord sélectionner un médecin");
                return;
            }

            if (selectedTime.isEmpty()) {
                logger.warning("No time selected when trying to proceed");
                showError("Erreur", "Veuillez sélectionner une heure");
                return;
            }
            logger.info("Proceeding to motif selection");
            root.getChildren().clear();
            root.getChildren().add(getContentWithMotifSelector());
        });

        ajouterBtn.setOnAction(e -> {
            if (medecinNom == null || medecinNom.trim().isEmpty()) {
                logger.warning("No doctor selected when trying to add");
                showError("Erreur", "Veuillez d'abord sélectionner un médecin");
                return;
            }

            if (selectedTime.isEmpty()) {
                logger.warning("No time selected when trying to add");
                showError("Erreur", "Veuillez sélectionner une heure");
                return;
            }
            logger.info("Proceeding to motif selection");
            root.getChildren().clear();
            root.getChildren().add(getContentWithMotifSelector());
        });

        actions.getChildren().addAll(ajouterBtn, suivantBtn);
        return actions;
    }

    private VBox getContentWithMotifSelector() {
        logger.info("Building motif selector view");
        VBox root = new VBox();
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(0, 0, 0, 0));

        HBox progressBar = new HBox(0);
        progressBar.setAlignment(Pos.CENTER);
        progressBar.setPadding(new Insets(22, 0, 0, 0));
        String[] steps = {"Médecin", "Date/Heure", "Motif", "Confirmation"};

        for (int i = 0; i < steps.length; i++) {
            VBox step = new VBox(5);
            step.setAlignment(Pos.CENTER);
            Circle circle = new Circle(15);
            Label stepLabel = new Label(steps[i]);

            if (i < 2) {
                circle.setFill(Color.web("#ffd600"));
                Label check = new Label("✓");
                check.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
                StackPane circlePane = new StackPane(circle, check);
                stepLabel.setStyle("-fx-text-fill: #ffd600; -fx-font-size: 16px; -fx-font-weight: bold;");
                step.getChildren().addAll(circlePane, stepLabel);
            } else if (i == 2) {
                circle.setFill(Color.web("#ffd600"));
                stepLabel.setStyle("-fx-text-fill: #ffd600; -fx-font-size: 16px; -fx-font-weight: bold;");
                step.getChildren().addAll(circle, stepLabel);
            } else {
                circle.setFill(Color.web("#e0e0e0"));
                stepLabel.setStyle("-fx-text-fill: #bdbdbd; -fx-font-size: 16px; -fx-font-weight: normal;");
                step.getChildren().addAll(circle, stepLabel);
            }

            if (i < steps.length - 1) {
                Rectangle line = new Rectangle(80, 4);
                line.setFill(i < 2 ? Color.web("#ffd600") : Color.web("#e0e0e0"));
                progressBar.getChildren().addAll(step, line);
            } else {
                progressBar.getChildren().add(step);
            }
        }

        // Bloc ID du patient en haut
        VBox idBox = new VBox(6);
        idBox.setAlignment(Pos.CENTER);
        Label patientIdLabel = new Label("ID du patient:");
        patientIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1c2c46;");
        patientIdField = new TextField();
        patientIdField.setPromptText("Entrez votre ID patient");
        patientIdField.setStyle("-fx-font-size: 16px; -fx-pref-width: 350;");
        
        Label phoneNumberLabel = new Label("Numéro de téléphone:");
        phoneNumberLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1c2c46;");
        phoneNumberField = new TextField();
        phoneNumberField.setPromptText("Entrez votre numéro de téléphone");
        phoneNumberField.setStyle("-fx-font-size: 16px; -fx-pref-width: 350;");
        
        VBox patientBox = new VBox(8);
        patientBox.getChildren().addAll(patientIdLabel, patientIdField, phoneNumberLabel, phoneNumberField);

        Label motifLabel = new Label("Veuillez saisir le motif du rendez-vous :");
        motifLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a237e; -fx-padding: 10 0 10 0;");

        // Les checkboxes des motifs fines et élégantes
        CheckBox checkControle = new CheckBox("Contrôle");
        CheckBox checkVaccination = new CheckBox("Vaccination");
        CheckBox checkOrdonnance = new CheckBox("Renouvellement d'ordonnance");
        CheckBox checkUrgence = new CheckBox("Urgence");
        CheckBox checkSpecialiste = new CheckBox("Consultation spécialiste");
        CheckBox checkExamen = new CheckBox("Examen médical");
        CheckBox checkSuivi = new CheckBox("Suivi de traitement");
        CheckBox checkAutre = new CheckBox("Autre :");
        CheckBox[] allChecks = {checkControle, checkVaccination, checkOrdonnance, checkUrgence, checkSpecialiste, checkExamen, checkSuivi, checkAutre};
        for (CheckBox cb : allChecks) {
            cb.setStyle("-fx-font-size: 15px; -fx-font-weight: normal; -fx-padding: 6 0 6 0;");
            cb.setPrefHeight(26);
            cb.setScaleX(1.05);
            cb.setScaleY(1.05);
        }

        // Zone de texte pour "Autre"
        TextArea autreDetailsArea = new TextArea();
        autreDetailsArea.setPromptText("Veuillez préciser votre besoin...");
        autreDetailsArea.setPrefRowCount(2);
        autreDetailsArea.setPrefWidth(350);
        autreDetailsArea.setDisable(true);
        autreDetailsArea.setStyle("-fx-font-size: 15px; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 8 8 8;");
        checkAutre.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autreDetailsArea.setDisable(!newVal);
            if (newVal) autreDetailsArea.requestFocus();
        });

        // Organisation en deux colonnes fines et centrées
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxWidth(340);
        grid.setPadding(new Insets(2, 0, 2, 0));
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        grid.add(checkControle, 0, 0);
        grid.add(checkVaccination, 0, 1);
        grid.add(checkOrdonnance, 0, 2);
        grid.add(checkUrgence, 0, 3);
        grid.add(checkSpecialiste, 1, 0);
        grid.add(checkExamen, 1, 1);
        grid.add(checkSuivi, 1, 2);
        grid.add(checkAutre, 0, 4);
        grid.add(autreDetailsArea, 0, 5, 2, 1);

        Button suivantBtn = new Button("Suivant");
        suivantBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 18px; -fx-font-weight: bold; -fx-border-radius: 8; -fx-padding: 10 40; -fx-effect: dropshadow(gaussian, rgba(255,214,0,0.13), 4, 0, 0, 1);");
        suivantBtn.setOnAction(e -> {
            if (patientIdField.getText().trim().isEmpty()) {
                showError("Erreur", "Veuillez saisir votre ID patient");
                return;
            }
            try {
                Integer.parseInt(patientIdField.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Erreur", "L'ID patient doit être un nombre valide");
                return;
            }
            if (phoneNumberField.getText().trim().isEmpty()) {
                showError("Erreur", "Veuillez saisir votre numéro de téléphone");
                return;
            }
            StringBuilder motifs = new StringBuilder();
            if (checkControle.isSelected()) motifs.append("Contrôle, ");
            if (checkVaccination.isSelected()) motifs.append("Vaccination, ");
            if (checkOrdonnance.isSelected()) motifs.append("Renouvellement d'ordonnance, ");
            if (checkUrgence.isSelected()) motifs.append("Urgence, ");
            if (checkSpecialiste.isSelected()) motifs.append("Consultation spécialiste, ");
            if (checkExamen.isSelected()) motifs.append("Examen médical, ");
            if (checkSuivi.isSelected()) motifs.append("Suivi de traitement, ");
            if (checkAutre.isSelected() && !autreDetailsArea.getText().isEmpty()) {
                motifs.append("Autre: ").append(autreDetailsArea.getText());
            } else if (motifs.length() > 2) {
                motifs.delete(motifs.length() - 2, motifs.length());
            }
            String motifComplet = motifs.toString();
            if (motifComplet.isEmpty()) {
                showError("Erreur", "Veuillez sélectionner au moins un motif ou saisir un besoin.");
                return;
            }
            motifField.setText(motifComplet); // Pour la suite du workflow
            logger.info("Motif sélectionné: " + motifComplet);
            root.getChildren().clear();
            root.getChildren().add(getContentWithConfirmationSelector());
        });

        // Bloc central motif + bouton
        VBox motifBox = new VBox(16, motifLabel, grid);
        motifBox.setAlignment(Pos.CENTER);
        motifBox.setPadding(new Insets(8, 0, 0, 0));
        motifBox.setMaxWidth(350);
        motifBox.setMaxHeight(180);
        motifBox.setStyle("-fx-background-color: #f8fafd; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);");

        // Bouton bien visible, rapproché du champ
        HBox boutonBox = new HBox(suivantBtn);
        boutonBox.setAlignment(Pos.CENTER);
        boutonBox.setPadding(new Insets(6, 0, 10, 0));
        VBox motifZone = new VBox(motifBox, boutonBox);
        motifZone.setAlignment(Pos.CENTER);
        motifZone.setPadding(new Insets(0, 0, 0, 0));

        root.getChildren().addAll(progressBar, patientBox, motifZone);
        return root;
    }

    public VBox getContentWithConfirmationSelector() {
        logger.info("Building confirmation view");
        if (selectedTime.isEmpty()) {
            logger.warning("No time selected for confirmation");
            showError("Erreur", "Veuillez sélectionner une heure");
            return getContentWithDateSelector();
        }

        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f9fbfd;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(0, 0, 0, 0));

        // Stepper progress bar (modern style)
        HBox progressBar = createStepper(3);

        // Title
        Label confirmationTitle = new Label("Confirmation du rendez-vous");
        confirmationTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        VBox.setMargin(confirmationTitle, new Insets(10, 0, 20, 0));

        // Card summary (styled like the reference)
        VBox card = new VBox();
        card.setMaxWidth(500);
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); -fx-background-radius: 15;");

        // Card header
        HBox cardHeader = new HBox();
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 15 15 0 0; -fx-padding: 15;");
        StackPane iconPane = new StackPane();
        iconPane.setMinSize(40, 40);
        iconPane.setStyle("-fx-background-color: #6a5acd33; -fx-background-radius: 40;");
        Label checkIcon = new Label("✓");
        checkIcon.setStyle("-fx-font-size: 20px; -fx-text-fill: #6a5acd;");
        iconPane.getChildren().add(checkIcon);
        Label headerLabel = new Label("Résumé de votre rendez-vous");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox.setMargin(headerLabel, new Insets(0, 0, 0, 15));
        cardHeader.getChildren().addAll(iconPane, headerLabel);

        // Card content grid
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setStyle("-fx-padding: 25;");
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        String formattedDate = datePicker.getValue().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));

        // Médecin
        Label medecinLabel = new Label("Médecin:");
        medecinLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label medecinValue = new Label(medecinNom != null ? medecinNom : "Non sélectionné");
        medecinValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        grid.add(medecinLabel, 0, 0);
        grid.add(medecinValue, 1, 0);

        // Spécialité
        Label specialiteLabel = new Label("Spécialité:");
        specialiteLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label specialiteValue = new Label(specialiteMedecin != null ? specialiteMedecin : "");
        specialiteValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        grid.add(specialiteLabel, 0, 1);
        grid.add(specialiteValue, 1, 1);

        // Date
        Label dateLabel = new Label("Date:");
        dateLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label dateValue = new Label(formattedDate);
        dateValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        grid.add(dateLabel, 0, 2);
        grid.add(dateValue, 1, 2);

        // Heure
        Label heureLabel = new Label("Heure:");
        heureLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label heureValue = new Label(selectedTime);
        heureValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        grid.add(heureLabel, 0, 3);
        grid.add(heureValue, 1, 3);

        // Motif
        Label motifLabel = new Label("Motif:");
        motifLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label motifValue = new Label(motifField.getText());
        motifValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        motifValue.setWrapText(true);
        grid.add(motifLabel, 0, 4);
        grid.add(motifValue, 1, 4);

        // Adresse (optionnel, à personnaliser selon vos données)
        Label adresseLabel = new Label("Adresse:");
        adresseLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #555;");
        Label adresseValue = new Label("Centre médical MaSanté, 123 Avenue de la Santé");
        adresseValue.setStyle("-fx-font-size: 15px; -fx-text-fill: #333;");
        adresseValue.setWrapText(true);
        grid.add(adresseLabel, 0, 5);
        grid.add(adresseValue, 1, 5);

        card.getChildren().addAll(cardHeader, grid);

        // Info box (reminder)
        VBox infoBox = new VBox();
        infoBox.setStyle("-fx-background-color: #fffdea; -fx-border-color: #ffd600; -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 15; -fx-max-width: 500;");
        HBox infoRow = new HBox(10);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        Label infoIcon = new Label("ℹ️");
        infoIcon.setStyle("-fx-font-size: 16px;");
        VBox infoTexts = new VBox(8);
        Label infoText1 = new Label("Un rappel sera envoyé 24h avant votre rendez-vous.");
        infoText1.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        Label infoText2 = new Label("En cas d'empêchement, merci d'annuler au moins 48h à l'avance.");
        infoText2.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        infoTexts.getChildren().addAll(infoText1, infoText2);
        infoRow.getChildren().addAll(infoIcon, infoTexts);
        infoBox.getChildren().add(infoRow);
        VBox.setMargin(infoBox, new Insets(20, 0, 20, 0));

        // Action buttons
        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER);
        Button confirmBtn = new Button("Confirmer le rendez-vous");
        confirmBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 25;");
        confirmBtn.setOnAction(e -> {
            try {
                if (medecinNom == null || medecinNom.trim().isEmpty() || medecinIdField.getText().trim().isEmpty()) {
                    logger.warning("No doctor selected during confirmation");
                    showError("Erreur", "Veuillez sélectionner un médecin");
                    return;
                }
                logger.info("Confirming appointment creation");
                handleAdd();
                showInfo("Succès", "Votre rendez-vous a été confirmé avec succès!");
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error confirming appointment", ex);
                showError("Erreur", "Erreur lors de la confirmation: " + ex.getMessage());
            }
        });
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 16px; -fx-border-color: #e74c3c; -fx-border-radius: 25; -fx-padding: 12 25;");
        cancelBtn.setOnAction(e -> {
            // Retour à la page précédente (motif)
            root.getChildren().clear();
            root.getChildren().add(getContentWithMotifSelector());
        });
        actions.getChildren().addAll(confirmBtn, cancelBtn);

        // Assemble all
        root.getChildren().addAll(progressBar, confirmationTitle, card, infoBox, actions);
        return root;
    }

    private void handleAdd() {
        try {
            logger.info("Attempting to add new appointment");
            logger.log(Level.FINE, "Current data - Motif: {0}, PatientID: {1}, Date: {2}, Time: {3}",
                    new Object[]{
                            motifField.getText(),
                            patientIdField.getText(),
                            datePicker.getValue(),
                            selectedTime
                    });

            // Validate required fields
            StringBuilder missingFields = new StringBuilder();
            if (motifField.getText().trim().isEmpty()) missingFields.append("motif, ");
            if (patientIdField.getText().trim().isEmpty()) missingFields.append("patient ID, ");
            if (phoneNumberField.getText().trim().isEmpty()) missingFields.append("téléphone, ");
            if (medecinIdField.getText().trim().isEmpty()) missingFields.append("médecin, ");
            if (datePicker.getValue() == null) missingFields.append("date, ");
            if (selectedTime.isEmpty()) missingFields.append("heure, ");
            if (missingFields.length() > 0) {
                showError("Champs manquants", "Veuillez remplir les champs suivants : " + missingFields.toString());
                return;
            }

            int userId = Integer.parseInt(patientIdField.getText().trim());
            String phoneNumber = phoneNumberField.getText().trim();
            String motif = motifField.getText().trim();
            LocalDate date = datePicker.getValue();
            String time = selectedTime;

            // Création du message personnalisé pour le SMS
            String message = String.format(
                "Bonjour, votre rendez-vous avec le %s est confirmé pour le %s à %s. Motif : %s. Merci pour votre confiance. À bientôt ! 👋",
                medecinNom,
                date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                time,
                motif
            );

            // Utilisation du Messaging Service SID pour envoyer le SMS
            smsService.sendSmsWithServiceSid(phoneNumber, message);

            showInfo("Succès", "Votre rendez-vous a été confirmé avec succès! Un SMS de confirmation a été envoyé.");

            clearFields();
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Invalid patient ID format", e);
            showError("Erreur", "L'ID du patient doit être un nombre valide");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error adding appointment", e);
            showError("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void clearFields() {
        logger.info("Clearing form fields");
        if (motifField != null) motifField.clear();
        if (patientIdField != null) patientIdField.clear();
        if (phoneNumberField != null) phoneNumberField.clear();
        if (medecinIdField != null) medecinIdField.clear();
        if (datePicker != null) datePicker.setValue(null);
        selectedTime = "";
        if (selectedHourBtn != null) selectedHourBtn.setText("");
        if (tableView != null) {
            tableView.getSelectionModel().clearSelection();
        }
    }

    private void showError(String title, String message) {
        logger.log(Level.WARNING, "Showing error dialog: {0} - {1}", new Object[]{title, message});
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        logger.log(Level.INFO, "Showing info dialog: {0} - {1}", new Object[]{title, message});
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private HBox createStepper(int currentStep) {
        logger.log(Level.FINE, "Creating stepper at step {0}", currentStep);
        String[] steps = {"Médecin", "Date/Heure", "Motif", "Confirmation"};
        HBox stepper = new HBox();
        stepper.setAlignment(Pos.CENTER);
        stepper.setSpacing(0);
        for (int i = 0; i < steps.length; i++) {
            VBox stepBox = new VBox(6);
            stepBox.setAlignment(Pos.CENTER);
            StackPane circlePane = new StackPane();
            Circle circle = new Circle(20);

            Label iconLabel;
            if (i < currentStep) {
                circle.setFill(Color.web("#FFD600"));
                iconLabel = new Label("\u2713");
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
            } else if (i == currentStep) {
                circle.setFill(Color.web("#FFD600"));
                iconLabel = new Label("●");
                iconLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
            } else {
                circle.setFill(Color.web("#e0e0e0"));
                iconLabel = new Label(String.valueOf(i + 1));
                iconLabel.setStyle("-fx-text-fill: #bdbdbd; -fx-font-size: 20px; -fx-font-weight: bold;");
            }
            circlePane.getChildren().addAll(circle, iconLabel);
            Label label = new Label(steps[i]);
            label.setStyle("-fx-font-size: 15px; -fx-font-weight: 600;");
            if (i <= currentStep) {
                label.setTextFill(Color.web("#FFD600"));
            } else {
                label.setTextFill(Color.web("#bdbdbd"));
            }
            stepBox.getChildren().addAll(circlePane, label);
            stepper.getChildren().add(stepBox);
            if (i < steps.length - 1) {
                VBox lineBox = new VBox();
                lineBox.setAlignment(Pos.CENTER);
                Rectangle line = new Rectangle(56, 5);
                line.setFill(i < currentStep ? Color.web("#FFD600") : Color.web("#e0e0e0"));
                lineBox.getChildren().add(line);
                stepper.getChildren().add(lineBox);
            }
        }
        stepper.setPadding(new Insets(20, 0, 30, 0));
        return stepper;
    }

    // Nouvelle vue CRUD minimaliste pour les rendez-vous
    public VBox getCrudTableOnlyView() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));

        // TableView
        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RendezVous, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<RendezVous, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> {
            LocalDateTime d = cell.getValue().getDate();
            return new ReadOnlyStringWrapper(d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        });

        TableColumn<RendezVous, String> motifCol = new TableColumn<>("Motif");
        motifCol.setCellValueFactory(new PropertyValueFactory<>("motif"));

        TableColumn<RendezVous, Integer> patientCol = new TableColumn<>("ID Patient");
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));

        TableColumn<RendezVous, Integer> medecinCol = new TableColumn<>("ID Médecin");
        medecinCol.setCellValueFactory(new PropertyValueFactory<>("medecinId"));

        TableColumn<RendezVous, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        TableColumn<RendezVous, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button modifBtn = new Button("Modifier");
            final Button supprBtn = new Button("Supprimer");
            final Button rateBtn = new Button("Évaluer");
            final HBox box = new HBox(6, modifBtn, supprBtn, rateBtn);
            {
                modifBtn.setStyle("-fx-background-color: #26c6da; -fx-text-fill: white;");
                supprBtn.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white;");
                rateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                modifBtn.setOnAction(e -> {
                    RendezVous rv = getTableView().getItems().get(getIndex());
                    showEditDialog(rv);
                });
                supprBtn.setOnAction(e -> {
                    RendezVous rv = getTableView().getItems().get(getIndex());
                    supprimerRendezVous(rv);
                });
                rateBtn.setOnAction(e -> {
                    RendezVous rv = getTableView().getItems().get(getIndex());
                    handleRateButton(rv);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableView.getColumns().addAll(idCol, dateCol, motifCol, patientCol, medecinCol, statutCol, actionCol);

        // Charger les rendez-vous depuis la base
        ObservableList<RendezVous> data = FXCollections.observableArrayList(service.listerRendezVous());
        tableView.setItems(data);

        layout.getChildren().addAll(new Label("Liste des rendez-vous"), tableView);
        return layout;
    }

    // Affiche une boîte de dialogue de modification (exemple simple)
    private void showEditDialog(RendezVous rv) {
        TextInputDialog dialog = new TextInputDialog(rv.getMotif());
        dialog.setTitle("Modifier le motif");
        dialog.setHeaderText("Modifier le motif du rendez-vous ID " + rv.getId());
        dialog.setContentText("Nouveau motif :");
        dialog.showAndWait().ifPresent(newMotif -> {
            rv.setMotif(newMotif);
            service.modifierRendezVous(rv);
            tableView.refresh();
        });
    }

    // Supprimer un rendez-vous
    private void supprimerRendezVous(RendezVous rv) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le rendez-vous ID " + rv.getId() + " ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce rendez-vous ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.supprimerRendezVous(rv.getId());
                tableView.getItems().remove(rv);
            }
        });
    }

    private void handleRateButton(RendezVous rv) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/frontend/src/views/Rating.fxml"));
            Stage ratingStage = new Stage();
            ratingStage.setScene(new Scene(loader.load()));
            ratingStage.setTitle("Évaluation du médecin");

            RatingController controller = loader.getController();
            controller.initialize(medecinId, getCurrentUserId()); // À adapter selon ta logique utilisateur

            ratingStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur de chargement de la fenêtre d'évaluation");
        }
    }

    // Méthode utilitaire fictive pour récupérer l'ID utilisateur courant
    private int getCurrentUserId() {
        // TODO: Remplacer par la vraie logique d'authentification/utilisateur
        return 1;
    }
}