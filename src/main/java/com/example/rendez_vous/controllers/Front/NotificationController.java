package com.example.rendez_vous.controllers.Front;

import com.example.rendez_vous.services.ExternalNotificationService;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationController {
    private final ExternalNotificationService notificationService = new ExternalNotificationService();
    private VBox notificationBox;
    private int currentUserId;
    private Label notifBadge;
    private List<ExternalNotificationService.Notification> allNotifications;

    public NotificationController(VBox notificationBox, int userId, Label notifBadge) {
        this.notificationBox = notificationBox;
        this.currentUserId = userId;
        this.notifBadge = notifBadge;
        loadNotifications("toutes");
    }

    public void loadNotifications(String filter) {
        new Thread(() -> {
            allNotifications = notificationService.getNotificationsForUser(currentUserId);
            List<ExternalNotificationService.Notification> filteredNotifications = filterNotifications(allNotifications, filter);

            Platform.runLater(() -> {
                notificationBox.getChildren().clear();
                for (ExternalNotificationService.Notification notif : filteredNotifications) {
                    notificationBox.getChildren().add(createNotifItem(notif));
                }
                updateNotifBadge();
            });
        }).start();
    }

    private List<ExternalNotificationService.Notification> filterNotifications(List<ExternalNotificationService.Notification> notifications, String filter) {
        return notifications.stream()
                .filter(notif -> {
                    switch (filter) {
                        case "non_lues":
                            return !notif.read;
                        case "medicales":
                            return notif.title.toLowerCase().contains("rendez-vous") ||
                                    notif.title.toLowerCase().contains("médical") ||
                                    notif.title.toLowerCase().contains("medical");
                        case "administratives":
                            return notif.title.toLowerCase().contains("admin") ||
                                    notif.title.toLowerCase().contains("paiement") ||
                                    notif.title.toLowerCase().contains("document");
                        default: // "toutes"
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    private Node createNotifItem(ExternalNotificationService.Notification notif) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-padding: 8 12; -fx-background-radius: 4; -fx-border-radius: 4; -fx-font-size: 13px; -fx-margin: 4 0;");

        // Tick icon container
        Label tickIcon = new Label("");
        tickIcon.setStyle("-fx-text-fill: #1976d2; -fx-font-size: 16px; -fx-min-width: 20px;");

        VBox textBox = new VBox(2);
        Label titre = new Label(notif.title);
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 15px;");
        Label message = new Label(notif.message);
        message.setWrapText(true);
        textBox.getChildren().addAll(titre, message);

        Label markRead = new Label("Marquer comme lu");
        markRead.setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold; -fx-cursor: hand;");

        HBox actionBox = new HBox(5);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.getChildren().add(markRead);

        markRead.setOnMouseClicked(e -> {
            notif.read = true;
            notificationService.markAsRead(notif.id);
            tickIcon.setText("✓");
            itemBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-padding: 8 12; -fx-background-radius: 4; -fx-border-radius: 4; -fx-font-size: 13px; -fx-margin: 4 0;");
            markRead.setText("Lu");
            markRead.setStyle("-fx-text-fill: #757575; -fx-font-weight: normal;");
            updateNotifBadge();
        });

        itemBox.getChildren().addAll(tickIcon, textBox, actionBox);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        return itemBox;
    }

    private void updateNotifBadge() {
        long unreadCount = allNotifications.stream()
                .filter(n -> !n.read)
                .count();

        if (unreadCount > 0) {
            notifBadge.setText(String.valueOf(unreadCount));
            notifBadge.setVisible(true);
        } else {
            notifBadge.setVisible(false);
        }
    }
}