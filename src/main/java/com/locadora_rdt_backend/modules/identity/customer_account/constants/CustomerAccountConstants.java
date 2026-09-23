package com.locadora_rdt_backend.modules.identity.customer_account.constants;

public final class CustomerAccountConstants {

    // Configurações e regras de negócio
    public static final String FRONTEND_BASE_URL_PROPERTY = "${app.frontend.base-url}";
    public static final String TOKEN_MINUTES_PROPERTY = "${app.activation.token-minutes:30}";
    public static final String CUSTOMER_ROLE = "ROLE_CLIENTE";
    public static final String CUSTOMER_REGISTRATION_AUDIT = "CUSTOMER_REGISTRATION";
    public static final String NON_DIGIT_PATTERN = "\\D";

    // Link e e-mail
    public static final String CREATE_PASSWORD_PATH = "/customer-account/create-password";
    public static final String TOKEN_QUERY_PARAM = "token";
    public static final String CREATE_PASSWORD_EMAIL_SUBJECT = "Crie sua senha - Locadora RDT";
    public static final String ACTIVATION_EMAIL_SEND_ERROR =
            "Não foi possível enviar o e-mail de ativação para {}.";

    // Validações
    public static final int NAME_MAX_LENGTH = 100;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int STATE_LENGTH = 2;
    public static final String CPF_PATTERN = "\\d{11}";
    public static final String PHONE_PATTERN = "[0-9() +\\-]{10,20}";
    public static final String NAME_REQUIRED = "Informe o nome";
    public static final String NAME_MAX_LENGTH_MESSAGE = "O nome deve ter no máximo 100 caracteres";
    public static final String CPF_REQUIRED = "Informe o CPF";
    public static final String CPF_INVALID = "O CPF deve conter 11 dígitos";
    public static final String EMAIL_REQUIRED = "Informe o e-mail";
    public static final String EMAIL_INVALID = "Informe um e-mail válido";
    public static final String PHONE_REQUIRED = "Informe o telefone";
    public static final String PHONE_INVALID = "Informe um telefone válido";
    public static final String STREET_REQUIRED = "Informe a rua";
    public static final String NUMBER_REQUIRED = "Informe o número";
    public static final String NEIGHBORHOOD_REQUIRED = "Informe o bairro";
    public static final String CITY_REQUIRED = "Informe a cidade";
    public static final String STATE_REQUIRED = "Informe o estado";
    public static final String STATE_LENGTH_MESSAGE = "O estado deve ter 2 caracteres";
    public static final String ZIP_CODE_REQUIRED = "Informe o CEP";
    public static final String PASSWORD_REQUIRED = "Informe a nova senha";
    public static final String PASSWORD_MIN_LENGTH_MESSAGE = "A senha deve ter pelo menos 6 caracteres";
    public static final String PASSWORD_CONFIRMATION_REQUIRED = "Confirme a nova senha";

    // Mensagens de erro
    public static final String EMAIL_ALREADY_REGISTERED_AS_USER = "E-mail já cadastrado em usuário.";
    public static final String EMAIL_ALREADY_REGISTERED_AS_CUSTOMER = "E-mail já cadastrado em cliente.";
    public static final String CPF_ALREADY_REGISTERED_AS_CUSTOMER = "CPF já cadastrado em cliente.";
    public static final String CUSTOMER_ROLE_NOT_FOUND = "Role de cliente não encontrada.";
    public static final String INVALID_TOKEN = "Token inválido.";
    public static final String PASSWORDS_DO_NOT_MATCH = "A senha e a confirmação devem ser iguais.";
    public static final String INVALID_EXPIRED_OR_USED_TOKEN = "Token inválido, expirado ou já utilizado.";
    public static final String USER_NOT_FOUND = "Usuário não encontrado.";
    public static final String ACCOUNT_ALREADY_ACTIVE = "A conta já está ativa.";

    private CustomerAccountConstants() {
    }
}
