package com.locadora_rdt_backend.modules.identity.users.service;

import com.locadora_rdt_backend.modules.identity.users.dto.ChangePasswordDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserMeUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserPhotoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {

    UserDTO getMe(Authentication authentication);

    void changePassword(Authentication authentication, ChangePasswordDTO dto);

    UserDTO updateMe(Authentication authentication, UserMeUpdateDTO dto);

    void updateMyPhoto(Authentication authentication, MultipartFile file);

    UserPhotoDTO getMyPhoto(Authentication authentication);
}
