package com.locadora_rdt_backend.modules.identity.password_recovery.mapper;

import com.locadora_rdt_backend.modules.identity.password_recovery.model.PasswordRecoveryToken;
import com.locadora_rdt_backend.modules.identity.password_recovery.model.TokenType;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PasswordRecoveryMapper {

    public PasswordRecoveryMapper() {
    }

    public PasswordRecoveryToken toEntity(User user, String token, Instant expiration) {

        PasswordRecoveryToken entity = new PasswordRecoveryToken();

        entity.setToken(token);
        entity.setExpiration(expiration);
        entity.setType(TokenType.PASSWORD_RESET);
        entity.setUser(user);

        return entity;
    }
}
