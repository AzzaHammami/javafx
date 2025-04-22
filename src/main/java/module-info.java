module GestionReclamation {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;
    requires org.json;
    requires jbcrypt;

    opens Controllers to javafx.fxml;
    opens Controllers.Front to javafx.fxml;
    opens Controllers.Back to javafx.fxml;
    opens models to javafx.base, javafx.controls, javafx.fxml;
    opens services to javafx.base, javafx.controls, javafx.fxml;
    opens application to javafx.fxml;
    
    exports Controllers;
    exports Controllers.Front;
    exports Controllers.Back;
    exports models;
    exports services;
    exports interfaces;
    exports application;
}
