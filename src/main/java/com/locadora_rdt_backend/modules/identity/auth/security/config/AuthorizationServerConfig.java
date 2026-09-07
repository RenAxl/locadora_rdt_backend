package com.locadora_rdt_backend.modules.identity.auth.security.config;

import java.util.Arrays;

import com.locadora_rdt_backend.modules.identity.auth.security.jwt.JwtClaimsTokenEnhancer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;

import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;


@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Value("${OAUTH_CLIENT_ID}")
    private String clientId;

    @Value("${OAUTH_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${jwt.duration}")
    private Integer jwtDuration;

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtAccessTokenConverter accessTokenConverter;
    private final JwtTokenStore tokenStore;
    private final AuthenticationManager authenticationManager;
    private final JwtClaimsTokenEnhancer tokenEnhancer;

    public AuthorizationServerConfig(BCryptPasswordEncoder passwordEncoder,
                                     JwtAccessTokenConverter accessTokenConverter,
                                     JwtTokenStore tokenStore,
                                     AuthenticationManager authenticationManager,
                                     JwtClaimsTokenEnhancer tokenEnhancer) {
        this.passwordEncoder = passwordEncoder;
        this.accessTokenConverter = accessTokenConverter;
        this.tokenStore = tokenStore;
        this.authenticationManager = authenticationManager;
        this.tokenEnhancer = tokenEnhancer;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient(clientId)
                .secret(passwordEncoder.encode(clientSecret))
                .scopes("read", "write")
                .authorizedGrantTypes("password")
                .accessTokenValiditySeconds(jwtDuration);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        TokenEnhancerChain chain = new TokenEnhancerChain();
        chain.setTokenEnhancers(Arrays.asList(accessTokenConverter, tokenEnhancer));

        endpoints
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore)
                .accessTokenConverter(accessTokenConverter)
                .tokenEnhancer(chain);
    }
}

