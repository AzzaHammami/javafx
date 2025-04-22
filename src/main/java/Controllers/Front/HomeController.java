package Controllers.Front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

public class HomeController implements Initializable {
    
    @FXML
    private BorderPane mainContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
    }
    
    private void setupUI() {
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f8f9fa);");
        
        // Create center content
        VBox centerContent = new VBox(30);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.setPadding(new Insets(50));
        
        // Welcome text
        Text welcomeText = new Text("Bienvenue dans MaSanté");
        welcomeText.setFont(Font.font("System", FontWeight.BOLD, 36));
        welcomeText.setStyle("-fx-fill: #2d3748;");
        
        Text subText = new Text("Gérez vos réclamations et suivez leur traitement");
        subText.setFont(Font.font("System", 18));
        subText.setStyle("-fx-fill: #718096;");
        
        // Buttons
        Button reclamationButton = new Button("Gérer mes Réclamations");
        reclamationButton.setStyle(
            "-fx-background-color: #5e72e4;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 15 30;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        reclamationButton.setOnAction(this::handleReclamationButton);
        
        Button dashboardButton = new Button("Tableau de Bord");
        dashboardButton.setStyle(
            "-fx-background-color: #2ecc71;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-padding: 15 30;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        dashboardButton.setOnAction(this::handleDashboardButton);
        
        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(reclamationButton, dashboardButton);
        
        centerContent.getChildren().addAll(welcomeText, subText, buttonContainer);
        mainContainer.setCenter(centerContent);
    }
    
    @FXML
    private void handleReclamationButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/front/ReclamationView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
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
            stage.setWidth(1200);
            stage.setHeight(800);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
