package com.locadora_rdt_backend.modules.users.service;

import com.locadora_rdt_backend.modules.identity.users.dto.ChangePasswordDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserMeUpdateDTO;
import com.locadora_rdt_backend.modules.identity.users.dto.UserPhotoDTO;
import com.locadora_rdt_backend.modules.identity.users.mapper.UserMapper;
import com.locadora_rdt_backend.modules.identity.users.model.Address;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.identity.users.service.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTests {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper mapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserProfileServiceImpl service;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setPassword("senha-criptografada");
        user.setActive(true);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Joao");

        when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void getMeShouldReturnAuthenticatedUser() {
        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(userDTO);

        UserDTO resultado = service.getMe(authentication);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Joao", resultado.getName());
    }

    @Test
    void getMeShouldThrowExceptionWhenUserDoesNotExist() {
        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(null);

        assertThrows(AccessDeniedException.class, () -> service.getMe(authentication));
    }

    @Test
    void changePasswordShouldSaveNewPassword() {
        ChangePasswordDTO dto = new ChangePasswordDTO("senha-atual", "nova-senha");

        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);
        when(passwordEncoder.matches("senha-atual", "senha-criptografada")).thenReturn(true);
        when(passwordEncoder.matches("nova-senha", "senha-criptografada")).thenReturn(false);
        when(passwordEncoder.encode("nova-senha")).thenReturn("nova-senha-criptografada");

        service.changePassword(authentication, dto);

        assertEquals("nova-senha-criptografada", user.getPassword());
        verify(repository).save(user);
    }

    @Test
    void changePasswordShouldThrowExceptionWhenCurrentPasswordIsWrong() {
        ChangePasswordDTO dto = new ChangePasswordDTO("senha-incorreta", "nova-senha");

        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);
        when(passwordEncoder.matches("senha-incorreta", "senha-criptografada")).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.changePassword(authentication, dto));

        verify(repository, never()).save(user);
    }

    @Test
    void updateMeShouldUpdateAuthenticatedUser() {
        Address address = new Address();
        address.setCity("Sao Paulo");
        UserMeUpdateDTO dto = new UserMeUpdateDTO(
                "Joao Silva",
                "joao@email.com",
                "11999999999",
                address
        );

        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);
        when(repository.save(user)).thenReturn(user);
        when(mapper.toDTO(user)).thenReturn(userDTO);

        UserDTO resultado = service.updateMe(authentication, dto);

        assertEquals(userDTO, resultado);
        assertEquals("Joao Silva", user.getName());
        assertEquals("11999999999", user.getTelephone());
        assertEquals(address, user.getAddress());
        verify(repository).save(user);
    }

    @Test
    void updateMeShouldThrowExceptionWhenDataIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateMe(authentication, null));

        verify(repository, never()).save(user);
    }

    @Test
    void updateMyPhotoShouldSavePhoto() {
        byte[] photo = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                photo
        );

        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        service.updateMyPhoto(authentication, file);

        assertArrayEquals(photo, user.getPhoto());
        assertEquals("image/png", user.getPhotoContentType());
        verify(repository).save(user);
    }

    @Test
    void updateMyPhotoShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "foto.png",
                "image/png",
                new byte[0]
        );

        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        assertThrows(IllegalArgumentException.class,
                () -> service.updateMyPhoto(authentication, file));

        verify(repository, never()).save(user);
    }

    @Test
    void getMyPhotoShouldReturnPhoto() {
        byte[] photo = new byte[]{1, 2, 3};
        user.setPhoto(photo);
        user.setPhotoContentType("image/png");

        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        UserPhotoDTO resultado = service.getMyPhoto(authentication);

        assertNotNull(resultado);
        assertArrayEquals(photo, resultado.getPhoto());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getMyPhotoShouldReturnNullWhenUserDoesNotHavePhoto() {
        when(authentication.getName()).thenReturn("joao@email.com");
        when(repository.findByEmail("joao@email.com")).thenReturn(user);

        UserPhotoDTO resultado = service.getMyPhoto(authentication);

        assertNull(resultado);
    }
}
