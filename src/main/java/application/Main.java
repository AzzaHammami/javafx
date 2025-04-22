package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.MyDataBase;
import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Tester la connexion
        Connection cnx = MyDataBase.getInstance().getConnection();
        if (cnx != null) {
            System.out.println("Connexion réussie !");
        } else {
            System.out.println("Échec de la connexion.");
        }

        // Vérification avancée : accès à la feuille de style dans le classpath
        java.net.URL cssUrl = getClass().getResource("/styles/theme.css");
        System.out.println("CSS URL: " + cssUrl);
        if (cssUrl == null) {
            throw new RuntimeException("theme.css introuvable dans le classpath !");
        }

        // Charger l'interface Home.fxml
        Parent root = FXMLLoader.load(getClass().getResource("/Home.fxml"));
        primaryStage.setTitle("Gestion des Réclamations");
        Scene scene = new Scene(root);
        // Charger la feuille de style depuis le classpath
        scene.getStylesheets().add(cssUrl.toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
