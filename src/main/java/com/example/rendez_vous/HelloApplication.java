package com.example.rendez_vous;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Démarrage de l'UI JavaFX avec la page d'accueil
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Views/landing.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setTitle("MaSanté - Système de Gestion des Rendez Vous");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}