package com.locadora_rdt_backend.modules.identity.users.service;

import com.locadora_rdt_backend.modules.identity.users.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface UserService {

    Page<UserDTO> findAllPaged(String name, PageRequest pageRequest);

    UserDetailsDTO findById(Long id);

    UserDTO insert(UserInsertDTO dto);

    UserDTO update(Long id, UserUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    UserPhotoDTO getUserPhotoById(Long id);

}
