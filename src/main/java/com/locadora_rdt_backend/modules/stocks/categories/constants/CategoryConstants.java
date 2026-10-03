package com.locadora_rdt_backend.modules.stocks.categories.constants;

import java.util.Set;

public final class CategoryConstants {

    // Arquivo de imagem
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    public static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024 * 1024;

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 60;
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String NAME_LENGTH = "O nome deve ter entre 3 a 60 caracteres";

    // Mensagens de erro
    public static final String CATEGORY_NOT_FOUND = "Categoria não encontrada";
    public static final String DATABASE_INTEGRITY_VIOLATION = "Violação de integridade no banco de dados";
    public static final String IMAGE_READ_ERROR = "Falha ao ler bytes do arquivo.";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String CHANGE_ACTIVE_STATUS_ERROR = "Error changing category status.";

    public static final String EMPTY_IMAGE_FILE = "Arquivo de foto vazio.";
    public static final String INVALID_IMAGE_TYPE = "Tipo de arquivo inválido. Use JPG, PNG ou WEBP.";
    public static final String IMAGE_TOO_LARGE = "Foto muito grande. Máximo: 2MB.";

    private CategoryConstants() {
    }
}
