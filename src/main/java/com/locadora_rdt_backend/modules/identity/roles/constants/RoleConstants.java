package com.locadora_rdt_backend.modules.identity.roles.constants;

public final class RoleConstants {

    // Valores padrão
    public static final long DEFAULT_PERMISSIONS_COUNT = 0L;
    public static final String ID_SEPARATOR = ": ";

    // Validações
    public static final int AUTHORITY_MIN_LENGTH = 3;
    public static final int AUTHORITY_MAX_LENGTH = 100;
    public static final String AUTHORITY_REQUIRED = "Campo requerido";
    public static final String AUTHORITY_LENGTH = "O perfil deve ter entre 3 e 100 caracteres";
    public static final String PERMISSIONS_REQUIRED = "Informe pelo menos uma permissão";

    // Mensagens de erro
    public static final String ROLE_NOT_FOUND = "Perfil não encontrado";
    public static final String PERMISSIONS_UPDATE_ERROR =
            "Erro ao atualizar permissões do perfil";

    private RoleConstants() {
    }
}
