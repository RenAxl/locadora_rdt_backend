package com.locadora_rdt_backend.modules.identity.users.mapper;

import com.locadora_rdt_backend.modules.identity.users.dto.UserDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDetailsDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserInsertDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserMapper() {
    }

    public UserDTO toDTO(User entity) {

        UserDTO dto = new UserDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());

        dto.setActive(entity.getActive());

        dto.setTelephone(entity.getTelephone());
        dto.setAddress(entity.getAddress());

        dto.setPhotoContentType(entity.getPhotoContentType());

        return dto;
    }

    public UserDetailsDTO toDetailsDTO(User entity) {

        UserDetailsDTO dto = new UserDetailsDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());

        dto.setActive(entity.getActive());

        dto.setTelephone(entity.getTelephone());
        dto.setAddress(entity.getAddress());

        dto.setPhotoContentType(entity.getPhotoContentType());

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public User toEntity(UserInsertDTO dto) {

        User entity = new User();

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setTelephone(dto.getTelephone());
        entity.setAddress(dto.getAddress());

        entity.setActive(true);

        return entity;
    }

    public void updateEntity(User entity, UserUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());

        entity.setTelephone(dto.getTelephone());
        entity.setAddress(dto.getAddress());

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }
}

