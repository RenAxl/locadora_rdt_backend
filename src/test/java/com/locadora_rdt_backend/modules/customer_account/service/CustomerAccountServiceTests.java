package com.locadora_rdt_backend.modules.customer_account.service;

import com.locadora_rdt_backend.modules.identity.account_activation.model.AccountActivation;
import com.locadora_rdt_backend.modules.identity.account_activation.repository.AccountActivationRepository;
import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountCreatePasswordDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountRegistrationDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountResendDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.event.CustomerAccountActivationEvent;
import com.locadora_rdt_backend.modules.identity.customer_account.mapper.CustomerAccountMapper;
import com.locadora_rdt_backend.modules.identity.customer_account.service.CustomerAccountServiceImpl;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import com.locadora_rdt_backend.modules.identity.roles.repository.RoleRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.shared.token.service.IdentityTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerAccountServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountActivationRepository tokenRepository;

    @Mock
    private IdentityTokenService identityTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomerAccountMapper mapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CustomerAccountServiceImpl service;

    private User user;
    private Customer customer;
    private Role role;
    private CustomerAccountRegistrationDTO registrationDTO;
    private CustomerAccountCreatePasswordDTO passwordDTO;
    private CustomerAccountResendDTO resendDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setActive(false);

        customer = new Customer();
        customer.setName("Joao");

        role = new Role();
        role.setAuthority("ROLE_CLIENTE");

        registrationDTO = new CustomerAccountRegistrationDTO();
        registrationDTO.setEmail(" JOAO@email.com ");
        registrationDTO.setCpf("123.456.789-00");

        passwordDTO = new CustomerAccountCreatePasswordDTO();
        passwordDTO.setPassword("nova-senha");
        passwordDTO.setPasswordConfirmation("nova-senha");

        resendDTO = new CustomerAccountResendDTO();
        resendDTO.setEmail(" JOAO@email.com ");

        ReflectionTestUtils.setField(service, "tokenMinutes", 30L);
    }

    @Test
    void registerShouldSaveCustomerAndUserAndPublishActivation() {
        user.setPassword("senha-anterior");
        user.setActive(true);

        when(roleRepository.findAll()).thenReturn(Collections.singletonList(role));
        when(mapper.toCustomer(registrationDTO)).thenReturn(customer);
        when(mapper.toUser(registrationDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(identityTokenService.generateToken()).thenReturn("token-gerado");

        service.register(registrationDTO);

        verify(customerRepository).existsByEmail("joao@email.com");
        verify(customerRepository).existsByCpf("12345678900");
        verify(customerRepository).save(customer);
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(AccountActivation.class));
        verify(eventPublisher).publishEvent(any(CustomerAccountActivationEvent.class));
        assertNull(user.getPassword());
        assertFalse(user.getActive());
        assertTrue(user.getRoles().contains(role));
        assertEquals(CustomerAccountConstants.CUSTOMER_REGISTRATION_AUDIT, user.getCreatedBy());
        assertEquals(CustomerAccountConstants.CUSTOMER_REGISTRATION_AUDIT, customer.getCreatedBy());
    }

    @Test
    void registerShouldThrowExceptionWhenUserEmailAlreadyExists() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.register(registrationDTO));

        assertEquals(CustomerAccountConstants.EMAIL_ALREADY_REGISTERED_AS_USER, exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowExceptionWhenCustomerEmailAlreadyExists() {
        when(customerRepository.existsByEmail("joao@email.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.register(registrationDTO));

        assertEquals(CustomerAccountConstants.EMAIL_ALREADY_REGISTERED_AS_CUSTOMER, exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowExceptionWhenCpfAlreadyExists() {
        when(customerRepository.existsByCpf("12345678900")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.register(registrationDTO));

        assertEquals(CustomerAccountConstants.CPF_ALREADY_REGISTERED_AS_CUSTOMER, exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerShouldThrowExceptionWhenCustomerRoleDoesNotExist() {
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.register(registrationDTO));

        assertEquals(CustomerAccountConstants.CUSTOMER_ROLE_NOT_FOUND, exception.getMessage());
        verify(customerRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createPasswordShouldSavePasswordAndActivateUserAndDeleteToken() {
        AccountActivation token = new AccountActivation();
        token.setUser(user);

        when(tokenRepository.findByTokenAndExpirationAfter(eq("token-valido"), any(Instant.class)))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("nova-senha")).thenReturn("senha-criptografada");

        service.createPassword("token-valido", passwordDTO);

        assertEquals("senha-criptografada", user.getPassword());
        assertTrue(user.getActive());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void createPasswordShouldThrowExceptionWhenTokenIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createPassword(null, passwordDTO));

        assertEquals(CustomerAccountConstants.INVALID_TOKEN, exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void createPasswordShouldThrowExceptionWhenTokenIsBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createPassword("   ", passwordDTO));

        assertEquals(CustomerAccountConstants.INVALID_TOKEN, exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void createPasswordShouldThrowExceptionWhenPasswordsDoNotMatch() {
        passwordDTO.setPasswordConfirmation("outra-senha");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createPassword("token-valido", passwordDTO));

        assertEquals(CustomerAccountConstants.PASSWORDS_DO_NOT_MATCH, exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void createPasswordShouldThrowExceptionWhenTokenIsInvalidExpiredOrUsed() {
        when(tokenRepository.findByTokenAndExpirationAfter(eq("token-invalido"), any(Instant.class)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createPassword("token-invalido", passwordDTO));

        assertEquals(CustomerAccountConstants.INVALID_EXPIRED_OR_USED_TOKEN, exception.getMessage());
        assertFalse(user.getActive());
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resendActivationShouldReplaceTokenAndPublishActivation() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);
        when(identityTokenService.generateToken()).thenReturn("novo-token");

        service.resendActivation(resendDTO);

        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(AccountActivation.class));
        verify(eventPublisher).publishEvent(any(CustomerAccountActivationEvent.class));
        assertFalse(user.getActive());
    }

    @Test
    void resendActivationShouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.resendActivation(resendDTO));

        assertEquals(CustomerAccountConstants.USER_NOT_FOUND, exception.getMessage());
        verify(tokenRepository, never()).deleteByUserId(any());
        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(CustomerAccountActivationEvent.class));
    }

    @Test
    void resendActivationShouldThrowExceptionWhenAccountIsAlreadyActive() {
        user.setActive(true);
        when(userRepository.findByEmail("joao@email.com")).thenReturn(user);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.resendActivation(resendDTO));

        assertEquals(CustomerAccountConstants.ACCOUNT_ALREADY_ACTIVE, exception.getMessage());
        verify(tokenRepository, never()).deleteByUserId(any());
        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(CustomerAccountActivationEvent.class));
    }
}
