package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import models.User;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // TODO: Propager l'utilisateur aux sous-contrôleurs si besoin
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation si besoin

    }

    public User getCurrentUser() {
        return currentUser;
    }
}
