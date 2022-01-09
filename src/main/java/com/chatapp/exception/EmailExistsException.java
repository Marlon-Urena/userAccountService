package com.chatapp.exception;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String email) {
        super("Email " + email + " has been taken. Try another.");
    }
}
