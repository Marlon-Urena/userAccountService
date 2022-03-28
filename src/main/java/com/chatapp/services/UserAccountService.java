package com.chatapp.services;

import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserAccountService {
    UserAccount findUserAccount(String accessToken) throws FirebaseAuthException;
    ResponseEntity<UserAccount> addUserAccount(UserAccount newUserAccount);
    ResponseEntity<UserAccount> updateUserAccount(UserAccount updatedUserAccount, String authorizationHeader) throws FirebaseAuthException;
    ResponseEntity<UserAccount> deleteUserAccount(String email);
    ResponseEntity<UserAccount> updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String authorizationHeader) throws FirebaseAuthException;
    Boolean checkUsernameAvailability(String username);
    Boolean checkEmailAvailability(String email);
    ResponseEntity<UserAccount> updateEmail(String newEmail, String authorizationHeader) throws FirebaseAuthException;
    ResponseEntity<UserAccount> updateUsername(String newUsername, String authorizationHeader) throws FirebaseAuthException;
    ResponseEntity<UserAccount> updateProfilePhoto(MultipartFile photo, String authorizationHeader) throws FirebaseAuthException, IOException;
    ResponseEntity<List<UserAccount>> findUserAccounts(String searchQuery) throws FirebaseAuthException;
}
