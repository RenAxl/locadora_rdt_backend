package com.locadora_rdt_backend.modules.identity.roles.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.modules.identity.roles.constants.RoleConstants;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDetailsDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleInsertDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RolePermissionsUpdateDTO;
import com.locadora_rdt_backend.modules.identity.permissions.model.Permission;
import com.locadora_rdt_backend.modules.identity.permissions.service.PermissionService;
import com.locadora_rdt_backend.modules.identity.roles.mapper.RoleMapper;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import com.locadora_rdt_backend.modules.identity.roles.repository.RoleRepository;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;
    private final PermissionService permissionService;
    private final RoleMapper mapper;

    public RoleServiceImpl(
            RoleRepository repository,
            PermissionService permissionService,
            RoleMapper mapper
    ) {
        this.repository = repository;
        this.permissionService = permissionService;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> findAllPaged(String authority, PageRequest pageRequest) {

        String search = authority == null ? "" : authority.trim();

        Page<Role> page = repository.findByAuthorityLikeIgnoreCase(search, pageRequest);

        List<Long> roleIds = page.getContent()
                .stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return page.map(role -> mapper.toDTO(role, RoleConstants.DEFAULT_PERMISSIONS_COUNT));
        }

        List<Object[]> rows = repository.countPermissionsByRoleIds(roleIds);

        Map<Long, Long> permissionsCountMap = rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return page.map(role -> mapper.toDTO(
                role,
                permissionsCountMap.getOrDefault(role.getId(), RoleConstants.DEFAULT_PERMISSIONS_COUNT)
        ));
    }


    @Override
    @Transactional(readOnly = true)
    public RoleDetailsDTO findById(Long id) {
        Role entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RoleConstants.ROLE_NOT_FOUND));

        return mapper.toDetailsDTO(entity);
    }

    @Override
    @Transactional
    public RoleDTO updateRolePermissions(Long roleId, RolePermissionsUpdateDTO dto) {

        try {

            Role role = repository.getOne(roleId);

            role.getPermissions().clear();

            for (Long permissionId : dto.getPermissionIds()) {

                Permission permission = permissionService.findEntityById(permissionId);

                role.getPermissions().add(permission);
            }

            role = repository.save(role);

            return mapper.toDTO(role);

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(
                    RoleConstants.ROLE_NOT_FOUND + RoleConstants.ID_SEPARATOR + roleId
            );

        } catch (DataAccessException e) {

            throw new DatabaseException(
                    RoleConstants.PERMISSIONS_UPDATE_ERROR
            );
        }
    }

    @Override
    @Transactional
    public RoleDTO insert(RoleInsertDTO dto) {
        Role entity = mapper.toEntity(dto);
        entity.setCreatedBy("Usuário Teste");
        entity = repository.save(entity);
        return mapper.toDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Role findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RoleConstants.ROLE_NOT_FOUND));
    }

}
