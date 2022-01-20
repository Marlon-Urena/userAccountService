package com.chatapp.services.implementation;

import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.services.UserAccountService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository repository;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserAccount findUserAccount(String authorizationHeader) throws FirebaseAuthException {
        String email;
        String accessToken = getBearerToken(authorizationHeader);
        try {
            email = FirebaseAuth.getInstance(FirebaseApp.getInstance())
                        .verifyIdToken(accessToken, true)
                        .getEmail();
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthException(e.getErrorCode(), e.getMessage(), e.getCause(), e.getHttpResponse(), e.getAuthErrorCode());
        }
        String finalEmail = email;
        return repository.findUserAccountByEmail(email).orElseThrow(() -> new UserAccountNotFoundException(finalEmail));
    }

    @Override
    public ResponseEntity<UserAccount> addUserAccount(UserAccount newUserDetails) {
        return null;
    }

    @Override
    public ResponseEntity<UserAccount> updateUserAccount(UserAccount newUserDetails, String authorizationHeader) throws FirebaseAuthException {
        String email = newUserDetails.getEmail();
        String accessToken = getBearerToken(authorizationHeader);
        try {
            FirebaseAuth.getInstance(FirebaseApp.getInstance())
                    .verifyIdToken(accessToken, true);
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthException(e.getErrorCode(), e.getMessage(), e.getCause(), e.getHttpResponse(), e.getAuthErrorCode());
        }
        UserAccount updatedUserAccount = repository.findUserAccountByEmail(email).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = newUserDetails.toBuilder();
            userAccount = userAccountBuilder.email(email).build();
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(email));
        return ResponseEntity.status(201).body(updatedUserAccount);
    }

    @Override
    public ResponseEntity<UserAccount> deleteUserAccount(String userUID) {
        return null;
    }

    @Override
    public ResponseEntity<UserAccount> updateUserPersonalInfo(UserPersonalInfo userPersonalInfo, String authorizationHeader) throws FirebaseAuthException {
        String accessToken = getBearerToken(authorizationHeader);
        String email = FirebaseAuth.getInstance(FirebaseApp.getInstance())
                .verifyIdToken(accessToken, true)
                .getEmail();
        UserAccount updatedUserAccount = repository.findUserAccountByEmail(email).map(userAccount -> {
            UserAccount.UserAccountBuilder userAccountBuilder = userAccount.toBuilder();
            userAccount = userAccountBuilder
                    .firstName(userPersonalInfo.firstName)
                    .lastName(userPersonalInfo.lastName)
                    .address(userPersonalInfo.address)
                    .city(userPersonalInfo.city)
                    .state(userPersonalInfo.state)
                    .country(userPersonalInfo.country)
                    .zipCode(userPersonalInfo.zipCode)
                    .build();
            return repository.save(userAccount);
        }).orElseThrow(() -> new UserAccountNotFoundException(email));
        return ResponseEntity.status(201).body(updatedUserAccount);
    }

    private String getBearerToken(String authorizationHeader) {
        String bearerToken = null;
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            bearerToken = authorizationHeader.substring(7);
        }
        return bearerToken;
    }
}
