package com.chatapp.controller;

import com.chatapp.dto.ContactDTO;
import com.chatapp.dto.UserPersonalInfo;
import com.chatapp.dto.UserResponseDTO;
import com.chatapp.model.UserAccount;
import com.chatapp.service.UserAccountService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/users")
public class UserAccountController {

    private final UserAccountService userAccountService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserAccountController(UserAccountService userAccountService, ModelMapper modelMapper) {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        this.userAccountService = userAccountService;
        this.modelMapper = modelMapper;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{userId}")
    public UserResponseDTO getUser(@PathVariable String userId) {
        return userAccountService.findUserAccount(userId);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @GetMapping("/search/contacts")
    public List<ContactDTO> getContacts(@RequestParam(name = "username", required = false) String username,
                                        Pageable pageable) {
        return userAccountService.findUserAccounts(username, pageable);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserResponseDTO createUser(@RequestBody UserAccount newUserAccount) {
        return userAccountService.addUserAccount(newUserAccount);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/email_availability")
    public boolean isEmailAvailable(@RequestBody String email) {
        return userAccountService.isEmailAvailable(email);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/username_availability")
    public boolean isUsernameAvailable(@RequestBody String username) {
        return userAccountService.isUsernameAvailable(username);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @PutMapping(path = "/{userId}")
    public UserResponseDTO updateUser(@PathVariable String userId, @RequestBody UserAccount userAccount) {
        return userAccountService.updateUserAccount(userAccount, userId);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @PutMapping(path = "/{userId}/personal_info")
    public UserResponseDTO updateUserPersonalInfo(@PathVariable String userId, @RequestBody UserPersonalInfo userPersonalInfo) {
        return userAccountService.updateUserPersonalInfo(userPersonalInfo, userId);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @PatchMapping(path = "/{userId}/email")
    public UserResponseDTO updateEmail(@PathVariable String userId, @RequestBody String newEmail) throws FirebaseAuthException {
        UpdateRequest request = new UpdateRequest(userId).setEmail(newEmail);

        return userAccountService.updateEmail(newEmail, userId, request);
    }
    @ResponseStatus(code = HttpStatus.OK)
    @PatchMapping(path = "/{userId}/username")
    public UserResponseDTO updateUsername(@PathVariable String userId, @RequestBody String newUsername) throws FirebaseAuthException {
        UpdateRequest request = new UpdateRequest(userId).setDisplayName(newUsername);

        return userAccountService.updateUsername(newUsername, userId, request);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @PatchMapping(path = "/{userId}/profile_photo")
    public UserResponseDTO updateProfilePhoto(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile photo)
            throws IOException {

        return userAccountService.updateProfilePhoto(photo, userId);
    }

    private ContactDTO convertToDto(UserAccount userAccount) {
        ContactDTO contactDTO = modelMapper.map(userAccount, ContactDTO.class);

        contactDTO.setAvatar(userAccount.getPhotoUrl());
        return contactDTO;
    }
}