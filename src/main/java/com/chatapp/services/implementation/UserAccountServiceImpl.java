package com.chatapp.services.implementation;

import com.chatapp.exception.EmailExistsException;
import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.exception.UsernameExistsException;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.services.UserAccountService;
import com.google.cloud.storage.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class UserAccountServiceImpl implements UserAccountService {

  private final UserAccountRepository repository;
  private final StorageClient storageClient;
  private final FirebaseAuth firebaseAuth;

  @Autowired
  public UserAccountServiceImpl(UserAccountRepository repository, StorageClient storageClient, FirebaseAuth firebaseAuth) {
    this.repository = repository;
    this.storageClient = storageClient;
    this.firebaseAuth = firebaseAuth;
  }

  @Override
  public UserAccount findUserAccount(String uid) {
    return repository
        .findById(uid)
        .orElseThrow(() -> new UserAccountNotFoundException(uid));
  }

  @Override
  public ResponseEntity<UserAccount> addUserAccount(UserAccount newUserDetails) {
    return null;
  }

  @Override
  public ResponseEntity<UserAccount> updateUserAccount(
          UserAccount newUserDetails, String uid) {
    UserAccount updatedUserAccount;
    if (repository.existsById(uid)) {
      updatedUserAccount = repository.save(newUserDetails);
    } else {
      throw new UserAccountNotFoundException(uid);
    }

    return ResponseEntity.status(201).body(updatedUserAccount);
  }

  @Override
  public ResponseEntity<UserAccount> deleteUserAccount(String userUID) {
    return null;
  }

  @Override
  public ResponseEntity<UserAccount> updateUserPersonalInfo(
          UserPersonalInfo userPersonalInfo, String uid) {
    UserAccount updatedUserAccount =
        repository
            .findById(uid)
            .map(
                userAccount -> {
                  UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
                  userAccount =
                      userAccountBuilder
                          .firstName(userPersonalInfo.firstName)
                          .lastName(userPersonalInfo.lastName)
                          .address(userPersonalInfo.address)
                          .city(userPersonalInfo.city)
                          .state(userPersonalInfo.state)
                          .country(userPersonalInfo.country)
                          .zipCode(userPersonalInfo.zipCode)
                          .build();
                  return repository.save(userAccount);
                })
            .orElseThrow(() -> new UserAccountNotFoundException(uid));

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
  public ResponseEntity<UserAccount> updateEmail(String newEmail, String uid, UpdateRequest request) throws FirebaseAuthException {
    boolean emailExists = repository.existsUserAccountByEmail(newEmail);

    if (emailExists) {
      throw new EmailExistsException(newEmail);
    }

    UserAccount updatedUserAccount =
        repository
            .findById(uid)
            .map(
                userAccount -> {
                  UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
                  userAccount = userAccountBuilder.email(newEmail).build();
                  return repository.save(userAccount);
                })
            .orElseThrow(() -> new UserAccountNotFoundException(uid));

    firebaseAuth.updateUser(request);

    return ResponseEntity.status(201).body(updatedUserAccount);
  }

  @Override
  public ResponseEntity<UserAccount> updateUsername(
          String newUsername, String uid) {
    boolean usernameExists = repository.existsUserAccountByUsername(newUsername);
    if (usernameExists) {
      throw new UsernameExistsException(newUsername);
    }
    UserAccount updatedUserAccount =
        repository
            .findById(uid)
            .map(
                userAccount -> {
                  UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
                  userAccount = userAccountBuilder.username(newUsername).build();

                  return repository.save(userAccount);
                })
            .orElseThrow(() -> new UserAccountNotFoundException(uid));

    return ResponseEntity.status(201).body(updatedUserAccount);
  }

  @Override
  public ResponseEntity<UserAccount> updateProfilePhoto(MultipartFile photo, String uid) throws IOException {
    Bucket bucket = storageClient.bucket();
    Blob blob = bucket.create(photo.getOriginalFilename(), photo.getInputStream(), photo.getContentType());
    String mediaUrl = buildMediaUrl(bucket.getName(), blob.getName());

    UserAccount updatedUserAccount =
        repository
            .findById(uid)
            .map(
                userAccount -> {
                  UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
                  userAccount = userAccountBuilder.photoUrl(mediaUrl).build();
                  return repository.save(userAccount);
                })
            .orElseThrow(() -> new UserAccountNotFoundException(uid));

    return ResponseEntity.status(201).body(updatedUserAccount);
  }

  @Override
  public List<UserAccount> findUserAccounts(String searchQuery, String uid) {
    Page<UserAccount> userAccounts;
    Pageable sortedByUsername = PageRequest.of(0, 20, Sort.by("username"));
    if (searchQuery == null || searchQuery.isEmpty()) {
      userAccounts = repository.findAll(sortedByUsername);
    } else {
      userAccounts = repository.searchByUsernameContainingIgnoreCase(searchQuery, sortedByUsername);
    }

    return userAccounts.getContent();
  }

  private String buildMediaUrl(String bucketName, String blobName) {
    return String.format(
        "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, blobName);
  }
}
