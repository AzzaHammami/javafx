package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Conversation;
import models.Message;
import models.User;
import services.ConversationService;
import services.MessageService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class CompactConversationListController implements Initializable {
    @FXML private ListView<Conversation> conversationListView;
    private ConversationService conversationService = new ConversationService();
    private MessageService messageService = new MessageService();
    private User currentUser;
    private Consumer<Conversation> onConversationSelected;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadConversations();
    }

    public void setOnConversationSelected(Consumer<Conversation> consumer) {
        this.onConversationSelected = consumer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        conversationListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation conv, boolean empty) {
                super.updateItem(conv, empty);
                if (empty || conv == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(8);
                    Label title = new Label(getTitle(conv));
                    title.setStyle("-fx-font-size: 14px; -fx-font-weight: normal;");
                    row.getChildren().add(title);
                    int unread = getUnreadCountForConversation(conv.getId(), currentUser.getId());
                    if (unread > 0) {
                        Circle badge = new Circle(7, Color.RED);
                        Label badgeLabel = new Label(String.valueOf(unread));
                        badgeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
                        javafx.scene.layout.StackPane badgeStack = new javafx.scene.layout.StackPane(badge, badgeLabel);
                        row.getChildren().add(badgeStack);
                    }
                    setGraphic(row);
                }
            }
        });

        conversationListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Conversation selected = conversationListView.getSelectionModel().getSelectedItem();
                if (selected != null && onConversationSelected != null) {
                    onConversationSelected.accept(selected);
                }
            }
        });
    }

    private void loadConversations() {
        if (currentUser == null) return;
        List<Conversation> conversations = conversationService.getUserConversations(currentUser.getId());
        ObservableList<Conversation> obsList = FXCollections.observableArrayList(conversations);
        conversationListView.setItems(obsList);
    }

    private String getTitle(Conversation conv) {
        if (conv.isGroup()) return conv.getTitle();
        for (User participant : conv.getParticipants()) {
            if (participant.getId() != currentUser.getId()) return participant.getName();
        }
        return "Conversation";
    }

    private int getUnreadCountForConversation(int conversationId, int userId) {
        // Requête manuelle sur les messages de la conversation
        List<Message> messages = messageService.getMessagesByConversation(conversationId);
        int count = 0;
        for (Message m : messages) {
            if (!m.isRead() && m.getSender().getId() != userId && m.getReceiverId() == userId) {
                count++;
            }
        }
        return count;
    }
}
