package com.locadora_rdt_backend.modules.stocks.items.constants;

import java.util.Set;

public final class ItemConstants {

    // Arquivo de imagem
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    public static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024 * 1024;

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MIN_LENGTH = 3;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final String MINIMUM_PRICE = "0.00";
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String NAME_LENGTH = "O nome deve ter entre 3 a 100 caracteres";
    public static final String DESCRIPTION_LENGTH = "A descrição deve ter entre 3 a 500 caracteres";
    public static final String PRICE_MINIMUM = "O preço não pode ser negativo";

    // Mensagens de erro
    public static final String ITEM_NOT_FOUND = "Item não encontrado";
    public static final String DATABASE_INTEGRITY_VIOLATION = "Violação de integridade no banco de dados";
    public static final String IMAGE_READ_ERROR = "Falha ao ler bytes do arquivo.";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String CHANGE_ACTIVE_STATUS_ERROR = "Error changing item status.";

    public static final String EMPTY_IMAGE_FILE = "Arquivo de foto vazio.";
    public static final String INVALID_IMAGE_TYPE = "Tipo de arquivo inválido. Use JPG, PNG ou WEBP.";
    public static final String IMAGE_TOO_LARGE = "Foto muito grande. Máximo: 2MB.";

    private ItemConstants() {
    }
}
