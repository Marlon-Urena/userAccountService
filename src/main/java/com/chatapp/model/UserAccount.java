package com.chatapp.model;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "user_account")
public class UserAccount {
    @Id
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
}
