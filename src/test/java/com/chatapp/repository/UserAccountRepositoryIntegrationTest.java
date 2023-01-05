package com.chatapp.repository;

import com.chatapp.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase
class UserAccountRepositoryIntegrationTest {

    @Autowired
    private UserAccountRepository repository;

    @Test
    void returnSavedUser_WhenPassedInUser() {
        UserAccount user = createUser("johndoe@gmail.com", "johndoe");
        UserAccount savedUser = repository.save(user);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getUid());
    }

    private void insertUser() {
        UserAccount user = createUser("johndoe@gmail.com", "johndoe");
        repository.save(user);
    }

    private UserAccount createUser(String email, String username) {
        UserAccount.UserAccountBuilder userBuilder = UserAccount.builder();

        UserAccount user = userBuilder
                .uid("1")
                .email(email)
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .address("2 Waverly Ave")
                .city("Newark")
                .state("NJ")
                .country("USA")
                .zipCode("10000")
                .status("offline")
                .lastActivity(OffsetDateTime.of(2020, 1, 10, 10, 10, 10, 10, ZoneOffset.MIN))
                .phoneNumber("000-000-0000")
                .photoUrl("url")
                .build();

        return user;
    }

}