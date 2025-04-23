package com.example.rendez_vous;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.rendez_vous.controllers.api.DisponibiliteRestController;

import java.io.IOException;

public class HelloApplication extends Application {
    private DisponibiliteRestController restServer;

    @Override
    public void start(Stage stage) throws IOException {
        // Start REST API server
        restServer = new DisponibiliteRestController();
        restServer.start();

        // Start JavaFX UI with landing page
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Views/landing.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        stage.setTitle("MaSanté - Système de Gestion des Rendez Vous");
        stage.setScene(scene);
        stage.show();

        // Stop REST server when JavaFX app closes
        stage.setOnCloseRequest(event -> {
            if (restServer != null) {
                restServer.stop();
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}