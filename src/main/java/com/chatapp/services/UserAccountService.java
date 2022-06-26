package com.chatapp.services;

import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserAccountService {
    UserAccount findUserAccount(String uid);
    ResponseEntity<UserAccount> addUserAccount(UserAccount newUserAccount);
    ResponseEntity<UserAccount> updateUserAccount(UserAccount updatedUserAccount, String uid);
    ResponseEntity<UserAccount> deleteUserAccount(String email);
    ResponseEntity<UserAccount> updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String uid);
    Boolean checkUsernameAvailability(String username);
    Boolean checkEmailAvailability(String email);
    ResponseEntity<UserAccount> updateEmail(String newEmail, String uid, UpdateRequest request) throws FirebaseAuthException;
    ResponseEntity<UserAccount> updateUsername(String newUsername, String uid);
    ResponseEntity<UserAccount> updateProfilePhoto(MultipartFile photo, String uid) throws IOException;
    List<UserAccount> findUserAccounts(String searchQuery, String uid);
}
