package com.locadora_rdt_backend.modules.identity.account_activation.constants;

public final class AccountActivationConstants {

    // Configurações
    public static final String FRONTEND_BASE_URL_PROPERTY = "${app.frontend.base-url}";
    public static final String TOKEN_MINUTES_PROPERTY = "${app.activation.token-minutes:30}";
    public static final int TOKEN_BYTES = 32;

    // Link e e-mail
    public static final String ACTIVATION_PATH = "/activate";
    public static final String TOKEN_QUERY_PARAM = "token";
    public static final String ACTIVATION_EMAIL_SUBJECT = "Ative sua conta - Locadora RDT";

    // Mensagens de erro
    public static final String INVALID_TOKEN = "Token inválido";
    public static final String INVALID_PASSWORD = "Senha inválida";
    public static final String INVALID_OR_EXPIRED_TOKEN = "Token inválido ou expirado";

    private AccountActivationConstants() {
    }
}
