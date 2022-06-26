package com.chatapp.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ContactDTO {
    private String id;

    private String email;

    private String username;

    private String name;

    private String address;

    private String phoneNumber;

    private String avatar;

    private String status;

    private OffsetDateTime lastActivity;
}
