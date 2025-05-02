package services;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationService {
    public static void envoyerNotification(String userId, String message) {
        // Toujours forcer l'initialisation AVANT toute utilisation
        services.FirebaseService.initialize();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications/" + userId);
        ref.push().setValueAsync(message);
        System.out.println("[DEBUG] Notification envoyée à userId=" + userId + " : " + message);
    }
}
