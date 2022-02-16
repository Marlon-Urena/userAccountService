package com.chatapp.services.implementation;

import com.chatapp.exception.EmailExistsException;
import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.exception.UsernameExistsException;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.services.UserAccountService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.NoCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository repository;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserAccount findUserAccount(String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String email = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserRecord userRecord = firebaseAuth.getUserByEmail(email);
        return repository.findUserAccountByEmail(email).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
            return userAccountBuilder
                    .phoneNumber(userRecord.getPhoneNumber())
                    .photoUrl(userRecord.getPhotoUrl())
                    .build();
        }).orElseThrow(() -> new UserAccountNotFoundException(email));
    }

    @Override
    public ResponseEntity<UserAccount> addUserAccount(UserAccount newUserDetails) {
        return null;
    }

    @Override
    public ResponseEntity<UserAccount> updateUserAccount(UserAccount newUserDetails, String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String email = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserAccount updatedUserAccount = repository.findUserAccountByEmail(email).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = newUserDetails.toBuilder();
            userAccount = userAccountBuilder.email(email).build();
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(email));
        return ResponseEntity.status(201).body(updatedUserAccount);
    }

    @Override
    public ResponseEntity<UserAccount> deleteUserAccount(String userUID) {
        return null;
    }

    @Override
    public ResponseEntity<UserAccount> updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String email = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserRecord userRecord = firebaseAuth.getUserByEmail(email);
        UserAccount updatedUserAccount = repository.findUserAccountByEmail(email).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
            userAccount = userAccountBuilder
                    .firstName(userPersonalInfo.firstName)
                    .lastName(userPersonalInfo.lastName)
                    .address(userPersonalInfo.address)
                    .city(userPersonalInfo.city)
                    .state(userPersonalInfo.state)
                    .country(userPersonalInfo.country)
                    .zipCode(userPersonalInfo.zipCode)
                    .phoneNumber(userRecord.getPhoneNumber())
                    .photoUrl(userRecord.getPhotoUrl())
                    .build();
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(email));
        return ResponseEntity.status(201).body(updatedUserAccount);
    }

    @Override
    public Boolean checkUsernameAvailability(String username) {
        return repository.existsUserAccountByUsername(username);
    }

    @Override
    public Boolean checkEmailAvailability(String email) {
        return repository.existsUserAccountByEmail(email);
    }

    @Override
    public ResponseEntity<UserAccount> updateEmail(String newEmail, String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String email = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserRecord userRecord = firebaseAuth.getUserByEmail(email);
        boolean emailExists = repository.existsUserAccountByEmail(newEmail);
        if (emailExists) {
            throw new EmailExistsException(newEmail);
        }
        UserAccount updatedUserAccount = repository.findUserAccountByEmail(email)
                .map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
            userAccount = userAccountBuilder.email(newEmail).build();
            repository.deleteById(email);
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(email));
        UserRecord.UpdateRequest request = userRecord.updateRequest().setEmail(newEmail);
        UserRecord updatedUserRecord = firebaseAuth.updateUser(request);
        updatedUserAccount.setPhoneNumber(updatedUserRecord.getPhoneNumber());
        updatedUserAccount.setPhotoUrl(updatedUserRecord.getPhotoUrl());
        return ResponseEntity.status(201).body(updatedUserAccount);
    }

    @Override
    public ResponseEntity<UserAccount> updateUsername(String newUsername, String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String email = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserRecord userRecord = firebaseAuth.getUserByEmail(email);
        boolean usernameExists = repository.existsUserAccountByUsername(newUsername);
        if (usernameExists) {
            throw new UsernameExistsException(newUsername);
        }
        UserAccount updatedUserAccount = repository.findUserAccountByEmail(email)
                .map(userAccount -> {
                    UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
                    userAccount = userAccountBuilder
                            .username(newUsername)
                            .build();
                    return repository.save(userAccount);
                }).orElseThrow(() -> new UserAccountNotFoundException(email));
        UserRecord.UpdateRequest request = userRecord.updateRequest().setDisplayName(newUsername);
        UserRecord updatedUserRecord = firebaseAuth.updateUser(request);
        updatedUserAccount.setPhoneNumber(updatedUserRecord.getPhoneNumber());
        updatedUserAccount.setPhotoUrl(updatedUserRecord.getPhotoUrl());
        return ResponseEntity.status(201).body(updatedUserAccount);
    }

    @Override
    public ResponseEntity<UserAccount> updateProfilePhoto(MultipartFile photo, String authorizationHeader) throws FirebaseAuthException, IOException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String email = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserAccount userAccount = repository
                .findUserAccountByEmail(email)
                .orElseThrow(() -> new UserAccountNotFoundException(email));
        UserRecord userRecord = firebaseAuth.getUserByEmail(email);
        String emulatorHostPort = System.getenv("EMULATOR_HOST"); // replace with the correct port for your emulator instance
        System.out.println(emulatorHostPort);

        Storage emulatorStorage = StorageOptions.newBuilder()
                .setProjectId("holidayclub")
                .setHost(emulatorHostPort)
                .setCredentials(NoCredentials.getInstance())
                .build()
                .getService();
        BlobId blobId = BlobId.of("default-bucket", photo.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(photo.getContentType()).build();
        InputStream inputStream = new BufferedInputStream(photo.getInputStream());
        System.out.println("Obtained inputStream");
        System.out.println(inputStream.available());
        Blob blob = emulatorStorage.createFrom(blobInfo, inputStream);
        UserRecord.UpdateRequest request = userRecord.updateRequest().setPhotoUrl(blob.getMediaLink().replaceFirst("0\\.0\\.0\\.0", "91.125.116.125"));
        UserRecord updatedUserRecord = firebaseAuth.updateUser(request);
        userAccount.setPhoneNumber(updatedUserRecord.getPhoneNumber());
        userAccount.setPhotoUrl(updatedUserRecord.getPhotoUrl());
        return ResponseEntity.status(201).body(userAccount);
    }

    private String getUIDFromEmail(String email) throws FirebaseAuthException {
        FirebaseAuth firebaseInstance = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        return firebaseInstance.getUserByEmail(email).getUid();
    }

    private String getBearerToken(String authorizationHeader) {
        String bearerToken = null;
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7);
        }
        return bearerToken;
    }
}
