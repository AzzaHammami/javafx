package Controllers;

import models.Conversation;
import models.Message;
import models.User;
import services.ConversationService;
import services.MessageService;
import services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MessengerController implements Initializable {
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private ListView<Conversation> conversationListView;
    @FXML private Label conversationTitleLabel;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;

    private UserService userService;
    private ConversationService conversationService;
    private MessageService messageService;
    private User currentUser;
    private Conversation selectedConversation;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService = new UserService();

        // Create services without circular dependencies
        messageService = new MessageService();
        conversationService = new ConversationService();

        // Set the cross-references
        messageService.setConversationService(conversationService);
        conversationService.setMessageService(messageService);
        System.out.println("Message input exists: " + (messageInput != null));
        System.out.println("Send button exists: " + (sendButton != null));

// Force visibility
        messageInput.setStyle("""
    -fx-background-color: white !important;
    -fx-border-color: black !important;
    -fx-min-height: 60px !important;
    -fx-pref-height: 60px !important;
    -fx-opacity: 1 !important;
    """);

        sendButton.setStyle("""
    -fx-background-color: green !important;
    -fx-text-fill: white !important;
    -fx-min-width: 80px !important;
    -fx-opacity: 1 !important;
    """);

        messagesContainer.setStyle("-fx-background-color: lightblue !important;");
        setupUI();
        setupEventHandlers();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (userNameLabel != null && user != null) {
            userNameLabel.setText(user.getName());
            loadConversationsSafe();
        }
    }

    private void setupUI() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            loadConversationsSafe();
        } else {
            userNameLabel.setText("Non connecté");
        }
        sendButton.setDisable(true);
    }

    private void setupEventHandlers() {
        setupConversationList();
        setupSearch();

        messageInput.textProperty().addListener((obs, oldVal, newVal) -> {
            updateSendButtonState();
        });

        sendButton.setOnAction(e -> handleSendMessage());
    }

    private void setupConversationList() {
        conversationListView.setCellFactory(param -> new ListCell<Conversation>() {
            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox(5);
                    String title = getConversationTitle(conversation);
                    Label nameLabel = new Label(title);
                    nameLabel.setStyle("-fx-font-weight: bold;");

                    // Add unread count badge if any
                    int unreadCount = messageService.getUnreadCount(currentUser.getId());
                    if (unreadCount > 0) {
                        Circle badge = new Circle(8, Color.RED);
                        HBox header = new HBox(5, nameLabel, badge);
                        container.getChildren().add(header);
                    } else {
                        container.getChildren().add(nameLabel);
                    }

                    // Add last message preview
                    List<Message> messages = messageService.getMessagesByConversation(conversation.getId());
                    if (!messages.isEmpty()) {
                        Message lastMessage = messages.get(messages.size() - 1);
                        Label lastMessageLabel = new Label(
                                lastMessage.getSender().getName() + ": " +
                                        (lastMessage.getContent().length() > 30 ?
                                                lastMessage.getContent().substring(0, 30) + "..." :
                                                lastMessage.getContent())
                        );
                        lastMessageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                        container.getChildren().add(lastMessageLabel);
                    }

                    setGraphic(container);
                }
            }
        });

        conversationListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedConversation = newVal;
                        loadMessages(newVal);
                        conversationTitleLabel.setText(getConversationTitle(newVal));
                        messageService.markMessagesAsRead(newVal.getId(), currentUser.getId());
                    } else {
                        selectedConversation = null;
                    }
                    updateSendButtonState();
                }
        );
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadConversationsSafe();
            } else {
                filterConversations(newVal);
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        if (selectedConversation == null || messageInput.getText().trim().isEmpty()) {
            return;
        }

        Message message = new Message();
        message.setConversationId(selectedConversation.getId());
        message.setSender(currentUser);

        // For direct messages, set the receiver
        if (!selectedConversation.isGroup()) {
            User receiver = getOtherParticipant(selectedConversation);
            if (receiver != null) {
                message.setReceiverId(receiver.getId());
            }
        }

        message.setContent(messageInput.getText().trim());
        message.setSentAt(LocalDateTime.now());

        try {
            messageService.sendMessage(message);
            messageInput.clear();
            loadMessages(selectedConversation);
        } catch (Exception e) {
            showError("Erreur lors de l'envoi du message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewConversation() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle conversation");
        dialog.setHeaderText("Sélectionnez un utilisateur");

        // Set the button types
        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create the user list
        ListView<User> userListView = new ListView<>();
        List<User> allUsers = userService.getAllUsers();
        allUsers.removeIf(u -> u.getId() == currentUser.getId());
        userListView.setItems(FXCollections.observableArrayList(allUsers));
        userListView.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName());
            }
        });

        dialog.getDialogPane().setContent(userListView);

        // Enable/disable create button based on selection
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            createButton.setDisable(newVal == null);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return userListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(this::createNewConversationWith);
    }

    private void createNewConversationWith(User otherUser) {
        Conversation conversation = conversationService.getOrCreateDirectConversation(
                currentUser.getId(),
                otherUser.getId()
        );

        if (conversation != null) {
            loadConversationsSafe();
            conversationListView.getSelectionModel().select(conversation);
        } else {
            showError("Échec lors de la création de la conversation");
        }
    }

    private User getOtherParticipant(Conversation conversation) {
        for (User participant : conversation.getParticipants()) {
            if (participant.getId() != currentUser.getId()) {
                return participant;
            }
        }
        return null;
    }

    private void loadConversations() {
        if (currentUser == null) return;

        List<Conversation> conversations = conversationService.getUserConversations(currentUser.getId());
        ObservableList<Conversation> obsList = FXCollections.observableArrayList(conversations);
        conversationListView.setItems(obsList);

        if (obsList.isEmpty()) {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        conversationTitleLabel.setText("Aucune conversation disponible");
        messagesContainer.getChildren().clear();
        Label emptyLabel = new Label("Cliquez sur 'Nouvelle conversation' pour démarrer un chat.");
        emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 16px; -fx-padding: 30px;");
        messagesContainer.getChildren().add(emptyLabel);
    }

    private void loadMessages(Conversation conversation) {
        messagesContainer.getChildren().clear();
        if (conversation == null) {
            return;
        }

        List<Message> messages = messageService.getMessagesByConversation(conversation.getId());
        if (messages.isEmpty()) {
            Label emptyLabel = new Label("Aucun message dans cette conversation");
            emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 16px; -fx-padding: 30px;");
            messagesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Message message : messages) {
            addMessageBubble(message);
        }

        messagesScrollPane.setVvalue(1.0);
    }

    private void addMessageBubble(Message message) {
        HBox messageBox = new HBox(10);
        VBox bubbleBox = new VBox(5);
        Label contentLabel = new Label(message.getContent());
        Label timeLabel = new Label(formatDateTime(message.getSentAt()));

        contentLabel.setWrapText(true);
        timeLabel.getStyleClass().add("message-time");

        // Create avatar with user initial
        Circle avatarBg = new Circle(15);
        avatarBg.setFill(Color.LIGHTGRAY);
        Label initial = new Label(message.getSender().getName().substring(0, 1).toUpperCase());
        StackPane avatar = new StackPane(avatarBg, initial);

        if (message.getSender().getId() == currentUser.getId()) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            bubbleBox.getStyleClass().add("message-bubble-sent");
            messageBox.getChildren().add(bubbleBox);
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            bubbleBox.getStyleClass().add("message-bubble-received");
            messageBox.getChildren().addAll(avatar, bubbleBox);
        }

        bubbleBox.getChildren().addAll(contentLabel, timeLabel);
        bubbleBox.getStyleClass().add("message-bubble");
        messagesContainer.getChildren().add(messageBox);
    }

    private String getConversationTitle(Conversation conversation) {
        if (conversation.isGroup()) {
            return conversation.getTitle();
        }

        for (User participant : conversation.getParticipants()) {
            if (participant.getId() != currentUser.getId()) {
                return participant.getName();
            }
        }
        return "Conversation";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return dateTime.format(formatter);
    }

    private void updateSendButtonState() {
        boolean disable = selectedConversation == null || messageInput.getText().trim().isEmpty();
        sendButton.setDisable(disable);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadConversationsSafe() {
        if (currentUser != null) {
            loadConversations();
        }
    }

    private void filterConversations(String searchText) {
        List<Conversation> allConversations = conversationService.getUserConversations(currentUser.getId());
        ObservableList<Conversation> filteredList = FXCollections.observableArrayList();

        for (Conversation conversation : allConversations) {
            String title = getConversationTitle(conversation);
            if (title.toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(conversation);
            }
        }

        conversationListView.setItems(filteredList);
    }
}