package com.chatapp.service;

import com.chatapp.dto.ContactDTO;
import com.chatapp.dto.UserResponseDTO;
import com.chatapp.model.UserAccount;
import com.chatapp.dto.UserPersonalInfo;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserAccountService {
    UserResponseDTO findUserAccount(String uid);
    UserResponseDTO addUserAccount(UserAccount newUserAccount);
    UserResponseDTO updateUserAccount(UserAccount updatedUserAccount, String uid);
    void deleteUserAccount(String email);
    UserResponseDTO updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String uid);
    boolean isUsernameAvailable(String username);
    boolean isEmailAvailable(String email);
    UserResponseDTO updateEmail(String newEmail, String uid, UpdateRequest request) throws FirebaseAuthException;
    UserResponseDTO updateUsername(String newUsername, String uid, UpdateRequest request) throws FirebaseAuthException;
    UserResponseDTO updateProfilePhoto(MultipartFile photo, String uid) throws IOException;
    List<ContactDTO> findUserAccounts(String searchQuery, Pageable pageable);
}
