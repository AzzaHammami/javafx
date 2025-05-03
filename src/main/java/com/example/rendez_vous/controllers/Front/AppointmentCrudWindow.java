package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.models.RendezVous;
import com.example.rendez_vous.services.Servicerendez_vous;
import com.example.rendez_vous.services.SmsService;
import com.example.rendez_vous.controllers.Front.PatientDashboardView; // Import PatientDashboardView pour garantir l'accès au dashboard
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppointmentCrudWindow {
    private static final Logger logger = Logger.getLogger(AppointmentCrudWindow.class.getName());
    private final Servicerendez_vous service;
    // UI Components that need to be accessed across methods
    private TextField motifField;
    private TextField patientIdField;
    private TextField phoneNumberField; // Added for confirmation and SMS
    private TextField medecinIdField; // To store the selected medecin's ID
    private DatePicker datePicker;
    private Button selectedHourBtn = new Button(); // Might not be needed if selectedTime is stored directly

    // Data related to the selected doctor and appointment
    private String medecinNom;
    private String specialiteMedecin;
    private String medecinImageUrl;
    private String selectedTime = ""; // Store selected time slot as "HH:mm" string
    private int medecinId;

    // Services
    private SmsService smsService = new SmsService(); // Initialize SMS service

    // Constructor for when a doctor is pre-selected
    public AppointmentCrudWindow(int medecinId, String medecinNom, String specialiteMedecin, String medecinImageUrl) {
        this.service = new Servicerendez_vous();
        this.medecinId = medecinId;
        this.medecinNom = medecinNom;
        this.specialiteMedecin = specialiteMedecin;
        this.medecinImageUrl = medecinImageUrl;

        // Initialize UI fields (even if they are created later in methods)
        this.motifField = new TextField();
        this.patientIdField = new TextField();
        this.phoneNumberField = new TextField();
        this.medecinIdField = new TextField(String.valueOf(medecinId)); // Pre-fill medecin ID field
        this.datePicker = new DatePicker(LocalDate.now()); // Initialize date picker

    }

    // Constructor for when no doctor is initially selected (e.g., starting from scratch)
    public AppointmentCrudWindow() {
        this.service = new Servicerendez_vous();

        // Initialize UI fields
        this.motifField = new TextField();
        this.patientIdField = new TextField();
        this.phoneNumberField = new TextField();
        this.medecinIdField = new TextField(); // Empty medecin ID field
        this.datePicker = new DatePicker(LocalDate.now()); // Initialize date picker

        // Doctor details will be set later via setMedecin or selection process
        this.medecinId = 0; // Or some indicator for no doctor selected
        this.medecinNom = null;
        this.specialiteMedecin = null;
        this.medecinImageUrl = null;
    }

    // Method to explicitly set doctor details if using the default constructor
    public void setMedecin(int id, String nom, String specialite, String imageUrl) {
        this.medecinId = id;
        this.medecinNom = nom;
        this.specialiteMedecin = specialite;
        this.medecinImageUrl = imageUrl;
        if(this.medecinIdField != null) {
            this.medecinIdField.setText(String.valueOf(id));
        } else {
            this.medecinIdField = new TextField(String.valueOf(id)); // Ensure it's initialized
        }
    }

    // --- Step 1: Date/Time Selection View ---

    public VBox getContentWithDateSelector() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(0));
        root.setStyle("-fx-background-color: #f8fafd;");
        // --- Contenu principal pleine largeur ---
        VBox mainContent = new VBox(15);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(40, 0, 0, 0));
        mainContent.setMinHeight(900); // Force un contenu plus haut pour activer le scroll si besoin
        // Progress Bar - Step 1 (Date/Heure is step 1 in this flow if doctor is pre-selected)
        HBox progressBar = createProgressBar(1); // Index 1 for Date/Heure

        // Doctor Card (Reused)
        VBox medCard = createDoctorCard(); // Shows selected doctor or prompt to choose

        // Calendar Section (Reused)
        VBox calendarSection = createCalendarSection(); // Contains DatePicker and Time Slots

        mainContent.getChildren().addAll(progressBar, medCard, calendarSection);

        // Action Buttons (Reused)
        HBox actions = createActionButtonsForDateStep(root); // Pass root to allow navigation
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(30, 0, 30, 0));

        mainContent.getChildren().add(actions);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(mainContent, Priority.NEVER); // Pour laisser le scroll gérer la hauteur
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return root;
    }

    // --- Step 2: Motif Selection View ---

    public VBox getContentWithMotifSelector() {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f8fafd;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40, 0, 0, 0));

        // Ajout de la barre de progression étape 2 (Motif)
        HBox progressBar = createProgressBar(2);
        root.getChildren().add(progressBar);

        // Carte centrale
        VBox card = new VBox(28);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(32, 38, 38, 38));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        card.setMaxWidth(600);

        // Partie infos patient (inchangée)
        VBox patientBox = new VBox(15);
        patientBox.setAlignment(Pos.CENTER_LEFT);
        patientBox.setMaxWidth(450);
        Label patientIdLabel = new Label("Votre ID Patient:");
        patientIdLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        patientIdField.setPromptText("Entrez votre numéro d'identification");
        patientIdField.setStyle("-fx-font-size: 15px; -fx-pref-height: 40px;");
        Label phoneLabel = new Label("Votre Numéro de Téléphone:");
        phoneLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        phoneNumberField.setPromptText("Veuillez saisir votre numéro (ex: +216XXXXXXXX)");
        phoneNumberField.setStyle("-fx-font-size: 15px; -fx-pref-height: 40px;");
        patientBox.getChildren().addAll(patientIdLabel, patientIdField, phoneLabel, phoneNumberField);

        Label title = new Label("Veuillez saisir le motif du rendez-vous :");
        title.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: #1a237e; -fx-alignment: center;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // Motifs en 2 colonnes
        GridPane grid = new GridPane();
        grid.setHgap(32);
        grid.setVgap(16);
        grid.setAlignment(Pos.CENTER);

        CheckBox cb1 = new CheckBox("Contrôle");
        CheckBox cb2 = new CheckBox("Vaccination");
        CheckBox cb3 = new CheckBox("Renouvellement d'ordonnance");
        CheckBox cb4 = new CheckBox("Urgence");
        CheckBox cb5 = new CheckBox("Consultation spécialiste");
        CheckBox cb6 = new CheckBox("Examen médical");
        CheckBox cb7 = new CheckBox("Suivi de traitement");
        grid.add(cb1, 0, 0); grid.add(cb5, 1, 0);
        grid.add(cb2, 0, 1); grid.add(cb6, 1, 1);
        grid.add(cb3, 0, 2); grid.add(cb7, 1, 2);
        grid.add(cb4, 0, 3);

        CheckBox cbAutre = new CheckBox("Autre :");
        grid.add(cbAutre, 1, 3);

        TextArea autreDetails = new TextArea();
        autreDetails.setPromptText("Veuillez préciser votre besoin...");
        autreDetails.setDisable(true);
        autreDetails.setPrefRowCount(2);
        autreDetails.setMaxWidth(Double.MAX_VALUE);
        autreDetails.setStyle("-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8;");
        cbAutre.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autreDetails.setDisable(!newVal);
            if (newVal) autreDetails.requestFocus();
            else autreDetails.clear();
        });

        // Bouton Suivant (centré en bas de la carte)
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(18, 0, 0, 0));
        Button suivantBtn = new Button("Suivant");
        suivantBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand;");
        suivantBtn.setOnAction(e -> handleGoToConfirmation(root));
        actions.getChildren().add(suivantBtn);

        card.getChildren().addAll(patientBox, title, grid, autreDetails, actions);
        root.getChildren().add(card);
        return root;
    }


    // --- Step 3: Confirmation View ---

    public VBox getContentWithConfirmationSelector() {

        // Basic validation before building the view
        if (datePicker == null || datePicker.getValue() == null) {
            showError("Erreur Interne", "La date n'a pas été sélectionnée.");
            return getContentWithDateSelector(); // Go back to date selection
        }
        if (selectedTime.isEmpty()) {
            showError("Erreur Interne", "L'heure n'a pas été sélectionnée.");
            return getContentWithDateSelector(); // Go back to date selection
        }
        if (motifField == null || motifField.getText().trim().isEmpty()) {
            showError("Champ requis", "Veuillez sélectionner ou saisir un motif de consultation.");
            return getContentWithMotifSelector(); // Retour à l'étape motif
        }
        if (patientIdField == null || patientIdField.getText().trim().isEmpty()) {
            showError("Erreur Interne", "L'ID Patient n'a pas été saisi.");
            return getContentWithMotifSelector(); // Go back to motif selection
        }
        if (phoneNumberField == null || phoneNumberField.getText().trim().isEmpty()) {
            showError("Erreur Interne", "Le numéro de téléphone n'a pas été saisi.");
            return getContentWithMotifSelector(); // Go back to motif selection
        }


        VBox root = new VBox();
        root.setStyle("-fx-background-color: white;"); // Consistent background
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(0)); // No padding for root if scrollpane is used

        VBox scrollContent = new VBox(20); // Spacing between elements
        scrollContent.setAlignment(Pos.TOP_CENTER);
        scrollContent.setPadding(new Insets(0, 30, 30, 30)); // Padding for content inside scrollpane
        scrollContent.setStyle("-fx-background-color: transparent;");

        // Progress Bar - Step 3 (Confirmation)
        HBox progressBar = createProgressBar(3); // Index 3 for Confirmation step

        // Title
        Label confirmationTitle = new Label("Confirmation du rendez-vous");
        confirmationTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        VBox.setMargin(confirmationTitle, new Insets(5, 0, 10, 0)); // Top margin adjusted

        // Summary Card
        VBox card = new VBox(15); // Spacing inside the card
        card.setMaxWidth(600);
        card.setStyle("-fx-background-color: #f8fafd; -fx-padding: 25px; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        // --- Doctor Info Section ---
        HBox medInfo = new HBox(20);
        medInfo.setAlignment(Pos.CENTER_LEFT);
        ImageView avatar = createDoctorAvatar(); // Reuse avatar creation
        VBox medLabels = new VBox();
        medLabels.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Dr " + (medecinNom != null ? medecinNom : "Nom inconnu"));
        nameLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");
        Label specialiteLabel = new Label(specialiteMedecin != null ? specialiteMedecin : "Spécialité inconnue");
        specialiteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575; -fx-font-weight: bold;");
        medLabels.getChildren().addAll(nameLabel, specialiteLabel);
        medInfo.getChildren().addAll(avatar, medLabels);
        card.getChildren().add(medInfo);

        // --- Appointment Details Section ---
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        VBox detailsBox = new VBox(12); // Spacing for details
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        LocalDate date = datePicker.getValue();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String formattedDate = date.format(dateFormatter);
        String formattedTime = selectedTime; // Assuming selectedTime is "HH:mm"
        detailsBox.getChildren().addAll(
                createDetailRow("Date:", formattedDate),
                createDetailRow("Heure:", formattedTime),
                createDetailRow("Motif:", motifField.getText()),
                createDetailRow("ID Patient:", patientIdField.getText()),
                createDetailRow("Téléphone:", phoneNumberField.getText())
        );
        card.getChildren().addAll(separator, detailsBox);

        // Action Buttons
        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER); // Center buttons
        actions.setPadding(new Insets(25, 0, 10, 0)); // Padding around buttons

        Button retourBtn = new Button("Retour");
        retourBtn.setStyle("-fx-background-color: #BDBDBD; -fx-text-fill: #424242; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 30;");
        retourBtn.setOnAction(e -> {
            // Go back to the motif selector view
            VBox parentContainer = findParentContainer(root); // Helper to find the main content area
            if (parentContainer != null) {
                parentContainer.getChildren().clear();
                parentContainer.getChildren().add(getContentWithMotifSelector());
            } else { // Fallback
                root.getChildren().clear();
                root.getChildren().add(getContentWithMotifSelector());
            }
        });


        Button confirmerBtn = new Button("Confirmer le RDV");
        confirmerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 30; -fx-effect: dropshadow(gaussian, rgba(46,204,113,0.3), 5, 0, 0, 1);");
        confirmerBtn.setOnAction(e -> {
            handleConfirmAppointment(e); // Pass the event to close the stage later
        });


        actions.getChildren().addAll(retourBtn, confirmerBtn);

        // Assemble the view
        scrollContent.getChildren().addAll(progressBar, confirmationTitle, card, actions);

        // Use ScrollPane for potentially long content
        ScrollPane scrollPane = new ScrollPane(scrollContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Allow scrollpane to grow

        root.getChildren().add(scrollPane);
        root.setId("confirmationArea"); // Optional ID

        return root;
    }


    // --- Helper Methods for UI Construction ---

    private HBox createProgressBar(int currentStepIndex) {
        // Adjusted steps based on flow where doctor is selected first
        String[] steps = {"Médecin", "Date/Heure", "Motif", "Confirmation"};
        HBox progressBar = new HBox();
        progressBar.setAlignment(Pos.CENTER);
        progressBar.setSpacing(0); // No space between elements for continuous look
        progressBar.setPadding(new Insets(22, 0, 20, 0)); // Padding top/bottom

        for (int i = 0; i < steps.length; i++) {
            VBox stepBox = new VBox(6); // Spacing between circle and label
            stepBox.setAlignment(Pos.CENTER);
            StackPane circlePane = new StackPane();
            Circle circle = new Circle(16); // Slightly smaller circle

            Label iconLabel = new Label();
            iconLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            if (i < currentStepIndex) { // Completed step
                circle.setFill(Color.web("#FFD600")); // Gold color for completed
                iconLabel.setText("✓");
                iconLabel.setTextFill(Color.WHITE);
            } else if (i == currentStepIndex) { // Current step
                circle.setFill(Color.web("#0288d1")); // Blue color for current
                // iconLabel.setText(String.valueOf(i + 1)); // Number for current step
                // iconLabel.setTextFill(Color.WHITE);
                // Using a filled circle symbol for current step might look cleaner
                iconLabel.setText("●"); // Or use a specific icon
                iconLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");


            } else { // Future step
                circle.setFill(Color.web("#e0e0e0")); // Gray for future
                iconLabel.setText(String.valueOf(i + 1)); // Number for future step
                iconLabel.setTextFill(Color.web("#bdbdbd")); // Gray text
            }
            circlePane.getChildren().addAll(circle, iconLabel);

            Label label = new Label(steps[i]);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Slightly smaller label
            if (i <= currentStepIndex) {
                label.setTextFill(Color.web("#333")); // Darker text for active/completed
            } else {
                label.setTextFill(Color.web("#bdbdbd")); // Gray text for future
            }

            stepBox.getChildren().addAll(circlePane, label);
            progressBar.getChildren().add(stepBox);

            // Line connecting steps
            if (i < steps.length - 1) {
                VBox lineBox = new VBox(); // Use VBox to center the line vertically if needed
                lineBox.setAlignment(Pos.CENTER);
                lineBox.setPadding(new Insets(0, 5, 20, 5)); // Add horizontal padding, adjust bottom to align with circles
                Rectangle line = new Rectangle(60, 3); // Thinner line
                line.setArcWidth(3);
                line.setArcHeight(3);
                if (i < currentStepIndex) {
                    line.setFill(Color.web("#FFD600")); // Gold line for completed segments
                } else {
                    line.setFill(Color.web("#e0e0e0")); // Gray line for future segments
                }
                lineBox.getChildren().add(line);
                progressBar.getChildren().add(lineBox);
            }
        }
        return progressBar;
    }


    private VBox createDoctorCard() {
        VBox medCard = new VBox();
        medCard.setAlignment(Pos.TOP_LEFT); // Default alignment
        medCard.setStyle("-fx-background-color: #0288d1; -fx-background-radius: 10; -fx-padding: 18 24 18 24; -fx-min-width: 600; -fx-max-width: 700; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        VBox.setMargin(medCard, new Insets(0, 0, 20, 0)); // Add bottom margin

        if (medecinNom == null || medecinNom.trim().isEmpty()) {
            // Card prompting user to select a doctor
            VBox noMedecinBox = new VBox(12);
            noMedecinBox.setAlignment(Pos.CENTER);
            Label aucunLabel = new Label("Aucun médecin choisi");
            aucunLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
            Button choisirBtn = new Button("Choisir un médecin");
            choisirBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #01579b; -fx-font-size: 15px; -fx-font-weight: bold; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 10 20; -fx-cursor: hand;");
            choisirBtn.setOnAction(e -> {
                try {
                    Node sourceNode = (Node) e.getSource();
                    Scene scene = sourceNode.getScene();
                    if (scene != null && scene.getRoot() instanceof BorderPane) {
                        BorderPane borderPane = (BorderPane) scene.getRoot();
                        showError("Fonctionnalité Incomplète", "La navigation vers la sélection du médecin doit être implémentée.");
                    } else {
                        showError("Erreur de Navigation", "Impossible de naviguer vers la sélection du médecin.");
                    }
                } catch (Exception ex) {
                    showError("Erreur", "Une erreur s'est produite lors de la tentative de navigation.");
                }
            });
            noMedecinBox.getChildren().addAll(aucunLabel, choisirBtn);
            medCard.getChildren().add(noMedecinBox);
            medCard.setAlignment(Pos.CENTER); // Center content when no doctor is selected
        } else {
            HBox medInfo = new HBox(16); // Spacing between avatar and text
            medInfo.setAlignment(Pos.CENTER_LEFT);
            ImageView avatar = createDoctorAvatar(); // Get the styled avatar
            VBox medLabels = new VBox();
            medLabels.setAlignment(Pos.CENTER_LEFT);
            Label nameLabel = new Label("Dr " + (medecinNom != null ? medecinNom : "Nom inconnu"));
            nameLabel.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: white;");
            Label specialiteLabel = new Label(specialiteMedecin != null ? specialiteMedecin : "Spécialité inconnue");
            specialiteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #757575; -fx-font-weight: bold;");
            medLabels.getChildren().addAll(nameLabel, specialiteLabel);
            medInfo.getChildren().addAll(avatar, medLabels);
            medCard.getChildren().add(medInfo);
        }
        return medCard;
    }

    private ImageView createDoctorAvatar() {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(70); // Slightly smaller avatar
        avatar.setFitHeight(70);
        avatar.setPreserveRatio(false); // Ensure it fills the 70x70 space

        // Try loading the image, use placeholder on error or if null/empty
        try {
            if (medecinImageUrl != null && !medecinImageUrl.isEmpty() && !medecinImageUrl.equalsIgnoreCase("null")) {
                Image img = new Image(medecinImageUrl, true); // Load in background
                if (img.isError()) {
                    setPlaceholderAvatarStyle(avatar);
                } else {
                    avatar.setImage(img);
                }
            } else {
                setPlaceholderAvatarStyle(avatar);
            }
        } catch (Exception e) {
            setPlaceholderAvatarStyle(avatar);
        }


        // Apply rounded clipping mask
        Rectangle avatarClip = new Rectangle(avatar.getFitWidth(), avatar.getFitHeight());
        avatarClip.setArcWidth(15); // Adjust corner radius
        avatarClip.setArcHeight(15);
        avatar.setClip(avatarClip);

        // Add a subtle border effect using StackPane (optional)
        // StackPane avatarPane = new StackPane(avatar);
        // avatarPane.setStyle("-fx-border-color: rgba(255, 255, 255, 0.5); -fx-border-width: 1; -fx-border-radius: 15; -fx-background-radius: 15;");
        // return avatarPane; // If using StackPane, return it instead

        return avatar; // Return ImageView directly if no border pane needed
    }

    // Helper to set style for placeholder avatar
    private void setPlaceholderAvatarStyle(ImageView avatar) {
        // Simple gray background as placeholder
        avatar.setImage(null); // Ensure no previous image is shown
        avatar.setStyle("-fx-background-color: #B0BEC5; -fx-background-radius: 15;");
        // You could add an icon here too if desired
    }


    private VBox createDoctorLabels() {
        VBox medLabels = new VBox(4); // Spacing between name and specialty
        Label nomMed = new Label("Dr " + (medecinNom != null ? medecinNom : "Nom Indisponible"));
        nomMed.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label specMed = new Label(specialiteMedecin != null ? specialiteMedecin : "Spécialité Indisponible");
        specMed.setStyle("-fx-font-size: 15px; -fx-text-fill: #e1f5fe;"); // Lighter blue/white for specialty
        medLabels.getChildren().addAll(nomMed, specMed);
        return medLabels;
    }

    private VBox createCalendarSection() {
        VBox calendarSection = new VBox(18); // Spacing between elements
        // Styling: White background, subtle shadow, padding
        calendarSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 25px 30px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label sectionTitle = new Label("Sélectionnez une date et une heure");
        sectionTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a237e; -fx-alignment: center;");
        sectionTitle.setMaxWidth(Double.MAX_VALUE); // Make title span width
        sectionTitle.setAlignment(Pos.CENTER);
        calendarSection.getChildren().add(sectionTitle);

        // Date Picker Row
        HBox dateRow = new HBox();
        dateRow.setAlignment(Pos.CENTER);
        // datePicker is initialized in constructor
        datePicker.setPromptText("Choisir une date");
        datePicker.setPrefWidth(250); // Fixed width for datepicker
        datePicker.setStyle("-fx-font-size: 16px;"); // Style datepicker text

        // --- Date Cell Factory for disabling past/full dates ---
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(false); // Reset disable state
                setStyle(""); // Reset style
                setTooltip(null); // Reset tooltip

                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE;"); // Style for past dates
                    return; // Don't check availability for past dates
                }

                // Check availability only if a doctor is selected
                if (medecinId > 0) {
                    try {
                        List<LocalTime> takenTimes = service.getTakenTimesForDate(date, medecinId);
                        // Assuming 12 slots per day based on the horaires array
                        if (takenTimes.size() >= 12) {
                            setDisable(true);
                            setStyle("-fx-background-color: #FFCDD2; -fx-opacity: 0.7;"); // Light red for full dates
                            setTooltip(new Tooltip("Cette date est complète"));
                        }
                        // Optional: Style for partially booked dates?
                        // else if (!takenTimes.isEmpty()) {
                        //     setStyle("-fx-background-color: #FFF9C4;"); // Light yellow
                        // }

                    } catch (Exception e) {
                        // Optionally disable date if availability check fails? Or just log?
                        // setDisable(true);
                        // setStyle("-fx-background-color: #FFEBEE;"); // Indicate error?
                    }
                } else {
                    // If no doctor selected, maybe disable date selection?
                    // setDisable(true);
                    // setTooltip(new Tooltip("Veuillez d'abord sélectionner un médecin"));
                }
            }
        });

        dateRow.getChildren().add(datePicker);
        calendarSection.getChildren().add(dateRow);

        // Time Slots Section
        Label horairesTitle = new Label("Choisissez un horaire disponible");
        horairesTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: 600; -fx-text-fill: #333; -fx-alignment: center;");
        horairesTitle.setMaxWidth(Double.MAX_VALUE);
        horairesTitle.setAlignment(Pos.CENTER);
        calendarSection.getChildren().add(horairesTitle);

        // Container for the time slots grid (to allow dynamic updates)
        VBox slotsContainer = new VBox();
        slotsContainer.setAlignment(Pos.CENTER);
        slotsContainer.setId("slotsContainer"); // ID for potential lookup
        slotsContainer.getChildren().add(createTimeSlotsGrid()); // Initial grid
        calendarSection.getChildren().add(slotsContainer);

        // Listener to update time slots when date changes
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                selectedTime = ""; // Reset selected time when date changes
                // Update the grid within the container
                slotsContainer.getChildren().clear();
                slotsContainer.getChildren().add(createTimeSlotsGrid());
            }
        });

        return calendarSection;
    }


    private GridPane createTimeSlotsGrid() {
        GridPane slotsGrid = new GridPane();
        slotsGrid.setHgap(12); // Horizontal gap
        slotsGrid.setVgap(12); // Vertical gap
        slotsGrid.setAlignment(Pos.CENTER);
        slotsGrid.setPadding(new Insets(10, 0, 0, 0)); // Padding above the grid

        // Check if a date is selected and a doctor ID is available
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate == null) {
            slotsGrid.add(new Label("Veuillez sélectionner une date."), 0, 0);
            return slotsGrid;
        }
        if (this.medecinId <= 0) {
            slotsGrid.add(new Label("Veuillez sélectionner un médecin."), 0, 0);
            return slotsGrid;
        }

        List<LocalTime> takenTimes = service.getTakenTimesForDate(selectedDate, this.medecinId);

        // Define available time slots
        String[] horaires = {"09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "12:00", "12:30",
                "14:00", "14:30", "15:00", "15:30"};
        int slotsPerRow = 4;

        for (int i = 0; i < horaires.length; i++) {
            String timeStr = horaires[i];
            Button slotButton = new Button(timeStr);
            slotButton.setPrefWidth(100); // Adjust button width
            slotButton.setPrefHeight(40); // Adjust button height
            slotButton.setUserData(timeStr); // Store the time string in the button

            // Default style (available)
            String baseStyle = "-fx-font-size: 15px; -fx-font-weight: 500; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-width: 1px; -fx-cursor: hand;";
            String availableStyle = baseStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #01579b; -fx-border-color: #bbdefb;";
            String takenStyle = baseStyle + "-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c; -fx-border-color: #ef9a9a; -fx-opacity: 0.7; -fx-cursor: default;";
            String selectedStyle = baseStyle + "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-border-color: #27ae60; -fx-border-width: 1.5px;";


            LocalTime slotTime = LocalTime.parse(timeStr);

            // Disable and style if taken
            if (takenTimes.contains(slotTime)) {
                slotButton.setStyle(takenStyle);
                slotButton.setDisable(true);
                slotButton.setTooltip(new Tooltip("Ce créneau est déjà réservé"));
            } else {
                // Style available slots, check if it's the currently selected one
                if (timeStr.equals(selectedTime)) {
                    slotButton.setStyle(selectedStyle); // Style for the selected button
                } else {
                    slotButton.setStyle(availableStyle); // Style for available, non-selected
                }

                // Add hover effect for available slots
                slotButton.setOnMouseEntered(e -> {
                    if (!timeStr.equals(selectedTime)) { // Don't change style if selected
                        slotButton.setStyle(availableStyle + "-fx-background-color: #bbdefb;"); // Darker blue on hover
                    }
                });
                slotButton.setOnMouseExited(e -> {
                    if (!timeStr.equals(selectedTime)) {
                        slotButton.setStyle(availableStyle); // Revert to normal available style
                    }
                });


                // Handle click action
                slotButton.setOnAction(e -> {
                    String clickedTime = (String) slotButton.getUserData();

                    // Update the selected time state
                    selectedTime = clickedTime;

                    // Update styles of all buttons in the grid
                    slotsGrid.getChildren().forEach(node -> {
                        if (node instanceof Button) {
                            Button b = (Button) node;
                            String buttonTime = (String) b.getUserData();
                            if (!b.isDisabled()) { // Only restyle available buttons
                                if (buttonTime.equals(selectedTime)) {
                                    b.setStyle(selectedStyle); // Apply selected style
                                } else {
                                    b.setStyle(availableStyle); // Apply default available style
                                }
                            }
                        }
                    });
                });
            }

            slotsGrid.add(slotButton, i % slotsPerRow, i / slotsPerRow);
        }

        return slotsGrid;
    }

    private HBox createActionButtonsForDateStep(VBox rootContainer) {
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT); // Align buttons to the right
        actions.setPadding(new Insets(15, 0, 20, 0)); // Padding top/bottom

        // "Suivant" (Next) Button
        Button suivantBtn = new Button("Suivant");
        // Style the button (e.g., primary action color)
        suivantBtn.setStyle("-fx-background-color: #ffd600; -fx-text-fill: #222; -fx-font-size: 16px; -fx-font-weight: bold; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12 30; -fx-cursor: hand;");
        suivantBtn.setOnAction(e -> {
            // Validation before proceeding
            if (medecinId <= 0) {
                showError("Sélection Requise", "Veuillez d'abord sélectionner un médecin.");
                return; // Stay on this step
            }
            if (datePicker.getValue() == null) {
                showError("Sélection Requise", "Veuillez sélectionner une date.");
                return; // Stay on this step
            }
            if (selectedTime.isEmpty()) {
                showError("Sélection Requise", "Veuillez sélectionner une heure.");
                return; // Stay on this step
            }

            // Navigate to the next step (Motif Selection)
            VBox parentContainer = findParentContainer(rootContainer); // Helper to find the main content area
            if (parentContainer != null) {
                parentContainer.getChildren().clear();
                parentContainer.getChildren().add(getContentWithMotifSelector());
            } else { // Fallback
                rootContainer.getChildren().clear();
                rootContainer.getChildren().add(getContentWithMotifSelector());
            }
        });

        actions.getChildren().add(suivantBtn);
        return actions;
    }

    private VBox createPatientInfoInputBox() {
        VBox patientBox = new VBox(15); // Spacing between elements
        patientBox.setAlignment(Pos.CENTER_LEFT);
        patientBox.setPadding(new Insets(10, 0, 10, 0));
        patientBox.setMaxWidth(450); // Control width

        // Patient ID Input
        Label patientIdLabel = new Label("Votre ID Patient:");
        patientIdLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        // patientIdField is initialized in constructor
        patientIdField.setPromptText("Entrez votre numéro d'identification");
        patientIdField.setStyle("-fx-font-size: 15px; -fx-pref-height: 40px;");

        // Phone Number Input
        Label phoneLabel = new Label("Votre Numéro de Téléphone:");
        phoneLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #333;");
        // phoneNumberField is initialized in constructor
        phoneNumberField.setPromptText("Veuillez saisir votre numéro (ex: +216XXXXXXXX)");
        phoneNumberField.setStyle("-fx-font-size: 15px; -fx-pref-height: 40px;");

        patientBox.getChildren().addAll(
                patientIdLabel, patientIdField,
                phoneLabel, phoneNumberField
        );
        return patientBox;
    }

    private VBox createMotifSelectionBox() {
        VBox motifBox = new VBox(15);
        motifBox.setAlignment(Pos.CENTER_LEFT);
        motifBox.setPadding(new Insets(10, 0, 10, 0));
        motifBox.setMaxWidth(450); // Control width
        motifBox.getStyleClass().add("vbox"); // Ajout de la classe CSS pour le lookup

        Label motifTitleLabel = new Label("Motif de la consultation:");
        motifTitleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1a237e;");

        // Checkboxes for common motifs
        CheckBox checkControle = new CheckBox("Contrôle de routine");
        CheckBox checkVaccination = new CheckBox("Vaccination");
        CheckBox checkOrdonnance = new CheckBox("Renouvellement d'ordonnance");
        CheckBox checkUrgence = new CheckBox("Urgence / Symptômes aigus");
        CheckBox checkSpecialiste = new CheckBox("Consultation spécialiste (référence)");
        CheckBox checkExamen = new CheckBox("Examen médical spécifique");
        CheckBox checkSuivi = new CheckBox("Suivi de traitement / maladie chronique");
        CheckBox checkAutre = new CheckBox("Autre motif (préciser ci-dessous)");

        CheckBox[] allChecks = {checkControle, checkVaccination, checkOrdonnance, checkUrgence, checkSpecialiste, checkExamen, checkSuivi, checkAutre};
        VBox checkVBox = new VBox(8); // Vertical layout for checkboxes
        for (CheckBox cb : allChecks) {
            cb.setStyle("-fx-font-size: 14px;");
            checkVBox.getChildren().add(cb);
        }

        // TextArea for "Autre" motif
        TextArea autreDetailsArea = new TextArea();
        autreDetailsArea.setPromptText("Si 'Autre', veuillez préciser votre motif ici...");
        autreDetailsArea.setPrefRowCount(3);
        autreDetailsArea.setWrapText(true);
        autreDetailsArea.setStyle("-fx-font-size: 14px;");
        autreDetailsArea.setDisable(true); // Disabled by default

        // Enable/disable TextArea based on "Autre" checkbox
        checkAutre.selectedProperty().addListener((obs, oldVal, newVal) -> {
            autreDetailsArea.setDisable(!newVal);
            if (newVal) autreDetailsArea.requestFocus();
            else autreDetailsArea.clear();
        });

        // Link the combined motif text to the motifField
        // This logic will run when moving to the next step (in handleGoToConfirmation)

        motifBox.getChildren().addAll(motifTitleLabel, checkVBox, autreDetailsArea);
        // Store references if needed for validation/data retrieval
        motifBox.setUserData(new Object[]{allChecks, autreDetailsArea});

        return motifBox;
    }

    // --- Navigation and Action Handlers ---

    private void handleGoToConfirmation(VBox rootContainer) {
        // 1. Validate Inputs from Motif Step
        if (patientIdField.getText().trim().isEmpty()) {
            showError("Champ Requis", "Veuillez saisir votre ID patient.");
            patientIdField.requestFocus();
            return;
        }
        // Simple numeric check for Patient ID
        try {
            Integer.parseInt(patientIdField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Format Invalide", "L'ID patient doit être un nombre.");
            patientIdField.requestFocus();
            return;
        }
        // Contrôle du numéro de téléphone
        String num = phoneNumberField.getText().trim();
        if (!isValidPhoneNumber(num)) {
            showError("Numéro invalide", "Le numéro doit commencer par +216 et comporter 12 caractères (ex: +216XXXXXXXX).");
            phoneNumberField.requestFocus();
            return;
        }
        // 2. Compile Motif Text
        // Recherche du GridPane (motifs) et du TextArea (autre motif)
        VBox card = (VBox) rootContainer.getChildren().filtered(node -> node instanceof VBox && ((VBox)node).getChildren().size() > 2).get(0);
        GridPane grid = null;
        TextArea autreDetails = null;
        for (javafx.scene.Node n : card.getChildren()) {
            if (n instanceof GridPane) grid = (GridPane) n;
            if (n instanceof TextArea) autreDetails = (TextArea) n;
        }
        if (grid == null) {
            showError("Erreur Interne", "Impossible de trouver la section du motif.");
            return;
        }
        StringBuilder motifs = new StringBuilder();
        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    if (!cb.getText().equals("Autre :")) {
                        if (motifs.length() > 0) motifs.append(", ");
                        motifs.append(cb.getText());
                    }
                }
            }
        }
        // Ajout du texte "Autre" si coché
        for (javafx.scene.Node node : grid.getChildren()) {
            if (node instanceof CheckBox) {
                CheckBox cb = (CheckBox) node;
                if (cb.getText().equals("Autre :") && cb.isSelected() && autreDetails != null && !autreDetails.getText().trim().isEmpty()) {
                    if (motifs.length() > 0) motifs.append(", ");
                    motifs.append("Autre: ").append(autreDetails.getText().trim());
                }
            }
        }
        motifField.setText(motifs.toString());

        // 3. NAVIGATION ROBUSTE : toujours remplacer le contenu du rootContainer
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(getContentWithConfirmationSelector());
    }

    // Contrôle de saisie pour le numéro de téléphone
    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.trim().startsWith("+216") && phone.trim().length() == 12;
    }

    private void handleConfirmAppointment(javafx.event.ActionEvent event) {
        try {
            // 1. Retrieve and Parse Data
            int patientId = Integer.parseInt(patientIdField.getText().trim());
            int currentMedecinId = this.medecinId;
            LocalDate selectedDate = datePicker.getValue();
            LocalTime selectedLocalTime = LocalTime.parse(selectedTime);
            LocalDateTime dateTimeRdv = LocalDateTime.of(selectedDate, selectedLocalTime);
            String motif = motifField.getText();
            String numeroTel = phoneNumberField.getText().trim();

            // Validate phone number
            if (!numeroTel.matches("\\+?[0-9\\s\\-]+") || numeroTel.length() < 8) {
                showError("Numéro Invalide", "Veuillez entrer un numéro de téléphone valide.");
                return;
            }

            // Format phone number
            if (!numeroTel.startsWith("+")) {
                if (numeroTel.length() == 8) {
                    numeroTel = "+216" + numeroTel;
                } else {
                    showError("Format Téléphone", "Numéro de téléphone non reconnu. Veuillez utiliser le format international (ex: +21612345678).");
                    return;
                }
            }

            // 2. Create RendezVous Object
            RendezVous newRdv = new RendezVous();
            newRdv.setDate(dateTimeRdv);
            newRdv.setMotif(motif);
            newRdv.setMedecinId(currentMedecinId);
            newRdv.setPatientId(patientId);
            newRdv.setStatut("Confirmé");

            // 3. Save to Database
            boolean savedSuccessfully = service.ajouterRendezVous(newRdv);
            if (!savedSuccessfully) {
                showError("Erreur d'enregistrement", "Impossible d'enregistrer le rendez-vous.");
                refreshTimeSlots();
                return;
            }

            // 4. Send SMS Confirmation
            try {
                DateTimeFormatter smsFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
                String smsMessage = String.format(
                        " Bonjour, votre rendez-vous avec le Dr %s est confirmé pour le %s. Motif : %s. Merci pour votre confiance. À bientôt ! 👋",
                        medecinNom,
                        dateTimeRdv.format(smsFormatter),
                        motif.length() > 40 ? motif.substring(0, 37) + "..." : motif
                );

                smsService.sendSms(numeroTel, smsMessage);
                showSuccess("Rendez-vous Confirmé", "Votre rendez-vous a été enregistré. Un SMS de confirmation a été envoyé au " + numeroTel + ".", event);
            } catch (Exception smsEx) {
                showSuccess("Rendez-vous Confirmé", "Votre rendez-vous a été enregistré. (Échec d'envoi SMS)", event);
            }

            // 5. Ajout notification dynamique dans le dashboard patient (si accessible)
            try {
                // Recherche d'une instance PatientDashboardView dans la fenêtre principale
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = stage.getScene();
                BorderPane root = (scene != null && scene.getRoot() instanceof BorderPane) ? (BorderPane) scene.getRoot() : null;
                PatientDashboardView dashboard = null;
                if (root != null && root.getCenter() instanceof PatientDashboardView) {
                    dashboard = (PatientDashboardView) root.getCenter();
                }
                // Fallback: si on vient de rediriger, utiliser la nouvelle instance
                if (dashboard == null && Stage.getWindows().stream().anyMatch(w -> w instanceof Stage && w.isShowing())) {
                    Stage mainStage = (Stage) Stage.getWindows().stream().filter(w -> w instanceof Stage && w.isShowing()).findFirst().get();
                    Scene mainScene = mainStage.getScene();
                    if (mainScene != null && mainScene.getRoot() instanceof BorderPane) {
                        BorderPane mainRoot = (BorderPane) mainScene.getRoot();
                        if (mainRoot.getCenter() instanceof PatientDashboardView) {
                            dashboard = (PatientDashboardView) mainRoot.getCenter();
                        }
                    }
                }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dateStr = dateTimeRdv.format(fmt);
                String heureStr = dateTimeRdv.toLocalTime().toString();
                if (dashboard != null) {
                    dashboard.addAppointmentNotification(medecinNom, dateStr, heureStr);
                }
            } catch (Exception ex) {
                // Ignorer si le dashboard n'est pas accessible
            }

            // 6. Redirige vers la page d'accueil (landing)
            try {
                Stage primaryStage = null;
                for (Window w : Stage.getWindows()) {
                    if (w instanceof Stage && w.isShowing()) {
                        primaryStage = (Stage) w;
                        break;
                    }
                }
                if (primaryStage != null) {
                    PatientDashboardView dashboardView = new PatientDashboardView();
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    String dateStr = dateTimeRdv.format(fmt);
                    String heureStr = dateTimeRdv.toLocalTime().toString();
                    dashboardView.addAppointmentNotification(medecinNom, dateStr, heureStr);
                    Scene scene = new Scene(dashboardView, 1200, 800); // ajuste la taille si besoin
                    primaryStage.setScene(scene);
                    primaryStage.show();
                } else {
                    System.err.println("Impossible de retrouver la fenêtre principale pour la redirection.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showError("Erreur de Données", "L'ID du patient doit être un nombre valide.");
        } catch (DateTimeException e) {
            showError("Erreur Date/Heure", "La date ou l'heure sélectionnée est invalide.");
        } catch (Exception e) {
            showError("Erreur", "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void refreshTimeSlots() {
        VBox slotsContainer = (VBox) datePicker.getScene().lookup("#slotsContainer");
        if (slotsContainer != null) {
            slotsContainer.getChildren().clear();
            slotsContainer.getChildren().add(createTimeSlotsGrid());
        }
    }

    private void closeCurrentWindow(javafx.event.ActionEvent event) {
        Node source = (Node) event.getSource();
        if (source != null && source.getScene() != null && source.getScene().getWindow() instanceof Stage) {
            ((Stage) source.getScene().getWindow()).close();
        }
    }

    // --- UI Helper Methods ---

    /**
     * Helper to find the main content container for navigation.
     * Correction : si non trouvé, retourne toujours un VBox (le courant ou un nouveau).
     */
    private VBox findParentContainer(Node currentNode) {
        // Correction : NE PLUS JAMAIS afficher d'erreur, et SI AUCUN parent trouvé, TOUJOURS retourner le VBox courant comme fallback
        Scene scene = currentNode.getScene();
        if (scene != null) {
            Node rootNode = scene.lookup("#mainContentArea");
            if (rootNode instanceof VBox) {
                return (VBox) rootNode;
            }
            else if (scene.getRoot() instanceof BorderPane) {
                Node centerNode = ((BorderPane) scene.getRoot()).getCenter();
                if (centerNode instanceof VBox) {
                    return (VBox) centerNode;
                }
            }
        }
        // Fallback : retourne toujours le VBox courant si possible
        if (currentNode instanceof VBox) {
            return (VBox) currentNode;
        }
        if (currentNode.getParent() instanceof VBox) {
            return (VBox) currentNode.getParent();
        }
        // Fallback ultime : crée un nouveau VBox pour éviter tout blocage
        return new VBox();
    }

    /**
     * Helper method to create a detail row (Label + Value) for the confirmation card.
     */
    private Node createDetailRow(String labelText, String valueText) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Text label = new Text(labelText);
        label.setFont(Font.font("System", FontWeight.BOLD, 15));
        label.setFill(Color.web("#424242")); // Dark gray label
        label.setWrappingWidth(100); // Max width for label

        Text value = new Text(valueText);
        value.setFont(Font.font("System", FontWeight.NORMAL, 15));
        value.setFill(Color.web("#212121")); // Black value
        value.setWrappingWidth(380); // Allow text wrapping

        row.getChildren().addAll(label, value);
        return row;
    }


    /**
     * Shows an error alert dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur - " + title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }

    /**
     * Shows a success/information alert dialog.
     */
    private void showSuccess(String title, String message, javafx.event.ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
        // Après OK, retourne vers l'accueil (dashboard patient)
        try {
            PatientDashboardView dashboardView = new PatientDashboardView();
            Scene scene = new Scene(dashboardView, 1200, 800); // ajuste la taille si besoin
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // The createStepper method is currently identical to createProgressBar
    // If you need different visuals for steps later, modify this.
    private HBox createStepper(int currentStep) {
        return createProgressBar(currentStep);
    }
} // End of AppointmentCrudWindow class