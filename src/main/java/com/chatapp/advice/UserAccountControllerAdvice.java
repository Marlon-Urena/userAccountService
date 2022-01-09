package com.chatapp.advice;

import com.chatapp.exception.EmailExistsException;
import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.exception.UsernameExistsException;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class UserAccountControllerAdvice {
    @ResponseBody
    @ExceptionHandler(EmailExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String emailExistInDatabaseHandler(EmailExistsException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UsernameExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String usernameExistInDatabaseHandler(UsernameExistsException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(UserAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String userAccountNotFoundHandler(UserAccountNotFoundException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(FirebaseAuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    String unauthorizedAccess(FirebaseAuthException e) {
        e.printStackTrace();
        return e.getMessage();
    }
}