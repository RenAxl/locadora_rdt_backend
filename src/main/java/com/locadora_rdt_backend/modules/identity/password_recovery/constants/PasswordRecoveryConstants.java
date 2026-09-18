package com.locadora_rdt_backend.modules.identity.password_recovery.constants;

public final class PasswordRecoveryConstants {

    // Configurações
    public static final String FRONTEND_BASE_URL_PROPERTY = "${app.frontend.base-url}";
    public static final String TOKEN_MINUTES_PROPERTY = "${app.password-reset.token-minutes:30}";

    // Link e e-mail
    public static final String PASSWORD_RESET_PATH = "/recovery-password/password-reset";
    public static final String TOKEN_QUERY_PARAM = "token";
    public static final String PASSWORD_RESET_EMAIL_SUBJECT = "Recuperação de senha - Locadora RDT";

    // Validações
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String EMAIL_REQUIRED = "Email é obrigatório";
    public static final String EMAIL_INVALID = "Email inválido";
    public static final String PASSWORD_REQUIRED = "A senha é obrigatória";
    public static final String PASSWORD_MIN_LENGTH_MESSAGE = "A senha deve ter no mínimo 6 caracteres";

    // Mensagens de erro
    public static final String INVALID_TOKEN = "Token inválido";
    public static final String INVALID_PASSWORD = "Senha inválida";
    public static final String INVALID_OR_EXPIRED_TOKEN = "Token inválido ou expirado";
    public static final String PASSWORD_EQUALS_CURRENT = "A nova senha não pode ser igual à senha atual";

    private PasswordRecoveryConstants() {
    }

}
