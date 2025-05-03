package com.example.rendez_vous.controllers.api;

import com.example.rendez_vous.entities.Notification;
import com.example.rendez_vous.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    @Autowired
    private NotificationRepository notificationRepository;

    // Ajouter une notification
    @PostMapping
    public Notification addNotification(@RequestBody Notification notification) {
        notification.setTimestamp(System.currentTimeMillis());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    // Récupérer les notifications d’un utilisateur
    @GetMapping
    public List<Notification> getNotifications(@RequestParam Integer userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    // Marquer comme lue
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        Notification notif = notificationRepository.findById(id).orElseThrow();
        notif.setRead(true);
        return notificationRepository.save(notif);
    }
}
