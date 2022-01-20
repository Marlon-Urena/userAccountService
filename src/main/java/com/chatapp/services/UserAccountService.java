package com.chatapp.services;

import com.chatapp.model.ChangePasswordForm;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.ResponseEntity;

public interface UserAccountService {
    UserAccount findUserAccount(String accessToken) throws FirebaseAuthException;
    ResponseEntity<UserAccount> addUserAccount(UserAccount newUserAccount);
    ResponseEntity<UserAccount> updateUserAccount(UserAccount updatedUserAccount, String authorizationHeader) throws FirebaseAuthException;
    ResponseEntity<UserAccount> deleteUserAccount(String email);
    ResponseEntity<UserAccount> updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String authorizationHeader) throws FirebaseAuthException;
}
