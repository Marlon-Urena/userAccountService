package com.chatapp.services;

import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.model.UserAccount;
import com.chatapp.model.UserPersonalInfo;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.services.implementation.UserAccountServiceImpl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import com.google.firebase.cloud.StorageClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

  @Mock StorageClient client;
  @Mock UserAccountRepository repository;
  @Mock FirebaseAuth firebaseAuth;

  @Test
  @DisplayName("Should return user account when uid exist in database ")
  void Should_return_user_when_user_exist_in_database() {
    when(repository.findById("1")).thenReturn(Optional.of(new UserAccount()));
    UserAccountService userAccountService =
        new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertNotNull(userAccountService.findUserAccount("1"));
  }

  @Test
  @DisplayName("Should throw exception when user is not found")
  void Should_throw_when_user_is_not_found() {
    when(repository.findById("1")).thenReturn(Optional.empty());
    UserAccountService userAccountService =
        new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertThrows(UserAccountNotFoundException.class, () -> userAccountService.findUserAccount("1"));
  }

  @Test
  @DisplayName("Should return response entity when user is successfully updated")
  void Should_successfully_update_user_when_user_exists() {
    UserAccount user = new UserAccount();
    when(repository.existsById("1")).thenReturn(true);
    when(repository.save(user)).thenReturn(user);
    UserAccountService userAccountService =
        new UserAccountServiceImpl(repository, client, firebaseAuth);
    ResponseEntity<UserAccount> response = userAccountService.updateUserAccount(user, "1");
    assertNotNull(response.getBody());
  }

  @Test
  @DisplayName("Should throw when user to be updated does not exist")
  void Should_throw_when_user_to_be_updated_does_not_exist() {
    UserAccount user = new UserAccount();
    when(repository.existsById("1")).thenReturn(false);
    UserAccountService userAccountService =
        new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertThrows(
        UserAccountNotFoundException.class, () -> userAccountService.updateUserAccount(user, "1"));
  }

  @Test
  @DisplayName("Should return response entity when user is successfully ")
  void Should_successfully_update_user_personal_information_when_user_exists() {
    UserPersonalInfo personalInfo =
        new UserPersonalInfo("John", "Doe", "1 Waverly Ave", "New York", "NY", "USA", "00000");
    UserAccount testUser =
        new UserAccount(
            "1",
            "johndoe@gmail.com",
            "johndoe",
            "Jane",
            "Do",
            "2 Waverly Ave",
            "New Jersey",
            "NJ",
            "USA",
            "10000",
            "online",
            OffsetDateTime.now(),
            "000-000-0000",
            "url");
    UserAccount testUpdatedUser =
        new UserAccount(
            "1",
            "johndoe@gmail.com",
            "johndoe",
            "John",
            "Doe",
            "1 Waverly Ave",
            "New York",
            "NY",
            "USA",
            "00000",
            "online",
            OffsetDateTime.now(),
            "000-000-0000",
            "url");
    when(repository.findById("1")).thenReturn(Optional.of(testUser));
    when(repository.save(testUpdatedUser)).thenReturn(testUpdatedUser);
    UserAccountService userAccountService =
        new UserAccountServiceImpl(repository, client, firebaseAuth);
    ResponseEntity<UserAccount> response =
        userAccountService.updateUserPersonalInfo(personalInfo, "1");
    assertEquals(response.getBody(), testUpdatedUser);
  }

  @Test
  @DisplayName("Should return response entity when user is successfully ")
  void Should_throw_when_updating_personal_info_and_user_does_not_exist() {
    UserPersonalInfo personalInfo =
        new UserPersonalInfo("John", "Doe", "1 Waverly Ave", "New York", "NY", "USA", "00000");
    when(repository.findById("1")).thenReturn(Optional.empty());
    UserAccountService userAccountService = new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertThrows(
        UserAccountNotFoundException.class,
        () -> userAccountService.updateUserPersonalInfo(personalInfo, "1"));
  }

  @Test
  @DisplayName("Should return true if username exists in database")
  void Should_return_true_if_username_exists() {
    when(repository.existsUserAccountByUsername("john")).thenReturn(true);
    UserAccountService userAccountService = new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertTrue(userAccountService.checkUsernameAvailability("john"));
  }

  @Test
  @DisplayName("Should return false if username does not exist in database")
  void Should_return_false_if_username_does_not_exist() {
    when(repository.existsUserAccountByUsername("john")).thenReturn(false);
    UserAccountService userAccountService = new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertFalse(userAccountService.checkUsernameAvailability("john"));
  }

  @Test
  @DisplayName("Should return true if email exists in database")
  void Should_return_true_if_email_exists() {
    when(repository.existsUserAccountByEmail("john@gmail.com")).thenReturn(true);
    UserAccountService userAccountService = new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertTrue(userAccountService.checkEmailAvailability("john@gmail.com"));
  }

  @Test
  @DisplayName("Should return false if email does not exist in database")
  void Should_return_false_if_email_does_not_exist() {
    when(repository.existsUserAccountByUsername("john@gmail.com")).thenReturn(false);
    UserAccountService userAccountService =
        new UserAccountServiceImpl(repository, client, firebaseAuth);
    assertFalse(userAccountService.checkUsernameAvailability("john@gmail.com"));
  }

  @Test
  @DisplayName("Should update email if the user exists in database")
  void Should_update_email_if_user_exists() throws FirebaseAuthException {
    UserAccount testUser = new UserAccount(
            "1",
            "johndoe@gmail.com",
            "johndoe",
            "John",
            "Doe",
            "2 Waverly Ave",
            "New Jersey",
            "NJ",
            "USA",
            "10000",
            "online",
            OffsetDateTime.now(),
            "000-000-0000",
            "url");

    UserAccount testUpdatedUser = new UserAccount(
            "1",
            "john@gmail.com",
            "johndoe",
            "John",
            "Doe",
            "2 Waverly Ave",
            "New Jersey",
            "NJ",
            "USA",
            "10000",
            "online",
            OffsetDateTime.now(),
            "000-000-0000",
            "url");

    when(repository.existsUserAccountByEmail("john@gmail.com")).thenReturn(false);
    when(repository.findById("1")).thenReturn(Optional.of(testUser));
    when(repository.save(testUpdatedUser)).thenReturn(testUpdatedUser);

    UpdateRequest request = mock(UpdateRequest.class);
    when(firebaseAuth.updateUser(request)).thenReturn(null);

    UserAccountService userAccountService = new UserAccountServiceImpl(repository, client, firebaseAuth);
    ResponseEntity<UserAccount> updatedUser = null;

    try {
      updatedUser = userAccountService.updateEmail("john@gmail.com", "1", request);
    } catch (FirebaseAuthException e) {
      fail(e);
    }
    assertEquals(updatedUser.getBody(), testUpdatedUser);
  }

  @Test
  @DisplayName("Should update username when user exists in database and username not taken")
  void Should_update_username_if_user_exists_and_username_is_available() {
    UserAccount testUser = new UserAccount(
            "1",
            "johndoe@gmail.com",
            "johndoe",
            "John",
            "Doe",
            "2 Waverly Ave",
            "New Jersey",
            "NJ",
            "USA",
            "10000",
            "online",
            OffsetDateTime.now(),
            "000-000-0000",
            "url");

    UserAccount testUpdatedUser = new UserAccount(
            "1",
            "johndoe@gmail.com",
            "john",
            "John",
            "Doe",
            "2 Waverly Ave",
            "New Jersey",
            "NJ",
            "USA",
            "10000",
            "online",
            OffsetDateTime.now(),
            "000-000-0000",
            "url");

    when(repository.existsUserAccountByUsername("john")).thenReturn(false);
    when(repository.findById("1")).thenReturn(Optional.of(testUser));
    when(repository.save(testUpdatedUser)).thenReturn(testUpdatedUser);

    UserAccountService userAccountService = new UserAccountServiceImpl(repository, client, firebaseAuth);
    ResponseEntity<UserAccount> updatedUser = userAccountService.updateUsername("john", "1");

    assertEquals(updatedUser.getBody(), testUpdatedUser);
  }
}



