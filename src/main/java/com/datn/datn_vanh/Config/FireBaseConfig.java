package com.datn.datn_vanh.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/*
   Cấu hình kết nối firebase
 */
@Configuration
public class FireBaseConfig {
    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json")) {

            if (serviceAccount == null) {
                throw new FileNotFoundException("serviceAccountKey.json file not found in resources folder.");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized from config.");
            }
        } catch (IOException e) {
            System.err.println("Firebase init failed: " + e.getMessage());
        }
    }
}
