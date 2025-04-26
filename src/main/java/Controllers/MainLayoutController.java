package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import models.Conversation;
import models.User;
import utils.UserContext;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class MainLayoutController {
    @FXML
    private StackPane contentPane;
    @FXML
    private Label adminNameLabel;
    @FXML
    private Button floatingMessengerButton;
    private Label messengerBadgeLabel;
    private Popup conversationListPopup;
    private final List<Popup> openMessengerPopups = new ArrayList<>();
    private int unreadGlobalCount = 0;
    private PauseTransition badgePollingTimer;
    private User currentUser;

    // Variable statique pour test : stocke l'utilisateur globalement
    public static User GLOBAL_USER = null;

    // Liste des popups ouverts (pour gérer plusieurs discussions)
    private final List<Popup> openMessengerPopupsList = new ArrayList<>();

    @FXML
    public void initialize() {
        setupFloatingMessengerBadge();
        floatingMessengerButton.setOnAction(e -> showFloatingMessengerChooser());
        startBadgePolling();
        // showDashboard(null); // Désactivé pour éviter le chargement avant passage du user
    }

    // Change l'accès à public pour permettre l'appel depuis LoginController
    public void showDashboard(ActionEvent event) {
        loadView("/views/back/ReclamationView.fxml");
    }

    @FXML
    private void showReclamations(ActionEvent event) {
        loadView("/views/back/ReclamationView.fxml");
    }

    @FXML
    private void showReponses(ActionEvent event) {
        loadView("/views/back/ReponseView.fxml");
    }

    @FXML
    private void showMessagerie(ActionEvent event) {
        System.out.println("[MainLayoutController] showMessagerie: currentUser = " + (getCurrentUser() != null ? getCurrentUser().getName() : "null"));
        loadView("/views/back/MessengerView.fxml");
    }

    @FXML
    private void showStats(ActionEvent event) {
        loadView("/views/back/StatistiquesView.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        // À adapter selon ta logique de déconnexion
        System.exit(0);
    }

    public void setCurrentUser(User user) {
        System.out.println("[MainLayoutController] setCurrentUser called on instance: " + this);
        this.currentUser = user;
        utils.UserContext.getInstance().setCurrentUser(user);
        System.out.println("[MainLayoutController] setCurrentUser: " + (user != null ? user.getName() : "null"));
        if (adminNameLabel != null && user != null) {
            adminNameLabel.setText(user.getName() + " (" + user.getEmail() + ")");
        }
    }

    public User getCurrentUser() {
        return currentUser != null ? currentUser : GLOBAL_USER;
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
            System.out.println("[MainLayoutController] loadView: currentUser = " + (getCurrentUser() != null ? getCurrentUser().getName() : "null"));
            if (fxmlPath.endsWith("MessengerView.fxml")) {
                MessengerController messengerController = loader.getController();
                System.out.println("[MainLayoutController] loadView: getCurrentUser() just before setCurrentUser = " + (getCurrentUser() != null ? getCurrentUser().getName() : "null"));
                messengerController.setCurrentUser(getCurrentUser());
            }
        } catch (IOException e) {
            System.err.println("[MainLayoutController] ERROR loading " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception ex) {
            System.err.println("[MainLayoutController] UNEXPECTED ERROR loading " + fxmlPath + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void setupFloatingMessengerBadge() {
        messengerBadgeLabel = new Label();
        messengerBadgeLabel.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 1 6 1 6; -fx-background-radius: 10; -fx-border-radius: 10;");
        messengerBadgeLabel.setVisible(false);
        // Ajout correct du badge dans la VBox parent du bouton
        VBox vbox = (VBox) floatingMessengerButton.getParent();
        StackPane badgeStack = new StackPane(floatingMessengerButton, messengerBadgeLabel);
        badgeStack.setMaxSize(54, 54);
        badgeStack.setMinSize(54, 54);
        badgeStack.setPrefSize(54, 54);
        vbox.getChildren().remove(floatingMessengerButton);
        vbox.getChildren().add(badgeStack);
    }

    private void updateMessengerBadge(int unreadCount) {
        unreadGlobalCount = unreadCount;
        if (unreadCount > 0) {
            messengerBadgeLabel.setText(String.valueOf(unreadCount));
            messengerBadgeLabel.setVisible(true);
        } else {
            messengerBadgeLabel.setVisible(false);
        }
    }

    private void startBadgePolling() {
        badgePollingTimer = new PauseTransition(Duration.seconds(2));
        badgePollingTimer.setOnFinished(e -> {
            int unread = getTotalUnreadMessages();
            updateMessengerBadge(unread);
            badgePollingTimer.playFromStart();
        });
        badgePollingTimer.play();
    }

    private int getTotalUnreadMessages() {
        try {
            // On parcourt toutes les conversations et on compte les messages non lus pour l'utilisateur courant
            services.ConversationService conversationService = new services.ConversationService();
            services.MessageService messageService = new services.MessageService();
            List<models.Conversation> conversations = conversationService.getUserConversations(getCurrentUser().getId());
            int total = 0;
            for (models.Conversation conv : conversations) {
                List<models.Message> messages = messageService.getMessagesByConversation(conv.getId());
                for (models.Message m : messages) {
                    if (!m.isRead() && m.getSender().getId() != getCurrentUser().getId() && m.getReceiverId() == getCurrentUser().getId()) {
                        total++;
                    }
                }
            }
            return total;
        } catch (Exception ex) {
            return 0;
        }
    }

    private void showFloatingMessengerChooser() {
        if (conversationListPopup != null && conversationListPopup.isShowing()) {
            conversationListPopup.hide();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/CompactConversationList.fxml"));
            Pane compactListRoot = loader.load();
            Controllers.CompactConversationListController ctrl = loader.getController();
            ctrl.setCurrentUser(getCurrentUser());
            ctrl.setOnConversationSelected(conversation -> {
                openMiniMessenger(conversation);
                if (conversationListPopup != null) conversationListPopup.hide();
            });
            conversationListPopup = new Popup();
            conversationListPopup.getContent().add(compactListRoot);
            conversationListPopup.setAutoHide(true);
            // Positionne le popup au-dessus du bouton Messenger
            double btnX = floatingMessengerButton.localToScene(0, 0).getX() + floatingMessengerButton.getScene().getWindow().getX() + floatingMessengerButton.getWidth() - 280;
            double btnY = floatingMessengerButton.localToScene(0, 0).getY() + floatingMessengerButton.getScene().getWindow().getY() - 350;
            conversationListPopup.setX(btnX);
            conversationListPopup.setY(btnY > 0 ? btnY : 50);
            conversationListPopup.show(floatingMessengerButton.getScene().getWindow());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Ouvre un mini chat flottant pour une conversation donnée
    public void openMiniMessenger(Conversation conversation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/back/MiniMessengerView.fxml"));
            Pane miniMessengerPane = loader.load();
            Controllers.MiniMessengerController miniMessengerController = loader.getController();
            miniMessengerController.setConversation(conversation, getCurrentUser(), () -> closeMiniMessenger(miniMessengerPane));
            Popup popup = new Popup();
            popup.getContent().add(miniMessengerPane);
            popup.setAutoHide(false);
            // Positionne le popup (ex : en bas à droite, décalé selon le nombre déjà ouverts)
            double baseRight = 40;
            double baseBottom = 40;
            int offset = openMessengerPopups.size();
            popup.setX(contentPane.getScene().getWindow().getX() + contentPane.getScene().getWindow().getWidth() - 380 - offset * 370);
            popup.setY(contentPane.getScene().getWindow().getY() + contentPane.getScene().getWindow().getHeight() - 520);
            popup.show(contentPane.getScene().getWindow());
            openMessengerPopups.add(popup);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeMiniMessenger(Pane miniMessengerPane) {
        openMessengerPopups.removeIf(popup -> {
            if (popup.getContent().contains(miniMessengerPane)) {
                popup.hide();
                return true;
            }
            return false;
        });
    }
}
