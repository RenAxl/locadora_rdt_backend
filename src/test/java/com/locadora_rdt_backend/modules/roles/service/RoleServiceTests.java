package com.locadora_rdt_backend.modules.roles.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.identity.permissions.model.Permission;
import com.locadora_rdt_backend.modules.identity.permissions.service.PermissionService;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDetailsDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleInsertDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RolePermissionsUpdateDTO;
import com.locadora_rdt_backend.modules.identity.roles.mapper.RoleMapper;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import com.locadora_rdt_backend.modules.identity.roles.repository.RoleRepository;
import com.locadora_rdt_backend.modules.identity.roles.service.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTests {

    @Mock
    private RoleRepository repository;

    @Mock
    private PermissionService permissionService;

    @Mock
    private RoleMapper mapper;

    @InjectMocks
    private RoleServiceImpl service;

    private Role role;
    private RoleDTO roleDTO;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setAuthority("ROLE_ADMIN");

        roleDTO = new RoleDTO();
        roleDTO.setId(1L);
        roleDTO.setAuthority("ROLE_ADMIN");
        roleDTO.setPermissionsCount(2L);
    }

    @Test
    void findAllPagedShouldReturnPageOfRoles() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Role> roles = new PageImpl<>(Collections.singletonList(role));
        Object[] quantidadeDePermissoes = new Object[]{1L, 2L};

        when(repository.findByAuthorityLikeIgnoreCase("ROLE_ADMIN", pageRequest)).thenReturn(roles);
        when(repository.countPermissionsByRoleIds(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(quantidadeDePermissoes));
        when(mapper.toDTO(role, 2L)).thenReturn(roleDTO);

        Page<RoleDTO> resultado = service.findAllPaged(" ROLE_ADMIN ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("ROLE_ADMIN", resultado.getContent().get(0).getAuthority());
        assertEquals(2L, resultado.getContent().get(0).getPermissionsCount());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.findByAuthorityLikeIgnoreCase("ROLE_ADMIN", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("ROLE_ADMIN", pageRequest));
    }

    @Test
    void findByIdShouldReturnRole() {
        RoleDetailsDTO detailsDTO = new RoleDetailsDTO();
        detailsDTO.setId(1L);
        detailsDTO.setAuthority("ROLE_ADMIN");

        when(repository.findById(1L)).thenReturn(Optional.of(role));
        when(mapper.toDetailsDTO(role)).thenReturn(detailsDTO);

        RoleDetailsDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("ROLE_ADMIN", resultado.getAuthority());
    }

    @Test
    void findByIdShouldThrowExceptionWhenRoleDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void updateRolePermissionsShouldUpdatePermissions() {
        RolePermissionsUpdateDTO updateDTO = new RolePermissionsUpdateDTO();
        updateDTO.setPermissionIds(Collections.singletonList(10L));
        Permission permission = new Permission(10L, "USER_READ", "Usuários");

        when(repository.getOne(1L)).thenReturn(role);
        when(permissionService.findEntityById(10L)).thenReturn(permission);
        when(repository.save(role)).thenReturn(role);
        when(mapper.toDTO(role)).thenReturn(roleDTO);

        RoleDTO resultado = service.updateRolePermissions(1L, updateDTO);

        assertEquals(roleDTO, resultado);
        assertEquals(1, role.getPermissions().size());
        assertEquals(permission, role.getPermissions().iterator().next());
    }

    @Test
    void updateRolePermissionsShouldThrowExceptionWhenRoleDoesNotExist() {
        RolePermissionsUpdateDTO updateDTO = new RolePermissionsUpdateDTO();
        updateDTO.setPermissionIds(Collections.singletonList(10L));
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateRolePermissions(1L, updateDTO));
    }

    @Test
    void insertShouldSaveRole() {
        RoleInsertDTO insertDTO = new RoleInsertDTO();
        insertDTO.setAuthority("ROLE_ADMIN");

        when(mapper.toEntity(insertDTO)).thenReturn(role);
        when(repository.save(role)).thenReturn(role);
        when(mapper.toDTO(role)).thenReturn(roleDTO);

        RoleDTO resultado = service.insert(insertDTO);

        assertEquals(roleDTO, resultado);
        assertEquals("Usuário Teste", role.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        RoleInsertDTO insertDTO = new RoleInsertDTO();
        when(mapper.toEntity(insertDTO)).thenReturn(role);
        when(repository.save(role))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void findEntityByIdShouldReturnRole() {
        when(repository.findById(1L)).thenReturn(Optional.of(role));

        Role resultado = service.findEntityById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("ROLE_ADMIN", resultado.getAuthority());
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenRoleDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }
}
