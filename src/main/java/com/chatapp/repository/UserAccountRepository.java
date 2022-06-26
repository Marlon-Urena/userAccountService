package com.chatapp.repository;

import com.chatapp.model.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Boolean existsUserAccountByUsername(String username);
    Boolean existsUserAccountByEmail(String email);
    Page<UserAccount> searchByUsernameContainingIgnoreCase(String searchQuery, Pageable sortedByUsername);
}
