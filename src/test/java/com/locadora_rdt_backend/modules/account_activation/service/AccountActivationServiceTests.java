package com.locadora_rdt_backend.modules.account_activation.service;

import com.locadora_rdt_backend.infrastructure.mail.service.EmailService;
import com.locadora_rdt_backend.infrastructure.mail.template.ActivationEmailTemplate;
import com.locadora_rdt_backend.modules.identity.account_activation.mapper.AccountActivationMapper;
import com.locadora_rdt_backend.modules.identity.account_activation.model.AccountActivation;
import com.locadora_rdt_backend.modules.identity.account_activation.repository.AccountActivationRepository;
import com.locadora_rdt_backend.modules.identity.account_activation.service.AccountActivationServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountActivationServiceTests {

    @Mock
    private AccountActivationRepository repository;

    @Mock
    private AccountActivationMapper mapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private ActivationEmailTemplate templateService;

    @Mock
    private IdentityTokenService identityTokenService;

    @InjectMocks
    private AccountActivationServiceImpl service;

    private User user;
    private AccountActivation accountActivation;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendBaseUrl", "https://locadora.example");
        ReflectionTestUtils.setField(service, "tokenMinutes", 30L);

        user = new User();
        user.setId(1L);
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setActive(false);

        accountActivation = new AccountActivation();
        accountActivation.setId(1L);
        accountActivation.setUser(user);
    }

    @Test
    void createActivationTokenAndSendEmailShouldSaveTokenAndSendEmail() {
        String link = "https://locadora.example/activate?token=token-ativacao";

        when(identityTokenService.generateToken()).thenReturn("token-ativacao");
        when(mapper.toEntity(eq(user), eq("token-ativacao"), any(Instant.class)))
                .thenReturn(accountActivation);
        when(templateService.buildTemplate(user.getName(), link, 30L)).thenReturn("html");

        service.createActivationTokenAndSendEmail(user);

        verify(repository).deleteByUserId(user.getId());
        verify(repository).save(accountActivation);
        verify(emailService).sendHtmlEmail(user.getEmail(), "Ative sua conta - Locadora RDT", "html");
    }

    @Test
    void createActivationTokenAndSendEmailShouldThrowExceptionWhenRepositoryFails() {
        when(identityTokenService.generateToken()).thenReturn("token-ativacao");
        when(mapper.toEntity(eq(user), eq("token-ativacao"), any(Instant.class)))
                .thenReturn(accountActivation);
        when(repository.save(accountActivation))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.createActivationTokenAndSendEmail(user));

        verify(repository).deleteByUserId(user.getId());
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
    }

}
