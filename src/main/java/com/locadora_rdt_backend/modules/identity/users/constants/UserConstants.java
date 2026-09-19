package com.locadora_rdt_backend.modules.identity.users.constants;

import java.util.Set;

public final class UserConstants {

    //Configurações
    public static final String FRONTEND_BASE_URL_PROPERTY = "${app.frontend.base-url}";

    // Arquivo de foto
    public static final Set<String> ALLOWED_PHOTO_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    public static final long MAX_PHOTO_SIZE_BYTES = 2L * 1024 * 1024;

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 60;
    public static final String NAME_LENGTH = "O nome deve ter entre 3 e 60 caracteres";
    public static final String REQUIRED_FIELD = "Campo requerido";
    public static final String INVALID_EMAIL = "Favor informar um email válido";
    public static final String ROLES_REQUIRED = "Informe pelo menos um perfil";
    public static final String CURRENT_PASSWORD_REQUIRED = "Senha atual é obrigatória";
    public static final String NEW_PASSWORD_REQUIRED = "Nova senha é obrigatória";
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String NEW_PASSWORD_MIN_LENGTH = "A nova senha deve ter no mínimo 6 caracteres";
    public static final String EMAIL_REQUIRED = "Email é obrigatório";
    public static final String EMAIL_INVALID = "Email inválido";

    // Mensagens de erro
    public static final String USER_NOT_FOUND = "Usuário não encontrado";
    public static final String ID_NOT_FOUND = "Id não encontrado";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String STATUS_CHANGE_ERROR = "Erro ao alterar o status do usuário.";
    public static final String NULL_ID = "O Id é nulo";
    public static final String ACCESS_DENIED = "Acesso não autorizado";
    public static final String INVALID_DATA = "Dados inválidos";
    public static final String PASSWORD_EQUALS_CURRENT = "A nova senha não pode ser igual à senha atual";
    public static final String EMPTY_PHOTO_FILE = "Arquivo de foto vazio.";
    public static final String INVALID_PHOTO_TYPE = "Tipo de arquivo inválido. Use JPG, PNG ou WEBP.";
    public static final String PHOTO_TOO_LARGE = "Foto muito grande. Máximo: 2MB.";
    public static final String PHOTO_PROCESSING_ERROR = "Falha ao processar o arquivo enviado.";

    private UserConstants() {
    }
}
