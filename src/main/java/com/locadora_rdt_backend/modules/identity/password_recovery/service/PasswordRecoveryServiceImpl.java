package com.locadora_rdt_backend.modules.identity.password_recovery.service;

import com.locadora_rdt_backend.infrastructure.mail.service.EmailService;
import com.locadora_rdt_backend.infrastructure.mail.template.PasswordRecoveryEmailTemplate;
import com.locadora_rdt_backend.modules.identity.password_recovery.constants.PasswordRecoveryConstants;
import com.locadora_rdt_backend.modules.identity.password_recovery.dto.ForgotPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.dto.NewPasswordDTO;
import com.locadora_rdt_backend.modules.identity.password_recovery.mapper.PasswordRecoveryMapper;
import com.locadora_rdt_backend.modules.identity.password_recovery.model.PasswordRecoveryToken;
import com.locadora_rdt_backend.modules.identity.password_recovery.model.TokenType;
import com.locadora_rdt_backend.modules.identity.password_recovery.repository.PasswordRecoveryRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.shared.token.service.IdentityTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final PasswordRecoveryRepository repository;
    private final PasswordRecoveryMapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordRecoveryEmailTemplate templateService;
    private final IdentityTokenService identityTokenService;

    @Value(PasswordRecoveryConstants.FRONTEND_BASE_URL_PROPERTY)
    private String frontendBaseUrl;

    @Value(PasswordRecoveryConstants.TOKEN_MINUTES_PROPERTY)
    private long tokenMinutes;

    public PasswordRecoveryServiceImpl(
            PasswordRecoveryRepository repository,
            PasswordRecoveryMapper mapper,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            PasswordRecoveryEmailTemplate templateService,
            IdentityTokenService identityTokenService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.templateService = templateService;
        this.identityTokenService = identityTokenService;
    }

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordDTO dto) {

        if (dto == null
                || dto.getEmail() == null
                || dto.getEmail().isBlank()) {
            return;
        }

        String email = dto.getEmail().trim();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            return;
        }

        if (!user.isActive()) {
            return;
        }

        repository.deleteByUserIdAndType(user.getId(), TokenType.PASSWORD_RESET);

        String token = identityTokenService.generateToken();
        Instant expiration = Instant.now().plus(tokenMinutes, ChronoUnit.MINUTES);

        PasswordRecoveryToken entity = mapper.toEntity(user, token, expiration);

        repository.save(entity);

        String link = UriComponentsBuilder
                .fromHttpUrl(frontendBaseUrl)
                .path(PasswordRecoveryConstants.PASSWORD_RESET_PATH)
                .queryParam(PasswordRecoveryConstants.TOKEN_QUERY_PARAM, token)
                .toUriString();

        String html = templateService.buildTemplate(user.getName(), link, tokenMinutes);

        emailService.sendHtmlEmail(
                user.getEmail(),
                PasswordRecoveryConstants.PASSWORD_RESET_EMAIL_SUBJECT,
                html
        );
    }

    @Override
    @Transactional
    public void resetPassword(String token, NewPasswordDTO dto) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(PasswordRecoveryConstants.INVALID_TOKEN);
        }

        if (dto == null
                || dto.getPassword() == null
                || dto.getPassword().isBlank()) {

            throw new IllegalArgumentException(PasswordRecoveryConstants.INVALID_PASSWORD);
        }

        Optional<PasswordRecoveryToken> tokenOptional = repository.findByTokenAndTypeAndExpirationAfter(
                token,
                TokenType.PASSWORD_RESET,
                Instant.now()
        );

        if (!tokenOptional.isPresent()) {
            throw new IllegalArgumentException(PasswordRecoveryConstants.INVALID_OR_EXPIRED_TOKEN);
        }

        PasswordRecoveryToken entity = tokenOptional.get();
        User user = entity.getUser();

        String password = dto.getPassword();
        String currentPassword = user.getPassword();

        if (currentPassword != null && passwordEncoder.matches(password, currentPassword)) {
            throw new IllegalArgumentException(PasswordRecoveryConstants.PASSWORD_EQUALS_CURRENT);
        }

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);

        userRepository.save(user);

        repository.delete(entity);
    }
}
