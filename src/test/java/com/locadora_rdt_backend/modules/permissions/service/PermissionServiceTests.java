package com.locadora_rdt_backend.modules.permissions.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;
import com.locadora_rdt_backend.modules.identity.permissions.model.Permission;
import com.locadora_rdt_backend.modules.identity.permissions.repository.PermissionRepository;
import com.locadora_rdt_backend.modules.identity.permissions.service.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceTests {

    @Mock
    private PermissionRepository repository;

    @InjectMocks
    private PermissionServiceImpl service;

    private Permission permission;

    @BeforeEach
    void setUp() {
        permission = new Permission(1L, "USER_READ", "Usuários");
    }

    @Test
    void findAllShouldReturnPermissions() {
        when(repository.findAllByOrderByGroupNameAscNameAsc())
                .thenReturn(Collections.singletonList(permission));
        when(repository.findByGroupNameIgnoreCaseOrderByNameAsc("Usuários"))
                .thenReturn(Collections.singletonList(permission));

        List<PermissionDTO> todasPermissoes = service.findAll(null);
        List<PermissionDTO> permissoesDoGrupo = service.findAll(" Usuários ");

        assertEquals(1, todasPermissoes.size());
        assertEquals("USER_READ", todasPermissoes.get(0).getName());
        assertEquals(1, permissoesDoGrupo.size());
        assertEquals("Usuários", permissoesDoGrupo.get(0).getGroupName());
    }

    @Test
    void findAllShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findAllByOrderByGroupNameAscNameAsc())
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findAll(null));
    }

    @Test
    void findAllGroupNamesShouldReturnGroupNames() {
        when(repository.findDistinctGroupNames())
                .thenReturn(Arrays.asList("Clientes", "Usuários"));

        List<String> resultado = service.findAllGroupNames();

        assertEquals(2, resultado.size());
        assertEquals("Clientes", resultado.get(0));
        assertEquals("Usuários", resultado.get(1));
    }

    @Test
    void findAllGroupNamesShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findDistinctGroupNames())
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.findAllGroupNames());
    }

    @Test
    void findEntityByIdShouldReturnPermission() {
        when(repository.findById(1L)).thenReturn(Optional.of(permission));

        Permission resultado = service.findEntityById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("USER_READ", resultado.getName());
    }

    @Test
    void findEntityByIdShouldThrowExceptionWhenPermissionDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findEntityById(1L));
    }
}
