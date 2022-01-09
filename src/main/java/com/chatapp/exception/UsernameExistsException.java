package com.chatapp.exception;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String username) {
        super("Username " + username + " has been taken. Try Another.");
    }
}
