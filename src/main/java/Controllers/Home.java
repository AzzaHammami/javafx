package Controllers;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;

public class Home extends Application {

    @FXML
    private Button btnUser;
    
    @FXML
    private Button btnAdmin;

    @FXML
    private void goToUserSpace(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginView.fxml"));
            Parent root = loader.load();
            Controllers.LoginController loginController = loader.getController();
            loginController.setRoleMode("user");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Connexion Utilisateur");
            stage.setScene(new Scene(root));
            stage.setMinWidth(500);
            stage.setMinHeight(350);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToAdminSpace(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginView.fxml"));
            Parent root = loader.load();
            Controllers.LoginController loginController = loader.getController();
            loginController.setRoleMode("admin");
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Connexion Admin");
            stage.setScene(new Scene(root));
            stage.setMinWidth(500);
            stage.setMinHeight(350);
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser Firebase explicitement (au démarrage global)
        services.FirebaseService.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/theme.css").toExternalForm());
        primaryStage.setTitle("Accueil - Gestion des Réclamations");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
