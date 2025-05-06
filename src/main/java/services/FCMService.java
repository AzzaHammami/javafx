package services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FCMService {
    private static final String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "PASTE_YOUR_SERVER_KEY_HERE"; // Mets ta clé serveur ici

    public static void sendNotification(String fcmToken, String title, String body) throws Exception {
        String message = "{" +
            "\"to\":\"" + fcmToken + "\"," +
            "\"notification\":{" +
            "\"title\":\"" + title + "\"," +
            "\"body\":\"" + body + "\"" +
            "}" +
            "}";

        URL url = new URL(FCM_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(message.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        System.out.println("FCM Response Code: " + responseCode);
    }
}
