package com.chatapp.services.implementation;

import com.chatapp.exception.EmailExistsException;
import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.exception.UsernameExistsException;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.services.UserAccountService;
import com.google.cloud.storage.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
        String uid = firebaseAuth.verifyIdToken(accessToken, true).getUid();
        UserRecord userRecord = firebaseAuth.getUser(uid);
        return repository.findById(uid).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
            return userAccountBuilder
                    .phoneNumber(userRecord.getPhoneNumber())
                    .photoUrl(userRecord.getPhotoUrl())
                    .build();
        }).orElseThrow(() -> new UserAccountNotFoundException(uid));
    }

    @Override
    public ResponseEntity<UserAccount> addUserAccount(UserAccount newUserDetails) {
        return null;
    }

    @Override
    public ResponseEntity<UserAccount> updateUserAccount(UserAccount newUserDetails, String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance(FirebaseApp.getInstance());
        String uid = firebaseAuth.verifyIdToken(accessToken, true).getUid();
        UserAccount updatedUserAccount = repository.findById(uid).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = newUserDetails.toBuilder();
            userAccount = userAccountBuilder.build();
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(uid));
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
        String uid = firebaseAuth.verifyIdToken(accessToken, true).getEmail();
        UserRecord userRecord = firebaseAuth.getUser(uid);
        UserAccount updatedUserAccount = repository.findById(uid).map(userAccount -> {
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
        }).orElseThrow(() -> new UserAccountNotFoundException(uid));
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
        String uid = firebaseAuth.verifyIdToken(accessToken, true).getUid();
        UserRecord userRecord = firebaseAuth.getUser(uid);
        boolean emailExists = repository.existsUserAccountByEmail(newEmail);
        if (emailExists) {
            throw new EmailExistsException(newEmail);
        }
        UserAccount updatedUserAccount = repository.findById(uid)
                .map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
            userAccount = userAccountBuilder.email(newEmail).build();
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(uid));
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
        String uid = firebaseAuth.verifyIdToken(accessToken, true).getUid();
        UserRecord userRecord = firebaseAuth.getUser(uid);
        boolean usernameExists = repository.existsUserAccountByUsername(newUsername);
        if (usernameExists) {
            throw new UsernameExistsException(newUsername);
        }
        UserAccount updatedUserAccount = repository.findById(uid)
                .map(userAccount -> {
                    UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
                    userAccount = userAccountBuilder
                            .username(newUsername)
                            .build();
                    return repository.save(userAccount);
                }).orElseThrow(() -> new UserAccountNotFoundException(uid));
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
        String uid = firebaseAuth.verifyIdToken(accessToken, true).getUid();
        UserRecord userRecord = firebaseAuth.getUser(uid);

        UserAccount userAccount = repository
                .findById(uid)
                .orElseThrow(() -> new UserAccountNotFoundException(uid));

        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(photo.getOriginalFilename(), photo.getInputStream(), photo.getContentType());
        String mediaUrl = buildMediaUrl(bucket.getName(), blob.getName());

        UserRecord.UpdateRequest request = userRecord.updateRequest().setPhotoUrl(mediaUrl);
        UserRecord updatedUserRecord = firebaseAuth.updateUser(request);
        userAccount.setPhoneNumber(updatedUserRecord.getPhoneNumber());
        userAccount.setPhotoUrl(updatedUserRecord.getPhotoUrl());

        return ResponseEntity.status(201).body(userAccount);
    }

    @Override
    public ResponseEntity<List<UserAccount>> findUserAccounts(String searchQuery) {
        Page<UserAccount> userAccounts;
        Pageable sortedByUsername =
                PageRequest.of(0, 20, Sort.by("username"));
        if (searchQuery == null || searchQuery.isEmpty()) {
            userAccounts = repository.findAll(sortedByUsername);
        } else {
            userAccounts = repository.searchByUsernameContainingIgnoreCase(searchQuery, sortedByUsername);
        }

        return ResponseEntity.ok(userAccounts.getContent());
    }

    private String buildMediaUrl(String bucketName, String blobName) {
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, blobName);
    }

    private String getBearerToken(String authorizationHeader) {
        String bearerToken = null;
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7);
        }
        return bearerToken;
    }
}
