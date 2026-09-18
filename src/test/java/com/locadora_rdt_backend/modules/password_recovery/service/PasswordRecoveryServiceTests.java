package com.locadora_rdt_backend.modules.password_recovery.service;

import com.locadora_rdt_backend.infrastructure.mail.service.EmailService;
import com.locadora_rdt_backend.infrastructure.mail.template.PasswordRecoveryEmailTemplate;
import com.locadora_rdt_backend.modules.identity.password_recovery.dto.ForgotPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.dto.NewPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.mapper.PasswordRecoveryMapper;
import com.locadora_rdt_backend.modules.identity.password_recovery.model.PasswordRecoveryToken;
import com.locadora_rdt_backend.modules.identity.password_recovery.model.TokenType;
import com.locadora_rdt_backend.modules.identity.password_recovery.repository.PasswordRecoveryRepository;
import com.locadora_rdt_backend.modules.identity.password_recovery.service.PasswordRecoveryServiceImpl;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.shared.token.service.IdentityTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordRecoveryServiceTests {

    @Mock
    private PasswordRecoveryRepository repository;

    @Mock
    private PasswordRecoveryMapper mapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordRecoveryEmailTemplate templateService;

    @Mock
    private IdentityTokenService identityTokenService;

    @InjectMocks
    private PasswordRecoveryServiceImpl service;

    private User user;
    private PasswordRecoveryToken recoveryToken;
    private ForgotPasswordDTO forgotPasswordDTO;
    private NewPasswordDTO newPasswordDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setActive(true);
        user.setPassword("senha-atual-codificada");

        recoveryToken = new PasswordRecoveryToken();
        recoveryToken.setToken("token-recuperacao");
        recoveryToken.setType(TokenType.PASSWORD_RESET);
        recoveryToken.setUser(user);

        forgotPasswordDTO = new ForgotPasswordDTO();
        forgotPasswordDTO.setEmail("joao@email.com");

        newPasswordDTO = new NewPasswordDTO();
        newPasswordDTO.setPassword("nova-senha");

        ReflectionTestUtils.setField(service, "frontendBaseUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(service, "tokenMinutes", 30L);
    }

    @Test
    void requestPasswordResetShouldSaveTokenAndSendEmail() {
        String link = "http://localhost:4200/password-recovery/password-reset?token=token-recuperacao";

        when(userRepository.findByEmail(forgotPasswordDTO.getEmail())).thenReturn(user);
        when(identityTokenService.generateToken()).thenReturn("token-recuperacao");
        when(mapper.toEntity(eq(user), eq("token-recuperacao"), any(Instant.class)))
                .thenReturn(recoveryToken);
        when(templateService.buildTemplate(user.getName(), link, 30L)).thenReturn("html");

        service.requestPasswordReset(forgotPasswordDTO);

        verify(repository).deleteByUserIdAndType(user.getId(), TokenType.PASSWORD_RESET);
        verify(repository).save(recoveryToken);
        verify(emailService).sendHtmlEmail(user.getEmail(), "Recuperação de senha - Locadora RDT", "html");
    }

    @Test
    void requestPasswordResetShouldThrowExceptionWhenRepositoryFails() {
        when(userRepository.findByEmail(forgotPasswordDTO.getEmail())).thenReturn(user);
        when(identityTokenService.generateToken()).thenReturn("token-recuperacao");
        when(mapper.toEntity(eq(user), eq("token-recuperacao"), any(Instant.class)))
                .thenReturn(recoveryToken);
        when(repository.save(recoveryToken))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.requestPasswordReset(forgotPasswordDTO));

        verify(emailService, never()).sendHtmlEmail(any(), any(), any());
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndDeleteToken() {
        when(repository.findByTokenAndTypeAndExpirationAfter(
                eq("token-recuperacao"), eq(TokenType.PASSWORD_RESET), any(Instant.class)))
                .thenReturn(Optional.of(recoveryToken));
        when(passwordEncoder.matches(newPasswordDTO.getPassword(), user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPasswordDTO.getPassword())).thenReturn("nova-senha-codificada");

        service.resetPassword("token-recuperacao", newPasswordDTO);

        assertEquals("nova-senha-codificada", user.getPassword());
        verify(userRepository).save(user);
        verify(repository).delete(recoveryToken);
    }

    @Test
    void resetPasswordShouldThrowExceptionWhenTokenIsInvalidOrExpired() {
        when(repository.findByTokenAndTypeAndExpirationAfter(
                eq("token-recuperacao"), eq(TokenType.PASSWORD_RESET), any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.resetPassword("token-recuperacao", newPasswordDTO));

        verify(userRepository, never()).save(any());
        verify(repository, never()).delete(any(PasswordRecoveryToken.class));
    }
}
