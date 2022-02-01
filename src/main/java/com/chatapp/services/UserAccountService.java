package com.chatapp.services;

import com.chatapp.model.ChangePasswordForm;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.google.firebase.auth.FirebaseAuthException;
import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;

public interface UserAccountService {
    UserAccount findUserAccount(String accessToken) throws FirebaseAuthException;
    ResponseEntity<UserAccount> addUserAccount(UserAccount newUserAccount);
    ResponseEntity<UserAccount> updateUserAccount(UserAccount updatedUserAccount, String authorizationHeader) throws FirebaseAuthException;
    ResponseEntity<UserAccount> deleteUserAccount(String email);
    ResponseEntity<UserAccount> updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String authorizationHeader) throws FirebaseAuthException;
    Boolean checkUsernameAvailability(String username);
    Boolean checkEmailAvailability(String email);
    ResponseEntity<UserAccount> updateEmail(String newEmail, String authorizationHeader) throws FirebaseAuthException;
}
