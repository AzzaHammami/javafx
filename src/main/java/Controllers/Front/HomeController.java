package Controllers.Front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

public class HomeController implements Initializable {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private Button btnLireSheets;
    @FXML
    private TableView<SheetRow> tableSheets;
    @FXML
    private TableColumn<SheetRow, String> colA;
    @FXML
    private TableColumn<SheetRow, String> colB;
    @FXML
    private TableColumn<SheetRow, String> colC;
    @FXML
    private TableColumn<SheetRow, String> colD;
    @FXML
    private TableColumn<SheetRow, String> colE;
    @FXML
    private TableColumn<SheetRow, String> colF;

    public HomeController() {
        System.out.println("[DEBUG] Constructeur HomeController appelé !");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[DEBUG] HomeController initialisé !");
        // On ne crée plus dynamiquement les nodes ici, tout est dans le FXML
        // Mapping Google Sheets columns (tableSheets)
        if (colA != null) colA.setCellValueFactory(new PropertyValueFactory<>("a"));
        if (colB != null) colB.setCellValueFactory(new PropertyValueFactory<>("b"));
        if (colC != null) colC.setCellValueFactory(new PropertyValueFactory<>("c"));
        if (colD != null) colD.setCellValueFactory(new PropertyValueFactory<>("d"));
        if (colE != null) colE.setCellValueFactory(new PropertyValueFactory<>("e"));
        if (colF != null) colF.setCellValueFactory(new PropertyValueFactory<>("f"));
    }

    @FXML
    private void setupUI() {
        // Cette méthode peut rester vide ou être supprimée si tu utilises le FXML pour toute la structure
    }

    @FXML
    private void handleReclamationButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/ReclamationView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboardButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/ReclamationView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onLireSheetsClicked() {
        try {
            System.out.println("[DEBUG] Bouton Lire Google Sheets cliqué");
            final com.google.api.client.http.javanet.NetHttpTransport HTTP_TRANSPORT = com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport();
            com.google.api.services.sheets.v4.Sheets sheetsService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    HTTP_TRANSPORT,
                    com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                    services.GoogleApi.getCredentials(HTTP_TRANSPORT))
                    .setApplicationName("GestionReclamationApp")
                    .build();

            String spreadsheetId = "1TrWsvtqrDd2yja0fTHDxcDD-pjZ6QfIdHmkZcl18Dm0"; // Mets ici ton vrai ID
            String range = "messages_sheet!A1:F";
            com.google.api.services.sheets.v4.model.ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, range).execute();
            java.util.List<java.util.List<Object>> values = response.getValues();
            System.out.println("[DEBUG] Valeurs récupérées : " + values);
            ObservableList<SheetRow> data = FXCollections.observableArrayList();
            if (values != null && !values.isEmpty()) {
                for (java.util.List<Object> row : values) {
                    String a = row.size() > 0 ? row.get(0).toString() : "";
                    String b = row.size() > 1 ? row.get(1).toString() : "";
                    String c = row.size() > 2 ? row.get(2).toString() : "";
                    String d = row.size() > 3 ? row.get(3).toString() : "";
                    String e = row.size() > 4 ? row.get(4).toString() : "";
                    String f = row.size() > 5 ? row.get(5).toString() : "";
                    data.add(new SheetRow(a, b, c, d, e, f));
                }
            } else {
                System.out.println("[DEBUG] Aucune donnée trouvée dans Google Sheets.");
            }
            tableSheets.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[ERROR] Erreur lors de la lecture Google Sheets : " + e.getMessage());
        }
    }

    public static class SheetRow {
        private String a, b, c, d, e, f;
        public SheetRow(String a, String b, String c, String d, String e, String f) {
            this.a = a; this.b = b; this.c = c; this.d = d; this.e = e; this.f = f;
        }
        public String getA() { return a; }
        public String getB() { return b; }
        public String getC() { return c; }
        public String getD() { return d; }
        public String getE() { return e; }
        public String getF() { return f; }
    }
}
