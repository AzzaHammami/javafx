package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.services.Servicerendez_vous;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.logging.Level;

public class AppointmentCrudWindow {
    private static final Logger logger = Logger.getLogger(AppointmentCrudWindow.class.getName());
    private final Servicerendez_vous service;
    private TableView<RendezVous> tableView;
    private TextField motifField;
    private TextField patientIdField;
    private TextField medecinIdField;
    private DatePicker datePicker;
    private Button selectedHourBtn = new Button();
    private String medecinNom;
    private String specialiteMedecin;
    private String medecinImageUrl;
    private String selectedTime = "";
    private int medecinId;

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
        this.medecinIdField = new TextField(String.valueOf(medecinId));

        logger.log(Level.FINE, "Initialized fields - MedecinIDField: {0}", medecinIdField.getText());
    }

    public AppointmentCrudWindow() {
        logger.info("Creating empty AppointmentCrudWindow");
        service = new Servicerendez_vous();
        this.selectedHourBtn = new Button();
        this.motifField = new TextField();
        this.patientIdField = new TextField();
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

        VBox formBox = new VBox(12);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40, 0, 0, 0));

        // Patient ID Field
        Label patientIdLabel = new Label("ID du patient:");
        patientIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1c2c46;");
        patientIdField = new TextField();
        patientIdField.setPromptText("Entrez votre ID patient");
        patientIdField.setStyle("-fx-font-size: 16px; -fx-pref-width: 350;");

        // Motif Field
        Label motifLabel = new Label("Veuillez saisir le motif du rendez-vous :");
        motifLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1c2c46;");
        motifField = new TextField();
        motifField.setPromptText("Motif de consultation");
        motifField.setStyle("-fx-font-size: 16px; -fx-pref-width: 350;");

        formBox.getChildren().addAll(
                patientIdLabel,
                patientIdField,
                new Label(""), // Spacer
                motifLabel,
                motifField
        );

        Button suivantBtn = new Button("Suivant");
        suivantBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-padding: 12 25;");

        suivantBtn.setOnAction(e -> {
            if (patientIdField.getText().trim().isEmpty()) {
                logger.warning("No patient ID entered");
                showError("Erreur", "Veuillez saisir votre ID patient");
            } else if (motifField.getText().trim().isEmpty()) {
                logger.warning("No motif entered");
                showError("Erreur", "Veuillez saisir un motif pour le rendez-vous");
            } else {
                try {
                    // Validate patient ID is numeric
                    Integer.parseInt(patientIdField.getText().trim());
                    logger.info("Proceeding to confirmation");
                    root.getChildren().clear();
                    root.getChildren().add(getContentWithConfirmationSelector());
                } catch (NumberFormatException ex) {
                    logger.warning("Invalid patient ID format");
                    showError("Erreur", "L'ID patient doit être un nombre valide");
                }
            }
        });

        VBox center = new VBox(30, formBox, suivantBtn);
        center.setAlignment(Pos.CENTER);

        root.getChildren().addAll(progressBar, center);
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

            if (i < 3) {
                circle.setFill(Color.web("#ffd600"));
                Label check = new Label("✓");
                check.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
                StackPane circlePane = new StackPane(circle, check);
                stepLabel.setStyle("-fx-text-fill: #ffd600; -fx-font-size: 16px; -fx-font-weight: bold;");
                step.getChildren().addAll(circlePane, stepLabel);
            } else {
                circle.setFill(Color.web("#ffd600"));
                stepLabel.setStyle("-fx-text-fill: #ffd600; -fx-font-size: 16px; -fx-font-weight: bold;");
                step.getChildren().addAll(circle, stepLabel);
            }

            if (i < steps.length - 1) {
                Rectangle line = new Rectangle(80, 4);
                line.setFill(Color.web("#ffd600"));
                progressBar.getChildren().addAll(step, line);
            } else {
                progressBar.getChildren().add(step);
            }
        }

        VBox confirmationBox = new VBox(20);
        confirmationBox.setAlignment(Pos.CENTER);
        confirmationBox.setPadding(new Insets(40, 0, 0, 0));

        VBox summaryBox = new VBox(10);
        summaryBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 20;");

        String formattedDate = datePicker.getValue().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
        logger.log(Level.FINE, "Formatted appointment date: {0}", formattedDate);

        Label confirmationLabel = new Label("Confirmation du rendez-vous");
        confirmationLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1c2c46;");

        Label doctorLabel = new Label("Médecin: " + (medecinNom != null ? medecinNom : "Non sélectionné"));
        doctorLabel.setStyle("-fx-font-size: 16px;");

        Label dateLabel = new Label("Date: " + formattedDate);
        dateLabel.setStyle("-fx-font-size: 16px;");

        Label timeLabel = new Label("Heure: " + selectedTime);
        timeLabel.setStyle("-fx-font-size: 16px;");

        Label reasonLabel = new Label("Motif: " + motifField.getText());
        reasonLabel.setStyle("-fx-font-size: 16px;");

        summaryBox.getChildren().addAll(confirmationLabel, doctorLabel, dateLabel, timeLabel, reasonLabel);

        Button confirmBtn = new Button("Confirmer le rendez-vous");
        confirmBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 25;");

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
                showError("Erreur", "Une erreur est survenue lors de la confirmation: " + ex.getMessage());
            }
        });

        confirmationBox.getChildren().addAll(summaryBox, confirmBtn);
        root.getChildren().addAll(progressBar, confirmationBox);

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
            if (datePicker.getValue() == null) missingFields.append("date, ");
            if (selectedTime.isEmpty()) missingFields.append("time, ");

            if (missingFields.length() > 0) {
                String errorMsg = "Missing fields: " + missingFields.substring(0, missingFields.length() - 2);
                logger.warning(errorMsg);
                showError("Erreur", "Veuillez remplir tous les champs: " + missingFields.substring(0, missingFields.length() - 2));
                return;  // This return was missing in your original code
            }

            RendezVous rv = new RendezVous();
            rv.setMotif(motifField.getText());
            rv.setPatientId(Integer.parseInt(patientIdField.getText()));
            rv.setMedecinId(this.medecinId);
            rv.setDate(datePicker.getValue().atTime(LocalTime.parse(selectedTime)));
            rv.setStatut("En attente");

            logger.log(Level.INFO, "Creating appointment: {0}", rv);

            service.ajouterRendezVous(rv);
            logger.info("Appointment successfully added to service");

            clearFields();
            showInfo("Succès", "Rendez-vous ajouté avec succès");

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
                line.setArcWidth(5);
                line.setArcHeight(5);
                if (i < currentStep) {
                    line.setFill(Color.web("#FFD600"));
                } else {
                    line.setFill(Color.web("#e0e0e0"));
                }
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
            final HBox box = new HBox(6, modifBtn, supprBtn);
            {
                modifBtn.setStyle("-fx-background-color: #26c6da; -fx-text-fill: white;");
                supprBtn.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white;");
                modifBtn.setOnAction(e -> {
                    RendezVous rv = getTableView().getItems().get(getIndex());
                    showEditDialog(rv);
                });
                supprBtn.setOnAction(e -> {
                    RendezVous rv = getTableView().getItems().get(getIndex());
                    supprimerRendezVous(rv);
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
}