package com.locadora_rdt_backend.modules.organization.suppliers.constants;

import java.util.Set;

public final class SupplierConstants {

    // Arquivo de imagem
    public static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    public static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024 * 1024;

    // Anexos
    public static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/zip",
            "application/x-rar-compressed",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "text/plain",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel",
            "application/xml",
            "text/xml",
            "application/vnd.oasis.opendocument.text"
    );
    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    // Regras de negócio
    public static final long NEW_ENTITY_ID = -1L;

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 100;
    public static final int TRADE_NAME_MIN_LENGTH = 3;
    public static final int TRADE_NAME_MAX_LENGTH = 100;
    public static final int COMPANY_NAME_MIN_LENGTH = 3;
    public static final int COMPANY_NAME_MAX_LENGTH = 150;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int PHONE_MAX_LENGTH = 20;
    public static final String CNPJ_PATTERN = "\\d{14}";
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String NAME_LENGTH = "O nome deve ter entre 3 e 100 caracteres";
    public static final String TRADE_NAME_LENGTH = "O nome fantasia deve ter entre 3 e 100 caracteres";
    public static final String COMPANY_NAME_LENGTH = "A razão social deve ter entre 3 e 150 caracteres";
    public static final String CNPJ_LENGTH = "O CNPJ deve possuir 14 dígitos";
    public static final String EMAIL_INVALID = "Email inválido";
    public static final String EMAIL_MAX_LENGTH_MESSAGE = "O email deve possuir no máximo 100 caracteres";
    public static final String PHONE_MAX_LENGTH_MESSAGE = "O telefone deve possuir no máximo 20 caracteres";

    // Mensagens de erro
    public static final String SUPPLIER_NOT_FOUND = "Fornecedor não encontrado";
    public static final String FILE_NOT_FOUND = "Arquivo não encontrado";
    public static final String FILE_DOES_NOT_BELONG_TO_SUPPLIER =
            "Arquivo não pertence ao fornecedor informado.";
    public static final String DATABASE_INTEGRITY_VIOLATION =
            "Violação de integridade no banco de dados";
    public static final String IMAGE_READ_ERROR = "Erro ao ler a imagem enviada.";
    public static final String CNPJ_ALREADY_EXISTS = "CNPJ já existe";
    public static final String EMAIL_ALREADY_EXISTS = "Email já existe";
    public static final String PHONE_ALREADY_EXISTS = "Telefone já existe";

    public static final String EMPTY_IMAGE_FILE = "Arquivo de imagem vazio.";
    public static final String INVALID_IMAGE_TYPE = "Tipo de arquivo inválido. Use JPG, PNG ou WEBP.";
    public static final String IMAGE_TOO_LARGE = "Imagem muito grande. Máximo: 2MB.";
    public static final String FILE_NAME_REQUIRED = "É obrigatório informar o nome do arquivo.";
    public static final String EMPTY_FILE = "É obrigatório enviar um arquivo.";
    public static final String INVALID_FILE_NAME = "Nome original do arquivo é inválido.";
    public static final String FILE_TOO_LARGE = "O arquivo excede o tamanho máximo permitido de 10MB.";
    public static final String INVALID_FILE_TYPE = "Tipo de arquivo não permitido.";
    public static final String FILE_READ_ERROR = "Erro ao ler o arquivo enviado.";

    private SupplierConstants() {
    }
}
