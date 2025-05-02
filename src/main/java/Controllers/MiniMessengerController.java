package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import models.Conversation;
import models.Message;
import models.User;
import services.MessageService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class MiniMessengerController {
    @FXML private VBox miniMessengerRoot;
    @FXML private Label conversationTitleLabel;
    @FXML private Button closeButton;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;

    private MessageService messageService = new MessageService();
    private Conversation conversation;
    private User currentUser;
    private ScheduledExecutorService pollingExecutor;
    private List<Message> lastMessagesSnapshot = new ArrayList<>();
    private Runnable onClose;

    public void setConversation(Conversation conversation, User currentUser, Runnable onClose) {
        this.conversation = conversation;
        this.currentUser = currentUser;
        this.onClose = onClose;
        // Affiche le nom du destinataire dans une conversation privée
        if (!conversation.isGroup() && conversation.getParticipants() != null) {
            for (User participant : conversation.getParticipants()) {
                if (participant.getId() != currentUser.getId()) {
                    conversationTitleLabel.setText(participant.getName());
                    break;
                }
            }
        } else {
            conversationTitleLabel.setText(conversation.getTitle());
        }
        setupPolling();
        loadMessages();
    }

    @FXML
    public void initialize() {
        sendButton.setOnAction(this::handleSendMessage);
        closeButton.setOnAction(e -> closeWindow());
        messageInput.setOnKeyPressed(this::handleKeyPressed);
    }

    private void handleSendMessage(ActionEvent event) {
        sendCurrentMessage();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendCurrentMessage();
        }
    }

    private void sendCurrentMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || conversation == null || currentUser == null) return;
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSender(currentUser);
        message.setContent(content);
        message.setSentAt(java.time.LocalDateTime.now());
        // --- DEBUG LOGGING ---
        System.out.println("[DEBUG] Sending message:");
        System.out.println("  conversationId=" + message.getConversationId());
        System.out.println("  senderId=" + currentUser.getId());
        // Set receiverId for direct conversations
        if (!conversation.isGroup() && conversation.getParticipants() != null) {
            for (User participant : conversation.getParticipants()) {
                if (participant.getId() != currentUser.getId()) {
                    message.setReceiverId(participant.getId());
                    System.out.println("  receiverId=" + participant.getId());
                    break;
                }
            }
        }
        try {
            messageService.sendMessage(message);
            messageInput.clear();
            loadMessages();
        } catch (Exception e) {
            showError("Erreur lors de l'envoi du message : " + e.getMessage());
        }
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        if (conversation == null) return;
        List<Message> messages = messageService.getMessagesByConversation(conversation.getId());
        lastMessagesSnapshot = new ArrayList<>(messages);
        for (Message message : messages) {
            addMessageBubble(message);
        }
        Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
    }

    private void addMessageBubble(Message message) {
        HBox messageBox = new HBox(10);
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-padding: 8; -fx-background-radius: 10; -fx-font-size: 13px;" +
            (message.getSender().getId() == currentUser.getId()
                ? "-fx-background-color: #0084ff; -fx-text-fill: white;"
                : "-fx-background-color: #f1f0f0; -fx-text-fill: #222;"));
        messageBox.getChildren().add(contentLabel);
        messageBox.setStyle("-fx-alignment: " + (message.getSender().getId() == currentUser.getId() ? "center-right" : "center-left") + ";");
        messagesContainer.getChildren().add(messageBox);
    }

    private void setupPolling() {
        pollingExecutor = Executors.newSingleThreadScheduledExecutor();
        pollingExecutor.scheduleAtFixedRate(() -> {
            if (conversation != null) {
                List<Message> currentMessages = messageService.getMessagesByConversation(conversation.getId());
                if (!currentMessages.equals(lastMessagesSnapshot)) {
                    lastMessagesSnapshot = new ArrayList<>(currentMessages);
                    Platform.runLater(this::loadMessages);
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void closeWindow() {
        if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
            pollingExecutor.shutdownNow();
        }
        if (onClose != null) onClose.run();
        miniMessengerRoot.setVisible(false);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
