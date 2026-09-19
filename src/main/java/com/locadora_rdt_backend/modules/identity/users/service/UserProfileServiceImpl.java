package com.locadora_rdt_backend.modules.identity.users.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.modules.identity.users.constants.UserConstants;
import com.locadora_rdt_backend.modules.identity.users.dto.ChangePasswordDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserMeUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserPhotoDTO;
import com.locadora_rdt_backend.modules.identity.users.mapper.UserMapper;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserProfileServiceImpl(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            UserMapper mapper
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getMe(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        String email = authentication.getName();

        User user = repository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        UserDTO userDTO = mapper.toDTO(user);

        return userDTO;
    }

    @Override
    @Transactional
    public void changePassword(Authentication authentication, ChangePasswordDTO dto) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        if (dto == null) {
            throw new IllegalArgumentException(UserConstants.INVALID_DATA);
        }

        String email = authentication.getName();

        User user = repository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        String currentPassword = dto.getCurrentPassword();
        String newPassword = dto.getNewPassword();
        String savedPassword = user.getPassword();

        boolean currentPasswordMatches = passwordEncoder.matches(currentPassword, savedPassword);

        if (!currentPasswordMatches) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        boolean newPasswordMatches = passwordEncoder.matches(newPassword, savedPassword);

        if (newPasswordMatches) {
            throw new IllegalArgumentException(UserConstants.PASSWORD_EQUALS_CURRENT);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);

        repository.save(user);
    }

    @Override
    @Transactional
    public UserDTO updateMe(Authentication authentication, UserMeUpdateDTO dto) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        if (dto == null) {
            throw new IllegalArgumentException(UserConstants.INVALID_DATA);
        }

        String email = authentication.getName();

        User user = repository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        String newEmail = dto.getEmail();

        if (newEmail != null && !newEmail.equalsIgnoreCase(user.getEmail())) {
            User userWithEmail = repository.findByEmail(newEmail);

            if (userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
                throw new IllegalArgumentException(UserConstants.INVALID_DATA);
            }

            user.setEmail(newEmail);
        }

        user.setName(dto.getName());
        user.setTelephone(dto.getTelephone());
        user.setAddress(dto.getAddress());

        User savedUser = repository.save(user);

        UserDTO userDTO = mapper.toDTO(savedUser);

        return userDTO;
    }

    @Override
    @Transactional
    public void updateMyPhoto(Authentication authentication, MultipartFile file) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        String email = authentication.getName();

        User user = repository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(UserConstants.EMPTY_PHOTO_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !UserConstants.ALLOWED_PHOTO_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(UserConstants.INVALID_PHOTO_TYPE);
        }

        if (file.getSize() > UserConstants.MAX_PHOTO_SIZE_BYTES) {
            throw new IllegalArgumentException(UserConstants.PHOTO_TOO_LARGE);
        }

        try {

            byte[] photo = file.getBytes();

            user.setPhoto(photo);
            user.setPhotoContentType(contentType);

        } catch (IOException e) {

            throw new FileException(UserConstants.PHOTO_PROCESSING_ERROR);
        }

        repository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPhotoDTO getMyPhoto(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        String email = authentication.getName();

        User user = repository.findByEmail(email);

        if (user == null) {
            throw new AccessDeniedException(UserConstants.ACCESS_DENIED);
        }

        byte[] photo = user.getPhoto();

        if (photo == null || photo.length == 0) {
            return null;
        }

        String photoContentType = user.getPhotoContentType();

        UserPhotoDTO userPhotoDTO = new UserPhotoDTO(
                photo,
                photoContentType
        );

        return userPhotoDTO;
    }

}
