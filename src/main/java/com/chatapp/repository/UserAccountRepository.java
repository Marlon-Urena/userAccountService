package com.chatapp.repository;

import com.chatapp.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findUserAccountByEmail(String email);
    Boolean existsUserAccountByUsername(String username);
    Boolean existsUserAccountByEmail(String email);
    UserAccount deleteUserAccountByEmail(String userAccountId);
}
