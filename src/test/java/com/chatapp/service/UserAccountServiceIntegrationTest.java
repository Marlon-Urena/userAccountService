package com.chatapp.service;

import com.chatapp.repository.UserAccountRepository;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DataJpaTest
public class UserAccountServiceIntegrationTest {
    @Autowired
    private UserAccountService service;

    @Autowired
    private UserAccountRepository repository;

    @Nested
    public class FindUserAccount {

    }

}
