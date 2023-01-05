package com.chatapp.service;

import com.chatapp.dto.ContactDTO;
import com.chatapp.dto.UserPersonalInfo;
import com.chatapp.dto.UserResponseDTO;
import com.chatapp.exception.EmailExistsException;
import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.exception.UsernameExistsException;
import com.chatapp.model.UserAccount;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.service.implementation.UserAccountServiceImpl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAccountServiceUnitTest {

    @Mock
    UserAccountRepository repository;

    @Mock
    Bucket bucket;

    @Mock
    FirebaseAuth firebaseAuth;

    @Spy
    ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    UserAccountServiceImpl userAccountService;

    @Nested
    @DisplayName("findUserAccount method")
    public class FindUserAccount {
        @Test
        @DisplayName("Calls findById method of UserAccountRepository once")
        void callsFindByIdMethodOfUserAccountRepositoryOnce_WhenCalled() {
            String userId = "1";
            UserAccount userAccount = getTestUser();
            when(repository.findById(userId)).thenReturn(Optional.ofNullable(userAccount));
            
            userAccountService.findUserAccount(userId);

            verify(repository, times(1)).findById(anyString());
        }

        @Test
        @DisplayName("Should return user account when userId exists")
        void returnUser_WhenPassedInExistingUserId() {
            String userId = "1";
            UserAccount user = getTestUser();
            UserResponseDTO expectedUser = getTestUserResponseDTO();
            when(repository.findById(userId)).thenReturn(Optional.of(user));

            UserResponseDTO actualUser = userAccountService.findUserAccount(userId);

            assertEquals(expectedUser, actualUser);
        }

        @Test
        @DisplayName("Should throw exception when user is not found")
        void throwUserAccountNotFoundException_WhenPassedInNonExistingUserId() {
            String userId = "1";
            when(repository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserAccountNotFoundException.class,
                    () -> userAccountService.findUserAccount(userId));
        }
    }

    @Nested
    @DisplayName("addUserAccount method")
    public class AddUserAccount {
        @Test
        @DisplayName("Should add new user when passed in user has unique email and unique username")
        void returnNewUserAccount_WhenPassedInUniqueEmailAndUniqueUsername() {
            UserAccount passedInUser = getTestUser();
            when(repository.save(any(UserAccount.class))).thenReturn(passedInUser);
            UserResponseDTO expectedUser = getTestUserResponseDTO();

            UserResponseDTO actualUser = userAccountService.addUserAccount(passedInUser);

            assertEquals(expectedUser, actualUser);
        }

        @Test
        @DisplayName("Should throw EmailExistsException when passed in user with non-unique email and unique username")
        void throwEmailExistsException_WhenPassedInNonUniqueEmailAndUniqueUsername() {
            UserAccount passedInUser = getTestUser();
            when(repository.existsUserAccountByEmail(passedInUser.getEmail())).thenReturn(true);

            assertThrows(EmailExistsException.class, () -> userAccountService.addUserAccount(passedInUser));
        }

        @Test
        @DisplayName("Should throw UsernameExistsException when passed in user with unique email and non-unique username")
        void throwUsernameExistsException_WhenPassedInUniqueEmailAndNonUniqueUsername() {
            UserAccount passedInUser = getTestUser();
            when(repository.existsUserAccountByUsername(passedInUser.getUsername())).thenReturn(true);

            assertThrows(UsernameExistsException.class, () -> userAccountService.addUserAccount(passedInUser));
        }
    }

    @Nested
    @DisplayName("updateUserAccount method")
    public class UpdateUserAccount {
        @Test
        @DisplayName("Should return updated user when passed in userId of existing user")
        void returnUpdatedUser_WhenPassedInUserIdOfExistingUser() {
            String userId = "1";
            UserAccount passedInUser = getTestUser();
            when(repository.existsById(userId)).thenReturn(true);
            when(repository.save(any(UserAccount.class))).thenReturn(passedInUser);
            UserResponseDTO expectedUser = getTestUserResponseDTO();

            UserResponseDTO actualUser = userAccountService.updateUserAccount(passedInUser, userId);

            assertEquals(expectedUser, actualUser);
        }

        @Test
        @DisplayName("Should throw UserAccountNotFoundException when passed in userId of non-existing user")
        void throwUserAccountNotFoundException_WhenPassedInUserIdOfNonExistingUser() {
            String userId = "1";
            UserAccount user = new UserAccount();
            when(repository.existsById(userId)).thenReturn(false);

            assertThrows(UserAccountNotFoundException.class,
                    () -> userAccountService.updateUserAccount(user, userId));
            verify(repository, times(0)).save(user);
        }
    }

    @Nested
    @DisplayName("deleteUserAccount method")
    public class DeleteUserAccount {
        @Test
        @DisplayName("Removes user when there exists a user with passed in userId")
        void doesNotThrow_WhenPassedInUserIdOfExistingUser() {
            String userId = "1";
            when(repository.existsById(userId)).thenReturn(true);

            assertDoesNotThrow(() -> userAccountService.deleteUserAccount(userId));
        }

        @Test
        @DisplayName("Throws UserAccountNotFoundException when there does not exist a user with passed in userId")
        void throwUserAccountNotFoundException_WhenPassedInUserIdOfNonExistingUser() {
            String userId = "1";
            when(repository.existsById(userId)).thenReturn(false);

            assertThrows(UserAccountNotFoundException.class, () -> userAccountService.deleteUserAccount(userId));
        }
    }

    @Nested
    @DisplayName("updateUserPersonalInfo method")
    public class UpdateUserPersonalInfo {
        @Test
        @DisplayName("Should return updated user when passed in new user personal info and userId of existing user")
        void returnUpdatedUser_WhenPassedInUserIdOfExistingUser() {
            String userId = "1";
            UserAccount foundUser = getTestUser();

            UserPersonalInfo newUserPersonalInfo = getTestUserPersonalInfo();
            newUserPersonalInfo.setAddress("1 Waverly Ave");
            newUserPersonalInfo.setCity("New York");
            newUserPersonalInfo.setState("NY");
            newUserPersonalInfo.setZipCode("00000");

            UserResponseDTO expectedUser = getTestUserResponseDTO();
            expectedUser.setAddress("1 Waverly Ave");
            expectedUser.setCity("New York");
            expectedUser.setState("NY");
            expectedUser.setZipCode("00000");

            when(repository.findById(userId)).thenReturn(Optional.of(foundUser));
            when(repository.save(any(UserAccount.class))).thenReturn(foundUser);


            UserResponseDTO actualUpdatedUser = userAccountService.updateUserPersonalInfo(newUserPersonalInfo, userId);


            assertEquals(expectedUser, actualUpdatedUser);
        }

        @Test
        @DisplayName("Should throw UserAccountNotFoundException when passed in UserPersonalInfo and userId of user that does not exist ")
        void throwUserAccountNotFoundException_WhenPassedInUserIdOfNonExistingUser() {
            String userid = "1";
            UserPersonalInfo personalInfo = getTestUserPersonalInfo();

            when(repository.findById(userid)).thenReturn(Optional.empty());

            assertThrows(
                    UserAccountNotFoundException.class,
                    () -> userAccountService.updateUserPersonalInfo(personalInfo, userid));
        }
    }

    @Nested
    @DisplayName("isUsernameAvailable method")
    public class IsUsernameAvailable {
        @Test
        @DisplayName("Should return false if username exists in database")
        void returnFalse_WhenPassedInUsernameIsAlreadyTaken() {
            String unAvailableUsername = "john";
            when(repository.existsUserAccountByUsername(unAvailableUsername)).thenReturn(true);

            assertFalse(userAccountService.isUsernameAvailable(unAvailableUsername));
        }

        @Test
        @DisplayName("Should return true if username does not exist in database")
        void returnTrue_WhenPassedInAvailableUsername() {
            String availableUsername = "john";
            when(repository.existsUserAccountByUsername(availableUsername)).thenReturn(false);

            assertTrue(userAccountService.isUsernameAvailable(availableUsername));
        }
    }

    @Nested
    @DisplayName("isEmailAvailable method")
    public class IsEmailAvailable {
        @Test
        @DisplayName("Should return false if email exists in database")
        void returnFalse_WhenPassedInEmailIsAlreadyTaken() {
            String email = "john@gmail.com";
            when(repository.existsUserAccountByEmail(email)).thenReturn(true);

            assertFalse(userAccountService.isEmailAvailable(email));
        }

        @Test
        @DisplayName("Should return true if email does not exist in database")
        void returnTrue_WhenPassedInAvailableEmail() {
            String email = "john@gmail.com";
            when(repository.existsUserAccountByUsername(email)).thenReturn(false);

            assertTrue(userAccountService.isUsernameAvailable(email));
        }
    }

    @Nested
    @DisplayName("updateEmail method")
    public class UpdateEmail {
        @Test
        @DisplayName("Should update email if the user exists and email is not already taken")
        void returnUpdatedUser_WhenPassedInUserIdOfExistingUser() throws FirebaseAuthException {
            String newEmail = "john@gmail.com";
            String userId = "1";

            UserAccount user = getTestUser();
            user.setEmail(newEmail);
            UpdateRequest request = mock(UpdateRequest.class);

            when(repository.existsUserAccountByEmail(newEmail)).thenReturn(false);
            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(repository.save(any(UserAccount.class))).thenReturn(user);
            UserResponseDTO expectedUser = getTestUserResponseDTO();
            expectedUser.setEmail(newEmail);


            UserResponseDTO updatedUser = userAccountService.updateEmail(newEmail, userId, request);


            assertEquals(expectedUser, updatedUser);
        }

        @Test
        @DisplayName("Throws EmailExistException when passed in email that has been already taken")
        void throwEmailExistException_WhenPassedInEmailIsAlreadyTaken() {
            String newEmail = "john@gmail.com";
            String userId = "1";
            UpdateRequest request = mock(UpdateRequest.class);
            when(repository.existsUserAccountByEmail(newEmail)).thenReturn(true);


            assertThrows(EmailExistsException.class,
                    () -> userAccountService.updateEmail(newEmail, userId, request));
        }

        @Test
        @DisplayName("Throws UserAccountNotFoundException when passed in userId of that does not belong to a user")
        void throwUserAccountNotFoundException_WhenPassedInUserIdOfNonExistingUser() {
            String newEmail = "john@gmail.com";
            String userId = "1";
            UpdateRequest request = mock(UpdateRequest.class);
            when(repository.existsUserAccountByEmail(newEmail)).thenReturn(false);


            assertThrows(UserAccountNotFoundException.class,
                    () -> userAccountService.updateEmail(newEmail, userId, request));
        }
    }

    @Nested
    @DisplayName("updateUsername method")
    public class UpdateUsername {
        @Test
        @DisplayName("Should update username when user exists and username not already taken")
        void returnUpdatedUser_WhenPassedInExistingUserIdAndAvailableUsername() throws FirebaseAuthException {
            String newUsername = "john";
            String userId = "1";
            UserAccount user = getTestUser();
            user.setUsername(newUsername);
            UserResponseDTO expectedUpdatedUser = getTestUserResponseDTO();
            expectedUpdatedUser.setUsername(newUsername);
            UpdateRequest request = mock(UpdateRequest.class);

            when(repository.existsUserAccountByUsername(newUsername)).thenReturn(false);
            when(repository.findById(userId)).thenReturn(Optional.of(user));
            when(repository.save(any(UserAccount.class))).thenReturn(user);

            UserResponseDTO actualUpdatedUser = userAccountService.updateUsername(newUsername, userId, request);

            assertEquals(expectedUpdatedUser, actualUpdatedUser);
        }

        @Test
        @DisplayName("Throws UsernameExistException when passed in unavailable username")
        void throwUsernameExistException_WhenPassedInUnAvailableUsername() {
            String newUsername = "john";
            String userId = "1";
            when(repository.existsUserAccountByUsername(newUsername)).thenReturn(true);
            UpdateRequest request = mock(UpdateRequest.class);

            assertThrows(UsernameExistsException.class,
                    () -> userAccountService.updateUsername(newUsername, userId, request));
        }

        @Test
        @DisplayName("Throws UserAccountNotFoundException when passed userId that does not belong to a user")
        void throwUserAccountNotFoundException_WhenPassedInNonExistingUserId() {
            String newUsername = "john";
            String userId = "1";
            UpdateRequest request = mock(UpdateRequest.class);
            when(repository.existsUserAccountByUsername(newUsername)).thenReturn(false);
            when(repository.findById(userId)).thenReturn(Optional.empty());


            assertThrows(UserAccountNotFoundException.class,
                    () -> userAccountService.updateUsername(newUsername, userId, request));
        }
    }

    @Nested
    @DisplayName("updateProfilePhoto method")
    public class UpdateProfilePhoto {

        @Test
        void returnUpdatedUser_WhenPassedInUserIdOfExistingUser() throws IOException {
            String userId = "1";
            UserAccount userAccount = getTestUser();

            String fileName = "profile-photo";
            String contentType = "jpeg";
            InputStream content = new ByteArrayInputStream("Hello, World!".getBytes(UTF_8));
            String blobName = "123";

            Blob mockBlob = mock(Blob.class);
            when(mockBlob.getName()).thenReturn(blobName);

            when(bucket.create(fileName, content, contentType)).thenReturn(mockBlob);

            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(fileName);
            when(mockFile.getInputStream()).thenReturn(content);
            when(mockFile.getContentType()).thenReturn(contentType);

            when(repository.findById(userId)).thenReturn(Optional.of(userAccount));
            when(repository.save(any(UserAccount.class))).thenReturn(userAccount);
            UserResponseDTO expectedUpdatedUser = getTestUserResponseDTO();
            expectedUpdatedUser.setPhotoUrl("https://firebasestorage.googleapis.com/v0/b/123/o/123?alt=media");


            UserResponseDTO actualUpdatedUser = userAccountService.updateProfilePhoto(mockFile, userId);


            assertEquals(expectedUpdatedUser, actualUpdatedUser);
        }

        @Test
        void throwUserAccountNotFoundException_WhenPassedInUserIdOfNonExistingUser() throws IOException {
            String blobName = "123";
            Blob mockBlob = mock(Blob.class);
            when(mockBlob.getName()).thenReturn(blobName);

            String fileName = "profile-photo";
            String contentType = "jpeg";
            InputStream content = new ByteArrayInputStream("Hello, World!".getBytes(UTF_8));
            when(bucket.create(fileName, content, contentType)).thenReturn(mockBlob);

            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(fileName);
            when(mockFile.getInputStream()).thenReturn(content);
            when(mockFile.getContentType()).thenReturn(contentType);

            String userId = "1";
            when(repository.findById(userId)).thenReturn(Optional.empty());


            assertThrows(UserAccountNotFoundException.class,
                    () -> userAccountService.updateProfilePhoto(mockFile, userId));
        }
    }

    @Nested
    @DisplayName("findAllUserAccounts method")
    public class FindUserAccounts {

        @Test
        void returnListOfUsersOfSizeOne_WhenPassedInValidUsername() {
            String username = "john";
            int expectedSize = 1;
            Pageable pageable = Pageable.unpaged();
            when(repository.searchByUsernameContainingIgnoreCase(username, pageable))
                    .thenReturn(new PageImpl<>(List.of(new UserAccount())));

            List<ContactDTO> actualUsers = userAccountService.findUserAccounts(username, pageable);
            int actualSize = actualUsers.size();

            assertEquals(expectedSize, actualSize);
        }

        @Test
        void returnListOfUsersOfSizeThree_WhenPassedInNullUsername() {
            String username = null;
            Pageable pageable = Pageable.unpaged();
            List<UserAccount> userAccounts = Stream.generate(UserAccount::new)
                    .limit(3)
                    .collect(Collectors.toList());
            Page<UserAccount> pages = new PageImpl<>(userAccounts);
            when(repository.findAll(pageable)).thenReturn(pages);
            int expectedSize = 3;

            List<ContactDTO> actualUserAccounts = userAccountService.findUserAccounts(username, pageable);
            int actualSize = actualUserAccounts.size();

            assertEquals(expectedSize, actualSize);
        }

        @Test
        void returnListOfUsersOfSizeFour_WhenPassedInEmptyUsername() {
            String username = "";
            Pageable pageable = Pageable.unpaged();
            int expectedSize = 4;
            List<UserAccount> userAccounts = Stream.generate(UserAccount::new)
                    .limit(expectedSize)
                    .collect(Collectors.toList());
            Page<UserAccount> pages = new PageImpl<>(userAccounts);
            when(repository.findAll(pageable)).thenReturn(pages);

            List<ContactDTO> actualUserAccounts = userAccountService.findUserAccounts(username, pageable);
            int actualSize = actualUserAccounts.size();

            assertEquals(expectedSize, actualSize);
        }
    }

    private UserAccount getTestUser() {
        UserAccount.UserAccountBuilder userBuilder = UserAccount.builder();

        return userBuilder
                .uid("1")
                .email("johndoe@gmail.com")
                .username("johndoe")
                .firstName("John")
                .lastName("Doe")
                .address("2 Waverly Ave")
                .city("Newark")
                .state("NJ")
                .country("USA")
                .zipCode("10000")
                .status("online")
                .lastActivity(OffsetDateTime.of(2020, 1, 10, 10, 10, 10, 10, ZoneOffset.MIN))
                .phoneNumber("000-000-0000")
                .photoUrl("url")
                .build();
    }

    private UserResponseDTO getTestUserResponseDTO() {
        UserResponseDTO userResponseDTO = new UserResponseDTO();

        userResponseDTO.setUid("1");
        userResponseDTO.setEmail("johndoe@gmail.com");
        userResponseDTO.setUsername("johndoe");
        userResponseDTO.setFirstName("John");
        userResponseDTO.setLastName("Doe");
        userResponseDTO.setAddress("2 Waverly Ave");
        userResponseDTO.setCity("Newark");
        userResponseDTO.setState("NJ");
        userResponseDTO.setCountry("USA");
        userResponseDTO.setZipCode("10000");
        userResponseDTO.setStatus("online");
        userResponseDTO.setLastActivity(OffsetDateTime.of(2020, 1, 10, 10, 10, 10, 10, ZoneOffset.MIN));
        userResponseDTO.setPhoneNumber("000-000-0000");
        userResponseDTO.setPhotoUrl("url");

        return userResponseDTO;
    }

    private UserPersonalInfo getTestUserPersonalInfo() {
        UserPersonalInfo personalInfo = new UserPersonalInfo();
        personalInfo.setFirstName("John");
        personalInfo.setLastName("Doe");
        personalInfo.setAddress("2 Waverly Ave");
        personalInfo.setCity("Newark");
        personalInfo.setState("NJ");
        personalInfo.setCountry("USA");
        personalInfo.setZipCode("10000");

        return personalInfo;
    }
}



