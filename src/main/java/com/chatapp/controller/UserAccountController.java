package com.chatapp.controller;

import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.services.UserAccountService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController @CrossOrigin
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Autowired
    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping(path = "/user")
    public UserAccount getUser(@RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.findUserAccount(authorizationHeader);
    }

    @PostMapping(path = "/register")
    public ResponseEntity<UserAccount> createUser(@RequestBody UserAccount newUserAccount) {
        return userAccountService.addUserAccount(newUserAccount);
    }

    @PostMapping(path = "/email-availability")
    public Boolean checkEmailAvailability(@RequestBody String email) {
        return userAccountService.checkEmailAvailability(email);
    }

    @PostMapping(path = "/username-availability")
    public Boolean checkUsernameAvailability(@RequestBody String username) {
        return userAccountService.checkUsernameAvailability(username);
    }

    @PutMapping(path = "/user")
    public ResponseEntity<UserAccount> updateUser(
            @RequestBody UserAccount userAccount,
            @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.updateUserAccount(userAccount, authorizationHeader);
    }

    @PutMapping(path = "/user/personal_info")
    public ResponseEntity<UserAccount> updateUserPersonalInfo(
            @RequestBody UserPersonalInfo userPersonalInfo,
            @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.updateUserPersonalInfo(userPersonalInfo, authorizationHeader);
    }

    @PatchMapping(path = "/user/change_email")
    public ResponseEntity<UserAccount> updateEmail(
            @RequestBody String newEmail,
            @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.updateEmail(newEmail, authorizationHeader);
    }

    @PatchMapping(path = "/user/change_username")
    public ResponseEntity<UserAccount> updateUsername(
            @RequestBody String newUsername,
            @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.updateUsername(newUsername, authorizationHeader);
    }

    @PostMapping(path = "/user/change_profile_photo")
    public ResponseEntity<UserAccount> updateProfilePhoto(
            @RequestParam("file") MultipartFile photo,
            @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException, IOException {
        return userAccountService.updateProfilePhoto(photo, authorizationHeader);
    }

    @GetMapping(path = "/contacts")
    public ResponseEntity<Map<String, List<UserAccount>>> getContacts(
            @RequestParam(name = "query", required = false) String searchQuery) throws FirebaseAuthException {
        return userAccountService.findUserAccounts(searchQuery);
    }

}
