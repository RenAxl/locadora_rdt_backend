package com.locadora_rdt_backend.modules.identity.customer_account.service;

import com.locadora_rdt_backend.modules.identity.account_activation.model.AccountActivation;
import com.locadora_rdt_backend.modules.identity.account_activation.repository.AccountActivationRepository;
import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountCreatePasswordDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountRegistrationDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.dto.CustomerAccountResendDTO;
import com.locadora_rdt_backend.modules.identity.customer_account.event.CustomerAccountActivationEvent;
import com.locadora_rdt_backend.modules.identity.customer_account.mapper.CustomerAccountMapper;
import com.locadora_rdt_backend.modules.identity.roles.model.Role;
import com.locadora_rdt_backend.modules.identity.roles.repository.RoleRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.shared.token.service.IdentityTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerAccountServiceImpl implements CustomerAccountService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountActivationRepository tokenRepository;
    private final IdentityTokenService identityTokenService;
    private final PasswordEncoder passwordEncoder;
    private final CustomerAccountMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value(CustomerAccountConstants.TOKEN_MINUTES_PROPERTY)
    private long tokenMinutes;

    public CustomerAccountServiceImpl(
            CustomerRepository customerRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            AccountActivationRepository tokenRepository,
            IdentityTokenService identityTokenService,
            PasswordEncoder passwordEncoder,
            CustomerAccountMapper mapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
        this.identityTokenService = identityTokenService;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void register(CustomerAccountRegistrationDTO dto) {

        String email = dto.getEmail().trim().toLowerCase();
        String cpf = dto.getCpf().replaceAll(CustomerAccountConstants.NON_DIGIT_PATTERN, "");

        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException(CustomerAccountConstants.EMAIL_ALREADY_REGISTERED_AS_USER);
        }

        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(CustomerAccountConstants.EMAIL_ALREADY_REGISTERED_AS_CUSTOMER);
        }

        if (customerRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException(CustomerAccountConstants.CPF_ALREADY_REGISTERED_AS_CUSTOMER);
        }

        List<Role> roles = roleRepository.findAll();
        Role customerRole = null;

        for (Role role : roles) {
            if (CustomerAccountConstants.CUSTOMER_ROLE.equals(role.getAuthority())) {
                customerRole = role;
                break;
            }
        }

        if (customerRole == null) {
            throw new IllegalStateException(CustomerAccountConstants.CUSTOMER_ROLE_NOT_FOUND);
        }

        Customer customer = mapper.toCustomer(dto);

        customer.setCreatedBy(CustomerAccountConstants.CUSTOMER_REGISTRATION_AUDIT);

        customerRepository.save(customer);

        User user = mapper.toUser(dto);

        user.setPassword(null);
        user.setActive(false);
        user.setCreatedBy(CustomerAccountConstants.CUSTOMER_REGISTRATION_AUDIT);
        user.getRoles().add(customerRole);

        User savedUser = userRepository.save(user);

        createTokenAndPublishEvent(savedUser);
    }

    @Override
    @Transactional
    public void createPassword(String token, CustomerAccountCreatePasswordDTO dto) {

        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException(CustomerAccountConstants.INVALID_TOKEN);
        }

        if (!dto.getPassword().equals(dto.getPasswordConfirmation())) {
            throw new IllegalArgumentException(CustomerAccountConstants.PASSWORDS_DO_NOT_MATCH);
        }

        Optional<AccountActivation> tokenOptional = tokenRepository.findByTokenAndExpirationAfter(
                token,
                Instant.now()
        );

        if (!tokenOptional.isPresent()) {
            throw new IllegalArgumentException(CustomerAccountConstants.INVALID_EXPIRED_OR_USED_TOKEN);
        }

        AccountActivation tokenEntity = tokenOptional.get();

        User user = tokenEntity.getUser();

        String encodedPassword = passwordEncoder.encode(dto.getPassword());

        user.setPassword(encodedPassword);
        user.setActive(true);

        userRepository.save(user);

        tokenRepository.delete(tokenEntity);
    }

    @Override
    @Transactional
    public void resendActivation(CustomerAccountResendDTO dto) {

        String email = dto.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException(CustomerAccountConstants.USER_NOT_FOUND);
        }

        if (user.isActive()) {
            throw new IllegalArgumentException(CustomerAccountConstants.ACCOUNT_ALREADY_ACTIVE);
        }

        createTokenAndPublishEvent(user);
    }

    private void createTokenAndPublishEvent(User user) {

        tokenRepository.deleteByUserId(user.getId());

        String token = identityTokenService.generateToken();

        Instant expiration = Instant.now().plus(tokenMinutes, ChronoUnit.MINUTES);

        AccountActivation tokenEntity = new AccountActivation();
        tokenEntity.setToken(token);
        tokenEntity.setUser(user);
        tokenEntity.setExpiration(expiration);

        tokenRepository.save(tokenEntity);

        CustomerAccountActivationEvent event = new CustomerAccountActivationEvent(
                user.getName(),
                user.getEmail(),
                token
        );

        eventPublisher.publishEvent(event);
    }
}
