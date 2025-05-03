package Controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class AdminLoginController {
    
    @FXML
    private void handleUserSpace(ActionEvent event) throws IOException {
        // Navigate to user space (your existing front-end)
        Parent root = FXMLLoader.load(getClass().getResource("/Views/tableau-de-bord.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    private void handleAdminSpace(ActionEvent event) throws IOException {
        // Navigate to admin dashboard
        Parent root = FXMLLoader.load(getClass().getResource("/views/Back/Dash.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
