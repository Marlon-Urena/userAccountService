package com.chatapp.exception;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

public class FirebaseUserNotFoundException extends AuthenticationCredentialsNotFoundException {

    public FirebaseUserNotFoundException() {
        super("Firebase user not found");
    }
}
