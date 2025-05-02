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
        System.out.println("[DEBUG] currentUser in CompactConversationListController: " + (currentUser != null ? currentUser.getId() : "null"));
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

        conversationListView.setOnMouseClicked(event -> {
            if (event.getClickCount() >= 1) {
                Conversation selected = conversationListView.getSelectionModel().getSelectedItem();
                if (selected != null && onConversationSelected != null) {
                    System.out.println("[DEBUG] Conversation cliquée: " + selected.getId());
                    onConversationSelected.accept(selected);
                }
            }
        });
    }

    private void loadConversations() {
        if (currentUser == null) return;
        List<Conversation> conversations = conversationService.getUserConversations(currentUser.getId());
        // --- FIXED ORDER: sort by last_message_at DESC and then by id ASC for stability ---
        conversations.sort((c1, c2) -> {
            int cmp = 0;
            if (c1.getLastMessageAt() != null && c2.getLastMessageAt() != null) {
                cmp = c2.getLastMessageAt().compareTo(c1.getLastMessageAt());
            } else if (c1.getLastMessageAt() != null) {
                return -1;
            } else if (c2.getLastMessageAt() != null) {
                return 1;
            }
            if (cmp == 0) {
                cmp = Integer.compare(c1.getId(), c2.getId());
            }
            return cmp;
        });
        System.out.println("[DEBUG] Conversations trouvées: " + conversations.size());
        ObservableList<Conversation> obsList = FXCollections.observableArrayList(conversations);
        conversationListView.setItems(obsList);
        // Affiche un message si la liste est vide
        if (obsList.isEmpty()) {
            conversationListView.setPlaceholder(new Label("Aucune conversation trouvée"));
        }
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
