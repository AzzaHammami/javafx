package services;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import javafx.application.Platform;
import javafx.scene.control.Alert;
<<<<<<< HEAD
import utils.WindowsNotification;
=======
>>>>>>> 0437d716b496ba8972d63fba270ee7c757826b2b

public class FirebaseListener {
    public static void ecouterNotifications(String userId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("notifications/" + userId);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                String message = dataSnapshot.getValue(String.class);
                Platform.runLater(() -> {
<<<<<<< HEAD
                    WindowsNotification.show("Nouvelle notification !", message);
                    // Affichage optionnel JavaFX (peut être commenté si non désiré)
=======
>>>>>>> 0437d716b496ba8972d63fba270ee7c757826b2b
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Notification");
                    alert.setHeaderText("Nouvelle notification !");
                    alert.setContentText(message);
                    alert.showAndWait();
                });
<<<<<<< HEAD
                // Supprimer la notification après affichage
                dataSnapshot.getRef().removeValue(null);
=======
>>>>>>> 0437d716b496ba8972d63fba270ee7c757826b2b
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
