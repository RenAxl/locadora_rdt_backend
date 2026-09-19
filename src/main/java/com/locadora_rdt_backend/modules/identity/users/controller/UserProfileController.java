package com.locadora_rdt_backend.modules.identity.users.controller;

import com.locadora_rdt_backend.modules.identity.users.dto.ChangePasswordDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserMeUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserPhotoDTO;
import com.locadora_rdt_backend.modules.identity.users.service.UserProfileService;
import com.locadora_rdt_backend.shared.web.BinaryResponseBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/user-profile")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @PreAuthorize(USER_PROFILE_READ)
    @GetMapping(value = "/me")
    public ResponseEntity<UserDTO> getMe(Authentication authentication) {
        UserDTO dto = service.getMe(authentication);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize(USER_PROFILE_WRITE)
    @PutMapping(value = "/me/password")
    public ResponseEntity<Void> changePassword(Authentication authentication,
                                               @Valid @RequestBody ChangePasswordDTO dto) {
        service.changePassword(authentication, dto);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(USER_PROFILE_READ)
    @PutMapping(value = "/me")
    public ResponseEntity<UserDTO> updateMe(Authentication authentication,
                                            @Valid @RequestBody UserMeUpdateDTO dto) {
        UserDTO result = service.updateMe(authentication, dto);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize(USER_PROFILE_WRITE)
    @PutMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMyPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) {
        service.updateMyPhoto(authentication, file);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(USER_PROFILE_READ)
    @GetMapping(value = "/me/photo")
    public ResponseEntity<byte[]> getMyPhoto(Authentication authentication) {

        UserPhotoDTO dto = service.getMyPhoto(authentication);

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return BinaryResponseBuilder.noCacheMedia(dto.getPhoto(), dto.getContentType());
    }

}
