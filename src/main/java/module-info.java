module com.example.rendez_vous {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;
    requires jdk.httpserver;

    opens com.example.rendez_vous.controllers to javafx.fxml;
    opens com.example.rendez_vous.controllers.Back to javafx.fxml;
    opens com.example.rendez_vous.services to javafx.fxml;
    opens com.example.rendez_vous.models to com.google.gson;
    exports com.example.rendez_vous;
    exports com.example.rendez_vous.models;
    exports com.example.rendez_vous.controllers.api;
    exports com.example.rendez_vous.controllers.Back;
}