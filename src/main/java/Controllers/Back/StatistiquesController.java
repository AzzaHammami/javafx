package Controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import services.ReclamationService;
import services.ReponseService;
import services.UserService;
import models.Reclamation;
import models.Reponse;
import models.User;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StatistiquesController implements Initializable {

    @FXML private Label totalValue;
    @FXML private Label enAttenteValue;
    @FXML private Label enCoursValue;
    @FXML private Label traiteesValue;


    @FXML private RadioButton statRadio;
    @FXML private RadioButton userRadio;
    @FXML private ToggleGroup statToggleGroup;
    @FXML private Label pieChartTitle;
    @FXML private PieChart statutPieChart;



    @FXML private TableView<StatRow> statsTableView;
    @FXML private TableColumn<StatRow, String> periodeColumn;
    @FXML private TableColumn<StatRow, Integer> totalColumn;
    @FXML private TableColumn<StatRow, Integer> attenteColumn;
    @FXML private TableColumn<StatRow, Integer> coursColumn;
    @FXML private TableColumn<StatRow, Integer> traiteesColumn;

    private ReclamationService reclamationService;
    private ReponseService reponseService;
    private UserService userService;

    // File path to CSS stylesheet
    private static final String CSS_PATH = "/styles/theme.css";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        reponseService = new ReponseService();
        userService = new UserService();
        statToggleGroup = new ToggleGroup();
statRadio.setToggleGroup(statToggleGroup);
userRadio.setToggleGroup(statToggleGroup);
statRadio.setSelected(true);
statToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updatePieChart());
updatePieChart();
        loadStatistics();

        setupTable();
    }

    @FXML
private void updatePieChart() {
    // Log des utilisateurs connus
    List<User> users = userService.getAllUsers();
    System.out.println("[DEBUG] Utilisateurs connus:");
    for (User u : users) {
        System.out.println("  id=" + u.getId() + " | name=" + u.getName());
    }
    // Log des réclamations
    List<Reclamation> reclamations = reclamationService.getAll();
    System.out.println("[DEBUG] Réclamations:");
    for (Reclamation r : reclamations) {
        System.out.println("  id=" + r.getId() + " | userId=" + r.getUserId() + " | statut=" + r.getStatut());
    }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        // Log du mode sélectionné
System.out.println("[DEBUG] MODE: " + (statRadio.isSelected() ? "STATUT" : "UTILISATEUR"));
if (statRadio.isSelected()) {
            // Par statut
            Map<String, Integer> countByStatut = new HashMap<>();
            for (Reclamation r : reclamations) {
                countByStatut.put(r.getStatut(), countByStatut.getOrDefault(r.getStatut(), 0) + 1);
            }
            for (Map.Entry<String, Integer> entry : countByStatut.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            pieChartTitle.setText("Répartition des Statuts");
        } else {
            // Par utilisateur
            Map<String, Integer> countByUser = new HashMap<>();
            for (Reclamation r : reclamations) {
                String userName = userService.getAllUsers().stream()
                        .filter(u -> u.getId() == r.getUserId())
                        .map(models.User::getName)
                        .findFirst()
                        .orElse("?");
                System.out.println("Reclamation id=" + r.getId() + " | userId=" + r.getUserId() + " | userName=" + userName);
                countByUser.put(userName, countByUser.getOrDefault(userName, 0) + 1);
            }
            for (Map.Entry<String, Integer> entry : countByUser.entrySet()) {
                pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            pieChartTitle.setText("Répartition par Utilisateur");
        }
        // Log des données du PieChart
        System.out.println("[DEBUG] Données PieChart:");
        for (PieChart.Data d : pieData) {
            System.out.println("  label=" + d.getName() + " | value=" + d.getPieValue());
        }

        statutPieChart.setData(pieData);
        // Appliquer les couleurs APRÈS l'affichage du PieChart (sinon getNode() == null)
        statutPieChart.applyCss(); // force JavaFX à créer les nodes
        // Log du mode sélectionné
System.out.println("[DEBUG] MODE: " + (statRadio.isSelected() ? "STATUT" : "UTILISATEUR"));
if (statRadio.isSelected()) {
            // Par statut : couleurs fixes
            for (PieChart.Data d : pieData) {
                if (d.getName().toLowerCase().contains("attente")) {
                    d.getNode().setStyle("-fx-pie-color: #ffa726;"); // orange
                } else if (d.getName().toLowerCase().contains("cours")) {
                    d.getNode().setStyle("-fx-pie-color: #42a5f5;"); // bleu
                } else if (d.getName().toLowerCase().contains("trait")) {
                    d.getNode().setStyle("-fx-pie-color: #66bb6a;"); // vert
                } else {
                    d.getNode().setStyle("");
                }
            }
        } else {
            // Par utilisateur : couleurs dynamiques
            String[] colors = {"#42a5f5", "#e57373", "#66bb6a", "#ffd54f", "#ab47bc", "#ffb300", "#29b6f6"};
            int idx = 0;
            for (PieChart.Data d : pieData) {
                d.getNode().setStyle("-fx-pie-color: " + colors[idx % colors.length] + ";");
                idx++;
            }
        }
    }

    private void loadStatistics() {
        List<Reclamation> reclamations = reclamationService.getAll();

        int total = reclamations.size();
        int enAttente = 0;
        int enCours = 0;
        int traitees = 0;

        for (Reclamation r : reclamations) {
            switch (r.getStatut().toLowerCase()) {
                case "en attente": enAttente++; break;
                case "en cours": enCours++; break;
                case "traitée": traitees++; break;
            }
        }

        totalValue.setText(String.valueOf(total));
        enAttenteValue.setText(String.valueOf(enAttente));
        enCoursValue.setText(String.valueOf(enCours));
        traiteesValue.setText(String.valueOf(traitees));
    }

    private void setupTable() {
        periodeColumn.setCellValueFactory(data -> data.getValue().periodeProperty());
        totalColumn.setCellValueFactory(data -> data.getValue().totalProperty().asObject());
        attenteColumn.setCellValueFactory(data -> data.getValue().attenteProperty().asObject());
        coursColumn.setCellValueFactory(data -> data.getValue().coursProperty().asObject());
        traiteesColumn.setCellValueFactory(data -> data.getValue().traiteesProperty().asObject());

        // Exemple de données (à remplacer par des données réelles)
        ObservableList<StatRow> data = FXCollections.observableArrayList(
                new StatRow("Janvier 2024", 15, 5, 7, 3),
                new StatRow("Février 2024", 20, 8, 6, 6),
                new StatRow("Mars 2024", 18, 4, 8, 6)
        );

        statsTableView.setItems(data);
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        loadView(event, "/views/back/ReclamationView.fxml");
    }

    @FXML
    private void handleReclamations(ActionEvent event) {
        loadView(event, "/views/back/ReclamationView.fxml");
    }

    @FXML
    private void handleReponses(ActionEvent event) {
        loadView(event, "/views/back/ReponseView.fxml");
    }

    @FXML
    public void handlMessagerie(ActionEvent event) {
        loadView(event, "/views/back/MessengerView.fxml");
    }

    @FXML
    private void handleRetourHome(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            // Ajout explicite du CSS pour Home
            URL cssUrl = getClass().getResource(CSS_PATH);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("AVERTISSEMENT: Stylesheet CSS non trouvé: " + CSS_PATH);
            }

            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la vue Home: " + e.getMessage());
        }
    }

    private void loadView(ActionEvent event, String fxmlPath) {
        try {
            // Charger la vue FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root);

            // Ajouter EXPLICITEMENT le CSS à la scène
            URL cssUrl = getClass().getResource(CSS_PATH);
            if (cssUrl != null) {
                // Vérifier si le stylesheet existe déjà pour éviter les doublons
                if (!scene.getStylesheets().contains(cssUrl.toExternalForm())) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } else {
                System.err.println("AVERTISSEMENT: Stylesheet CSS non trouvé: " + CSS_PATH);
            }

            // Obtenir et mettre à jour la scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.sizeToScene();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement de la vue: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Classe interne pour les données du tableau
    public static class StatRow {
        private final javafx.beans.property.StringProperty periode;
        private final javafx.beans.property.IntegerProperty total;
        private final javafx.beans.property.IntegerProperty attente;
        private final javafx.beans.property.IntegerProperty cours;
        private final javafx.beans.property.IntegerProperty traitees;

        public StatRow(String periode, int total, int attente, int cours, int traitees) {
            this.periode = new javafx.beans.property.SimpleStringProperty(periode);
            this.total = new javafx.beans.property.SimpleIntegerProperty(total);
            this.attente = new javafx.beans.property.SimpleIntegerProperty(attente);
            this.cours = new javafx.beans.property.SimpleIntegerProperty(cours);
            this.traitees = new javafx.beans.property.SimpleIntegerProperty(traitees);
        }

        public javafx.beans.property.StringProperty periodeProperty() { return periode; }
        public javafx.beans.property.IntegerProperty totalProperty() { return total; }
        public javafx.beans.property.IntegerProperty attenteProperty() { return attente; }
        public javafx.beans.property.IntegerProperty coursProperty() { return cours; }
        public javafx.beans.property.IntegerProperty traiteesProperty() { return traitees; }
    }
}