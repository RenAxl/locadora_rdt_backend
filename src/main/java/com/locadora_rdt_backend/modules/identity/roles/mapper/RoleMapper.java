package com.locadora_rdt_backend.modules.identity.roles.mapper;

import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDetailsDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleInsertDTO;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public RoleDTO toDTO(Role entity) {

        RoleDTO dto = new RoleDTO();

        dto.setId(entity.getId());
        dto.setAuthority(entity.getAuthority());

        dto.setPermissionsCount(
                (long) entity.getPermissions().size()
        );

        dto.setPermissions(
                entity.getPermissions()
                        .stream()
                        .map(permission -> new PermissionDTO(
                                permission.getId(),
                                permission.getName(),
                                permission.getGroupName()
                        ))
                        .collect(Collectors.toList())
        );

        return dto;
    }

    // Método com o mesmo nome sobrecarga
    public RoleDTO toDTO(Role entity, Long permissionsCount) {

        RoleDTO dto = new RoleDTO();

        dto.setId(entity.getId());
        dto.setAuthority(entity.getAuthority());
        dto.setPermissionsCount(permissionsCount);

        return dto;
    }

    public RoleDetailsDTO toDetailsDTO(Role entity) {

        RoleDetailsDTO dto = new RoleDetailsDTO();

        dto.setId(entity.getId());
        dto.setAuthority(entity.getAuthority());
        dto.setPermissionsCount((long) entity.getPermissions().size());

        dto.setPermissions(
                entity.getPermissions()
                        .stream()
                        .map(permission -> new PermissionDTO(
                                permission.getId(),
                                permission.getName(),
                                permission.getGroupName()
                        ))
                        .collect(Collectors.toList())
        );

        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Role toEntity(RoleInsertDTO dto) {

        Role entity = new Role();

        entity.setAuthority(dto.getAuthority());

        return entity;
    }
}
