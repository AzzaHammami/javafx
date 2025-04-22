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
import models.Reclamation;

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

    @FXML private Pane pieChartContainer;

    @FXML private TableView<StatRow> statsTableView;
    @FXML private TableColumn<StatRow, String> periodeColumn;
    @FXML private TableColumn<StatRow, Integer> totalColumn;
    @FXML private TableColumn<StatRow, Integer> attenteColumn;
    @FXML private TableColumn<StatRow, Integer> coursColumn;
    @FXML private TableColumn<StatRow, Integer> traiteesColumn;

    private ReclamationService reclamationService;

    // File path to CSS stylesheet
    private static final String CSS_PATH = "/styles/theme.css";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reclamationService = new ReclamationService();
        loadStatistics();
        setupCharts();
        setupTable();
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

    private void setupCharts() {
        // Récupérer les valeurs
        int total = Integer.parseInt(totalValue.getText());
        int enAttente = Integer.parseInt(enAttenteValue.getText());
        int enCours = Integer.parseInt(enCoursValue.getText());
        int traitees = Integer.parseInt(traiteesValue.getText());

        // Calculer les pourcentages
        double pctEnAttente = total > 0 ? (enAttente * 100.0) / total : 0;
        double pctEnCours = total > 0 ? (enCours * 100.0) / total : 0;
        double pctTraitees = total > 0 ? (traitees * 100.0) / total : 0;

        // Créer le graphique circulaire
        PieChart pieChart = new PieChart();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data(String.format("En Attente (%.1f%%)", pctEnAttente), enAttente),
                new PieChart.Data(String.format("En Cours (%.1f%%)", pctEnCours), enCours),
                new PieChart.Data(String.format("Traitées (%.1f%%)", pctTraitees), traitees)
        );

        pieChart.setData(pieChartData);
        pieChart.setTitle("Répartition des Statuts");

        // Personnaliser l'apparence
        pieChart.setLabelsVisible(true);
        pieChart.setLabelLineLength(20);
        pieChart.setLegendVisible(false);
        pieChart.setStartAngle(90);

        // Ajouter des couleurs personnalisées
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #ffa726;"); // Orange pour En Attente
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #42a5f5;"); // Bleu pour En Cours
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #66bb6a;"); // Vert pour Traitées

        // Ajouter des tooltips avec les valeurs exactes
        pieChartData.forEach(data -> {
            Tooltip tooltip = new Tooltip(String.format(
                    "%s\nNombre: %d\nPourcentage: %.1f%%",
                    data.getName().split(" \\(")[0],
                    (int) data.getPieValue(),
                    data.getPieValue() * 100 / total
            ));
            Tooltip.install(data.getNode(), tooltip);
        });

        // Ajouter le graphique au conteneur
        pieChartContainer.getChildren().clear();
        pieChartContainer.getChildren().add(pieChart);
        pieChart.prefWidthProperty().bind(pieChartContainer.widthProperty());
        pieChart.prefHeightProperty().bind(pieChartContainer.heightProperty());
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