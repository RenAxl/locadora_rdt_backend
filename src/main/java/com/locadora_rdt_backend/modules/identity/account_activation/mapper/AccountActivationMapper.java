package com.locadora_rdt_backend.modules.identity.account_activation.mapper;

import com.locadora_rdt_backend.modules.identity.account_activation.model.AccountActivation;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccountActivationMapper {

    public AccountActivationMapper() {
    }

    public AccountActivation toEntity(User user, String token, Instant expiration) {

        AccountActivation entity = new AccountActivation();

        entity.setToken(token);
        entity.setUser(user);
        entity.setExpiration(expiration);

        return entity;
    }

}
