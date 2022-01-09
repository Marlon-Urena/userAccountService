package com.chatapp.repository;

import com.chatapp.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findUserAccountByEmail(String email);
    UserAccount deleteUserAccountByEmail(String userAccountId);
}
