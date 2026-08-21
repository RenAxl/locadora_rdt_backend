package com.locadora_rdt_backend.modules.users.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDetailsDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserInsertDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserPhotoDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.mapper.UserMapper;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.identity.users.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setActive(true);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Joao");
    }

    @Test
    void findAllPagedShouldReturnPageOfUsers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<User> users = new PageImpl<>(Collections.singletonList(user));

        when(repository.find("Joao", pageRequest)).thenReturn(users);
        when(mapper.toDTO(user)).thenReturn(userDTO);

        Page<UserDTO> resultado = service.findAllPaged("Joao", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Joao", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Joao", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Joao", pageRequest));
    }

    @Test
    void findByIdShouldReturnUser() {
        UserDetailsDTO detailsDTO = new UserDetailsDTO();
        detailsDTO.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toDetailsDTO(user)).thenReturn(detailsDTO);

        UserDetailsDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenUserDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveUser() {
        UserInsertDTO insertDTO = new UserInsertDTO();
        user.setPassword("senha");

        when(mapper.toEntity(insertDTO)).thenReturn(user);
        when(repository.save(user)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(userDTO);

        UserDTO resultado = service.insert(insertDTO);

        assertEquals(userDTO, resultado);
        assertEquals(null, user.getPassword());
        assertFalse(user.getActive());
        assertEquals("Usuário Teste", user.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        UserInsertDTO insertDTO = new UserInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(user);
        when(repository.save(user))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateUser() {
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        when(repository.getOne(1L)).thenReturn(user);
        when(repository.save(user)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(userDTO);

        UserDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(user, updateDTO);
        assertEquals(userDTO, resultado);
        assertEquals("Usuário Teste", user.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenUserDoesNotExist() {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteUser() {
        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(1L);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    void deleteAllShouldDeleteAllUsers() {
        User segundoUsuario = new User();
        segundoUsuario.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(user, segundoUsuario));

        service.deleteAll(Arrays.asList(1L, 2L));

        verify(repository).deleteAllByIds(Arrays.asList(1L, 2L));
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAll(Collections.emptyList()));

        verify(repository, never()).deleteAllByIds(any());
    }

    @Test
    void changeActiveStatusShouldChangeStatus() {
        when(repository.updateActiveById(1L, false)).thenReturn(1);

        service.changeActiveStatus(1L, false);

        verify(repository).updateActiveById(1L, false);
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenDatabaseFails() {
        when(repository.updateActiveById(1L, false))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DatabaseException.class,
                () -> service.changeActiveStatus(1L, false));
    }

    @Test
    void getUserPhotoByIdShouldReturnPhoto() {
        byte[] foto = new byte[]{1, 2, 3};
        user.setPhoto(foto);
        user.setPhotoContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        UserPhotoDTO resultado = service.getUserPhotoById(1L);

        assertNotNull(resultado);
        assertArrayEquals(foto, resultado.getPhoto());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getUserPhotoByIdShouldThrowExceptionWhenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.getUserPhotoById(null));
    }
}
