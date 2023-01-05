package com.chatapp.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
public class ContactDTO {
    private String uid;

    private String email;

    private String username;

    private String firstName;

    private String lastName;

    private String address;

    private String phoneNumber;

    private String avatar;

    private String status;

    private OffsetDateTime lastActivity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactDTO that = (ContactDTO) o;
        return Objects.equals(uid, that.uid) && Objects.equals(email, that.email) && Objects.equals(username, that.username) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(address, that.address) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(avatar, that.avatar) && Objects.equals(status, that.status) && Objects.equals(lastActivity, that.lastActivity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, email, username, firstName, lastName, address, phoneNumber, avatar, status, lastActivity);
    }
}
