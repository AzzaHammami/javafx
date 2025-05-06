package services;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class GoogleApi {

    private static final String APPLICATION_NAME = "GestionReclamationApp";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final java.util.List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "C:/Users/ghofr/Downloads/credentials.json"; // chemin vers ton fichier téléchargé

    public static Credential getCredentials(final com.google.api.client.http.javanet.NetHttpTransport HTTP_TRANSPORT) throws Exception {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) throws Exception {
        final com.google.api.client.http.javanet.NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String spreadsheetId = "1TrWsvtqrDd2yja0fTHDxcDD-pjZ6QfIdHmkZcl18Dm0"; // Remplace par l'ID réel du fichier Sheets
        String sheetName = "messages_sheet";
        String range = sheetName + "!A:Z"; // Pour toutes les colonnes de A à Z
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                System.out.println(row);
            }
        }
    }

    // Ajout d'une réclamation dans Google Sheets
    public static void ajouterReclamationDansSheet(String spreadsheetId, String sheetName, Object id, Object utilisateur, Object sujet, Object description, Object date, Object statut) throws Exception {
        final com.google.api.client.http.javanet.NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        String range = sheetName + "!A:F";
        List<Object> row = Arrays.asList(id, utilisateur, sujet, description, date, statut);
        ValueRange appendBody = new ValueRange().setValues(Collections.singletonList(row));
        AppendValuesResponse appendResult = service.spreadsheets().values()
                .append(spreadsheetId, range, appendBody)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}
