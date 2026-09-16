package com.locadora_rdt_backend.modules.identity.account_activation.service;

import com.locadora_rdt_backend.infrastructure.mail.service.EmailService;
import com.locadora_rdt_backend.infrastructure.mail.template.ActivationEmailTemplate;
import com.locadora_rdt_backend.modules.identity.account_activation.constants.AccountActivationConstants;
import com.locadora_rdt_backend.modules.identity.account_activation.dto.AccountActivationDTO;
import com.locadora_rdt_backend.modules.identity.account_activation.mapper.AccountActivationMapper;
import com.locadora_rdt_backend.modules.identity.account_activation.model.AccountActivation;
import com.locadora_rdt_backend.modules.identity.account_activation.repository.AccountActivationRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class AccountActivationServiceImpl implements AccountActivationService {

    private final AccountActivationRepository repository;
    private final AccountActivationMapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ActivationEmailTemplate templateService;

    @Value(AccountActivationConstants.FRONTEND_BASE_URL_PROPERTY)
    private String frontendBaseUrl;

    @Value(AccountActivationConstants.TOKEN_MINUTES_PROPERTY)
    private long tokenMinutes;

    public AccountActivationServiceImpl(
            AccountActivationRepository repository,
            AccountActivationMapper mapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            ActivationEmailTemplate templateService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.templateService = templateService;
    }

    @Override
    @Transactional
    public void createActivationTokenAndSendEmail(User user) {

        byte[] tokenBytes = new byte[AccountActivationConstants.TOKEN_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        Instant expiration = Instant.now().plus(tokenMinutes, ChronoUnit.MINUTES);

        repository.deleteByUserId(user.getId());

        AccountActivation accountActivation = mapper.toEntity(user, token, expiration);

        repository.save(accountActivation);

        String link = UriComponentsBuilder
                .fromHttpUrl(frontendBaseUrl)
                .path(AccountActivationConstants.ACTIVATION_PATH)
                .queryParam(AccountActivationConstants.TOKEN_QUERY_PARAM, token)
                .toUriString();

        String html = templateService.buildTemplate(user.getName(), link, tokenMinutes);

        emailService.sendHtmlEmail(user.getEmail(), AccountActivationConstants.ACTIVATION_EMAIL_SUBJECT, html);
    }

    @Override
    @Transactional
    public void activateAccount(String token, AccountActivationDTO dto) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(AccountActivationConstants.INVALID_TOKEN);
        }

        if (dto == null || dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException(AccountActivationConstants.INVALID_PASSWORD);
        }

        Optional<AccountActivation> accountActivationOptional = repository.findByTokenAndExpirationAfter(
                token,
                Instant.now()
        );

        if (!accountActivationOptional.isPresent()) {
            throw new IllegalArgumentException(AccountActivationConstants.INVALID_OR_EXPIRED_TOKEN);
        }

        AccountActivation accountActivation = accountActivationOptional.get();

        User user = accountActivation.getUser();

        String password = passwordEncoder.encode(dto.getPassword());

        user.setPassword(password);
        user.setActive(true);

        userRepository.save(user);

        repository.delete(accountActivation);
    }

}
