package com.chatapp.controller;

import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.services.UserAccountService;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @CrossOrigin
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Autowired
    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping(path="/user")
    public UserAccount getUser(@RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.findUserAccount(authorizationHeader);
    }

    @PostMapping(path="/register")
    public ResponseEntity<UserAccount> createUser(@RequestBody UserAccount newUserAccount) {
        return userAccountService.addUserAccount(newUserAccount);
    }

    @PutMapping(path="/user")
    public ResponseEntity<UserAccount> updateUser(@RequestBody UserAccount userAccount, @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.updateUserAccount(userAccount, authorizationHeader);
    }

    @PutMapping(path = "/user/personal_info")
    public ResponseEntity<UserAccount> updateUserPersonalInfo(@RequestBody UserPersonalInfo userPersonalInfo, @RequestHeader(name = "Authorization") String authorizationHeader) throws FirebaseAuthException {
        return userAccountService.updateUserPersonalInfo(userPersonalInfo, authorizationHeader);
    }
}
