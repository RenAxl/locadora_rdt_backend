package com.locadora_rdt_backend.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
public class JwtTokenStoreConfig {

    @Bean
    public JwtTokenStore tokenStore(JwtAccessTokenConverter accessTokenConverter) {
        return new JwtTokenStore(accessTokenConverter);
    }
}
