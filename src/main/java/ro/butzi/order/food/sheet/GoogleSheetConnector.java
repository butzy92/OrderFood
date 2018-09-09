package ro.butzi.order.food.sheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsRequestInitializer;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Configuration
public class GoogleSheetConnector {

    private final NetHttpTransport httpTransport;
    private final JsonFactory jsonFactory;



    public GoogleSheetConnector() throws GeneralSecurityException, IOException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        jsonFactory = JacksonFactory.getDefaultInstance();
    }

    private Credential getCredentials() throws IOException {
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(new ClassPathResource("google-credential.json").getInputStream()));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE))
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
                .setAccessType("offline")
                .build();
        LocalServerReceiver build = new LocalServerReceiver.Builder()
                .setPort(8081).build();

        return new AuthorizationCodeInstalledApp(flow, build)
                .authorize("userTest");
    }

    @Bean
    public Sheets.Spreadsheets googleSpreadSheets() throws IOException {
        Sheets build = new Sheets.Builder(
                httpTransport,
                jsonFactory,
                getCredentials()
        ).setSheetsRequestInitializer(new SheetsRequestInitializer())
                .setApplicationName("Test Project")
                .build();

        return build.spreadsheets();
    }
}
