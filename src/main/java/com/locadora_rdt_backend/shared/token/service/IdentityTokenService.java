package com.locadora_rdt_backend.shared.token.service;

import com.locadora_rdt_backend.shared.token.constants.TokenConstants;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class IdentityTokenService {

    public String generateToken() {

        byte[] tokenBytes = new byte[TokenConstants.TOKEN_BYTE_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        return token;
    }
}
