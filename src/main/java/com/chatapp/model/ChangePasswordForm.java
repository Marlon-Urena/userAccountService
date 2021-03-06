package com.chatapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChangePasswordForm {
    private String currentPassword;
    private String newPassword;
}
