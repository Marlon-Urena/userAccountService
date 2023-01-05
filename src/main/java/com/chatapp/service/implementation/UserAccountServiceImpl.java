package com.chatapp.service.implementation;

import com.chatapp.dto.ContactDTO;
import com.chatapp.dto.UserPersonalInfo;
import com.chatapp.dto.UserResponseDTO;
import com.chatapp.exception.EmailExistsException;
import com.chatapp.exception.UserAccountNotFoundException;
import com.chatapp.exception.UsernameExistsException;
import com.chatapp.model.UserAccount;
import com.chatapp.repository.UserAccountRepository;
import com.chatapp.service.UserAccountService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.UpdateRequest;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAccountServiceImpl implements UserAccountService {
    private final UserAccountRepository repository;
    private final FirebaseAuth firebaseAuth;
    private final Bucket bucket;
    private final ModelMapper modelMapper;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository repository, FirebaseAuth firebaseAuth, Bucket bucket, ModelMapper modelMapper) {
        this.repository = repository;
        this.firebaseAuth = firebaseAuth;
        this.bucket = bucket;
        this.modelMapper = modelMapper;
        TypeMap<UserAccount, ContactDTO> propertyMapper = this.modelMapper.createTypeMap(UserAccount.class, ContactDTO.class);
        propertyMapper.addMapping(UserAccount::getPhotoUrl, ContactDTO::setAvatar);
    }

    // TODO: Create facade for wrapping bucket and firebase auth
    // TODO: Change Return type for all methods to be DTOs without ResponseEntity

    @Override
    public UserResponseDTO findUserAccount(String uid) {
        UserAccount user = repository
                .findById(uid)
                .orElseThrow(() -> new UserAccountNotFoundException(uid));

        return modelMapper.map(user, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO addUserAccount(UserAccount user) {
        if (repository.existsUserAccountByEmail(user.getEmail())) {
            throw new EmailExistsException(user.getEmail());
        }

        if (repository.existsUserAccountByUsername(user.getUsername())) {
            throw new UsernameExistsException(user.getUsername());
        }

        UserAccount newUser = repository.save(user);

        return modelMapper.map(newUser, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO updateUserAccount(UserAccount newUserDetails, String uid) {
        if (!repository.existsById(uid)) {
            throw new UserAccountNotFoundException(uid);
        }

        UserAccount updatedUserAccount = repository.save(newUserDetails);

        return modelMapper.map(updatedUserAccount, UserResponseDTO.class);
    }

    @Override
    public void deleteUserAccount(String userUID) {
        if (!repository.existsById(userUID)) {
            throw new UserAccountNotFoundException(userUID);
        }

        repository.deleteById(userUID);
    }

    @Override
    public UserResponseDTO updateUserPersonalInfo(
            UserPersonalInfo userPersonalInfo, String uid) {
        UserAccount updatedUserAccount =
                repository.findById(uid)
                        .map(userAccount -> {
                            userAccount.setFirstName(userPersonalInfo.getFirstName());
                            userAccount.setLastName(userPersonalInfo.getLastName());
                            userAccount.setAddress(userPersonalInfo.getAddress());
                            userAccount.setCity(userPersonalInfo.getCity());
                            userAccount.setState(userPersonalInfo.getState());
                            userAccount.setCountry(userPersonalInfo.getCountry());
                            userAccount.setZipCode(userPersonalInfo.getZipCode());

                            return repository.save(userAccount);
                        })
                        .orElseThrow(() -> new UserAccountNotFoundException(uid));

        return modelMapper.map(updatedUserAccount, UserResponseDTO.class);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !repository.existsUserAccountByUsername(username);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !repository.existsUserAccountByEmail(email);
    }

    @Override
    public UserResponseDTO updateEmail(String newEmail, String uid, UpdateRequest request) throws FirebaseAuthException {
        boolean emailExists = repository.existsUserAccountByEmail(newEmail);

        if (emailExists) {
            throw new EmailExistsException(newEmail);
        }

        UserAccount updatedUserAccount =
                repository.findById(uid)
                        .map(userAccount -> {
                            userAccount.setEmail(newEmail);
                            return repository.save(userAccount);
                        })
                        .orElseThrow(() -> new UserAccountNotFoundException(uid));

        firebaseAuth.updateUser(request);

        return modelMapper.map(updatedUserAccount, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO updateUsername(String newUsername, String uid, UpdateRequest request) throws FirebaseAuthException {
        boolean usernameExists = repository.existsUserAccountByUsername(newUsername);

        if (usernameExists) {
            throw new UsernameExistsException(newUsername);
        }

        UserAccount updatedUserAccount = repository.findById(uid)
                .map(userAccount -> {
                    userAccount.setUsername(newUsername);
                    return repository.save(userAccount);
                })
                .orElseThrow(() -> new UserAccountNotFoundException(uid));

        firebaseAuth.updateUser(request);

        return modelMapper.map(updatedUserAccount, UserResponseDTO.class);
    }

    @Override
    public UserResponseDTO updateProfilePhoto(MultipartFile photo, String uid) throws IOException {
        Blob blob = bucket.create(photo.getOriginalFilename(), photo.getInputStream(), photo.getContentType());
        String mediaUrl = buildMediaUrl(blob.getName(), blob.getName());

        UserAccount updatedUserAccount =
                repository.findById(uid)
                        .map((userAccount) -> {
                                    userAccount.setPhotoUrl(mediaUrl);
                                    return repository.save(userAccount);
                                })
                        .orElseThrow(() -> new UserAccountNotFoundException(uid));

        return modelMapper.map(updatedUserAccount, UserResponseDTO.class);
    }

    @Override
    public List<ContactDTO> findUserAccounts(String searchQuery, Pageable pageable) {
        Page<UserAccount> userAccounts;

        if (searchQuery == null || searchQuery.isEmpty()) {
            userAccounts = repository.findAll(pageable);
        } else {
            userAccounts = repository.searchByUsernameContainingIgnoreCase(searchQuery, pageable);
        }

        List<UserAccount> users = userAccounts.getContent();

        return users.stream()
                .map((user) -> modelMapper.map(user, ContactDTO.class))
                .collect(Collectors.toList());
    }

    private String buildMediaUrl(String bucketName, String blobName) {
        return String.format(
                "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, blobName);
    }
}
