package com.chatapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.StorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Configuration
public class FirebaseConfig {
    @Bean
    public void firebaseInit() {
        try {
            String serviceAccountJson = System.getenv("SERVICE_ACCOUNT_JSON");
            InputStream serviceAccount = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(System.getenv("BUCKET_NAME"))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            System.out.println("Firebase Initialized");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    @DependsOn({"firebaseInit"})
    public StorageClient storageClient() {
        return StorageClient.getInstance();
    }

    @Bean
    @DependsOn({"firebaseInit"})
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }
}
