package services;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import utils.WindowsNotification;

public class FirebaseListener {
    public static void ecouterNotifications(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications/" + userId);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String message = dataSnapshot.getValue(String.class);
                Platform.runLater(() -> {
                    WindowsNotification.show("Nouvelle notification !", message);
                    // Affichage optionnel JavaFX (peut être commenté si non désiré)
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Notification");
                    alert.setHeaderText("Nouvelle notification !");
                    alert.setContentText(message);
                    alert.showAndWait();
                });
                // Supprimer la notification après affichage
                dataSnapshot.getRef().removeValue(null);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) { }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }
}
