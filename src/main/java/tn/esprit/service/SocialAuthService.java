package tn.esprit.service;

import com.google.gson.Gson;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;
import tn.esprit.config.DatabaseConfig;
import tn.esprit.model.User;

public class SocialAuthService {

    private static final Logger LOGGER = Logger.getLogger(SocialAuthService.class.getName());

    private static final String FB_APP_ID = System.getenv("FB_APP_ID");
    private static final String FB_APP_SECRET = System.getenv("FB_APP_SECRET");
    private static final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_CLIENT_ID"); // Ajouter dans les variables d'environnement
    private static final String GOOGLE_CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET"); // Ajouter dans les variables d'environnement
    private static final String REDIRECT_URI = "http://localhost:8081/callback";
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:8082/callback";

    public CompletableFuture<User> loginWithFacebook() {
        return CompletableFuture.supplyAsync(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8081);
                CompletableFuture<User> future = new CompletableFuture<>();

                String facebookLoginUrl = String.format(
                    "https://www.facebook.com/v18.0/dialog/oauth?" +
                    "client_id=%s" +
                    "&redirect_uri=%s" +
                    "&state=123456" +
                    "&scope=email,public_profile",
                    FB_APP_ID,
                    URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
                );

                LOGGER.info("Ouverture de l'URL Facebook : " + facebookLoginUrl);

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(facebookLoginUrl));
                } else {
                    throw new RuntimeException("Impossible d'ouvrir le navigateur.");
                }

                ServerSocket finalServerSocket = serverSocket;
                new Thread(() -> {
                    try {
                        LOGGER.info("En attente de la réponse de Facebook...");
                        Socket clientSocket = finalServerSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        String line = in.readLine();
                        LOGGER.info("Réponse reçue : " + line);

                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Type: text/html; charset=utf-8");
                        out.println("");
                        out.println("<html><body><h1>Authentification en cours...</h1><p>Vous pouvez fermer cette fenêtre.</p></body></html>");

                        if (line != null && line.contains("code=")) {
                            String code = line.split("code=")[1].split(" ")[0];
                            LOGGER.info("Code d'autorisation reçu : " + code);

                            String accessToken = exchangeCodeForAccessToken(code);
                            if (accessToken == null) {
                                throw new RuntimeException("Échec de l'échange du code pour un token d'accès");
                            }

                            String userInfo = getFacebookUserInfo(accessToken);
                            Gson gson = new Gson();
                            FacebookUserInfo userInfoJson = gson.fromJson(userInfo, FacebookUserInfo.class);
                            String email = userInfoJson.email;

                            User user = findOrCreateUser(email, "ROLE_PATIENT");
                            future.complete(user);
                        } else {
                            LOGGER.severe("Erreur : Pas de code dans la réponse");
                            future.completeExceptionally(new RuntimeException("Échec de l'authentification Facebook"));
                        }

                        clientSocket.close();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors de l'authentification Facebook", e);
                        future.completeExceptionally(e);
                    } finally {
                        try {
                            finalServerSocket.close();
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Erreur lors de la fermeture du ServerSocket", e);
                        }
                    }
                }).start();

                return future.get();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur globale lors de la connexion Facebook", e);
                throw new RuntimeException("Erreur de connexion Facebook : " + e.getMessage());
            }
        });
    }

    // Nouvelle méthode pour générer l'URL d'authentification Google
    public String getGoogleAuthUrl() {
        return String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?" +
            "client_id=%s" +
            "&redirect_uri=%s" +
            "&response_type=code" +
            "&scope=email profile" +
            "&access_type=offline" +
            "&prompt=consent",
            GOOGLE_CLIENT_ID,
            URLEncoder.encode(GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8)
        );
    }

    // Méthode pour échanger le code d'autorisation contre un token et récupérer les informations de l'utilisateur
    public CompletableFuture<User> completeGoogleLogin(String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Échanger le code pour un token d'accès
                String tokenUrl = "https://oauth2.googleapis.com/token";
                String postData = String.format(
                    "code=%s" +
                    "&client_id=%s" +
                    "&client_secret=%s" +
                    "&redirect_uri=%s" +
                    "&grant_type=authorization_code",
                    URLEncoder.encode(code, StandardCharsets.UTF_8),
                    URLEncoder.encode(GOOGLE_CLIENT_ID, StandardCharsets.UTF_8),
                    URLEncoder.encode(GOOGLE_CLIENT_SECRET, StandardCharsets.UTF_8),
                    URLEncoder.encode(GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8)
                );

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(postData))
                    .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();
                LOGGER.info("Réponse token Google : " + responseBody);

                Gson gson = new Gson();
                GoogleTokenResponse tokenResponse = gson.fromJson(responseBody, GoogleTokenResponse.class);
                if (tokenResponse.access_token == null) {
                    LOGGER.severe("Échec de l'échange du code pour un token d'accès");
                    throw new RuntimeException("Échec de l'échange du code pour un token d'accès");
                }

                // Récupérer les informations de l'utilisateur
                String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
                HttpRequest userInfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(userInfoUrl))
                    .header("Authorization", "Bearer " + tokenResponse.access_token)
                    .GET()
                    .build();

                HttpResponse<String> userInfoResponse = client.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());
                String userInfo = userInfoResponse.body();
                LOGGER.info("Informations utilisateur Google : " + userInfo);

                GoogleUserInfo userInfoJson = gson.fromJson(userInfo, GoogleUserInfo.class);
                String email = userInfoJson.email;

                if (email == null || email.isEmpty()) {
                    LOGGER.severe("Échec de la récupération de l'email de l'utilisateur Google");
                    throw new RuntimeException("Échec de la récupération de l'email de l'utilisateur");
                }

                LOGGER.info("Email Google récupéré : " + email);
                return findOrCreateUser(email, "ROLE_PATIENT");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la finalisation de la connexion Google", e);
                throw new RuntimeException("Erreur lors de la connexion Google : " + e.getMessage(), e);
            }
        });
    }

    private String exchangeCodeForAccessToken(String code) throws Exception {
        String tokenUrl = String.format(
            "https://graph.facebook.com/v18.0/oauth/access_token?" +
            "client_id=%s" +
            "&redirect_uri=%s" +
            "&client_secret=%s" +
            "&code=%s",
            FB_APP_ID,
            URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8),
            FB_APP_SECRET,
            URLEncoder.encode(code, StandardCharsets.UTF_8)
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(tokenUrl))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        LOGGER.info("Réponse token Facebook : " + responseBody);

        Gson gson = new Gson();
        FacebookTokenResponse tokenResponse = gson.fromJson(responseBody, FacebookTokenResponse.class);
        return tokenResponse.access_token;
    }

    private String getFacebookUserInfo(String accessToken) throws Exception {
        String userInfoUrl = String.format(
            "https://graph.facebook.com/me?fields=id,email&access_token=%s",
            URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(userInfoUrl))
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private User findOrCreateUser(String email, String defaultRole) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String selectSql = "SELECT * FROM user WHERE email = ?";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, email);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    return new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("roles")
                    );
                } else {
                    String insertSql = "INSERT INTO user (email, password, roles) VALUES (?, '', ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, email);
                        insertStmt.setString(2, defaultRole);
                        insertStmt.executeUpdate();

                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            return new User(
                                generatedKeys.getLong(1),
                                email,
                                "",
                                defaultRole
                            );
                        }
                    }
                }
            }
        }
        throw new Exception("Impossible de créer ou récupérer l'utilisateur");
    }

    private static class FacebookTokenResponse {
        String access_token;
    }

    private static class FacebookUserInfo {
        String id;
        String email;
    }

    private static class GoogleTokenResponse {
        String access_token;
    }

    private static class GoogleUserInfo {
        String email;
    }
}