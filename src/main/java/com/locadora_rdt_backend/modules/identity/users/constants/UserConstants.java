package com.locadora_rdt_backend.modules.identity.users.constants;

public final class UserConstants {

    // Valores padrão
    public static final String TEST_USER = "Usuário Teste";

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 60;
    public static final String NAME_LENGTH = "O nome deve ter entre 3 e 60 caracteres";
    public static final String REQUIRED_FIELD = "Campo requerido";
    public static final String INVALID_EMAIL = "Favor informar um email válido";
    public static final String ROLES_REQUIRED = "Informe pelo menos um perfil";

    // Mensagens de erro
    public static final String USER_NOT_FOUND = "Usuário não encontrado";
    public static final String ID_NOT_FOUND = "Id não encontrado";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String STATUS_CHANGE_ERROR = "Erro ao alterar o status do usuário.";
    public static final String NULL_ID = "O Id é nulo";

    private UserConstants() {
    }
}
