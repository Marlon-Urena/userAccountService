package com.chatapp.exception;

public class UserAccountNotFoundException extends RuntimeException {

   public UserAccountNotFoundException(String email) {
        super("Could not find user account with email:" + email);
    }
}
