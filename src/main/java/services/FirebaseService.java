package services;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseService {

    static {
        initialize();
    }

    public static void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FileInputStream serviceAccount = new FileInputStream("C:/Users/ghofr/Downloads/econsult-1973a-firebase-adminsdk-fbsvc-f94a30b512.json");
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://econsult-1973a-default-rtdb.firebaseio.com")
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialisé (bloc statique) ! URL utilisée : https://econsult-1973a-default-rtdb.firebaseio.com");
            } catch (IOException e) {
                System.err.println("Erreur d'initialisation Firebase: " + e.getMessage());
            }
        }
    }
}
