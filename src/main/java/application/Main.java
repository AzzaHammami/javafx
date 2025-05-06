package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import utils.MyDataBase;
import java.sql.Connection;
import services.FirebaseService;
import services.FirebaseListener;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Demander dynamiquement l'ID utilisateur au lancement
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sélection de l'utilisateur");
        dialog.setHeaderText("Tester les notifications pour un utilisateur spécifique");
        dialog.setContentText("Entrez l'ID utilisateur :");
        String userId = dialog.showAndWait().orElse("");
        if (userId == null || userId.isBlank()) {
            System.out.println("Aucun ID utilisateur saisi. Arrêt de l'application.");
            System.exit(0);
        }
        // Écouter dynamiquement les notifications pour l'utilisateur choisi
        FirebaseListener.ecouterNotifications(userId);
<<<<<<< HEAD
        utils.WindowsNotification.show("Test Notification", "Ceci est un test !");
=======
>>>>>>> 0437d716b496ba8972d63fba270ee7c757826b2b

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
        primaryStage.sizeToScene();
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            // Force le chargement de la classe FirebaseService pour garantir l'initialisation statique
            Class.forName("services.FirebaseService");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement de FirebaseService: " + e.getMessage());
        }
        launch(args);
    }
}
