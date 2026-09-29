package com.locadora_rdt_backend.modules.organization.employees.constants;

import java.util.Set;

public final class EmployeeConstants {

    // Arquivo de foto
    public static final Set<String> ALLOWED_PHOTO_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    public static final long MAX_PHOTO_SIZE_BYTES = 2L * 1024 * 1024;

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

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 120;
    public static final int EMPLOYEE_CODE_MIN_LENGTH = 3;
    public static final int EMPLOYEE_CODE_MAX_LENGTH = 30;
    public static final int EMAIL_MAX_LENGTH = 120;
    public static final int PHONE_MAX_LENGTH = 20;
    public static final int ADDRESS_MAX_LENGTH = 255;
    public static final int CONTRACT_TYPE_MAX_LENGTH = 30;
    public static final int SALARY_MAX_INTEGER_DIGITS = 10;
    public static final int SALARY_MAX_FRACTION_DIGITS = 2;
    public static final String MINIMUM_SALARY = "0.0";
    public static final String NAME_REQUIRED = "Nome é obrigatório";
    public static final String NAME_LENGTH = "Nome deve ter entre 3 e 120 caracteres";
    public static final String EMPLOYEE_CODE_REQUIRED = "Matrícula é obrigatória";
    public static final String EMPLOYEE_CODE_LENGTH = "Matrícula deve ter entre 3 e 30 caracteres";
    public static final String EMAIL_INVALID = "Email inválido";
    public static final String EMAIL_MAX_LENGTH_MESSAGE = "Email deve ter no máximo 120 caracteres";
    public static final String PHONE_MAX_LENGTH_MESSAGE = "Telefone deve ter no máximo 20 caracteres";
    public static final String ADDRESS_MAX_LENGTH_MESSAGE = "Endereço deve ter no máximo 255 caracteres";
    public static final String SALARY_MUST_BE_POSITIVE = "Salário deve ser maior que zero";
    public static final String SALARY_INVALID = "Salário inválido";
    public static final String HIRE_DATE_REQUIRED = "Data de admissão é obrigatória";
    public static final String CONTRACT_TYPE_REQUIRED = "Tipo de contratação é obrigatório";
    public static final String CONTRACT_TYPE_MAX_LENGTH_MESSAGE =
            "Tipo de contratação deve ter no máximo 30 caracteres";
    public static final String POSITION_REQUIRED = "Cargo é obrigatório";
    public static final String DEPARTMENT_REQUIRED = "Setor é obrigatório";
    public static final String VALIDATION_ERROR = "Validation error";
    public static final String EMAIL_ALREADY_EXISTS = "Email já existe";
    public static final String EMPLOYEE_CODE_ALREADY_EXISTS = "Matrícula já existe";
    public static final String PHONE_ALREADY_EXISTS = "Telefone já existe";
    public static final String TERMINATION_DATE_BEFORE_HIRE_DATE =
            "A data de desligamento não pode ser menor que a data de admissão";

    // Mensagens de erro
    public static final String EMPLOYEE_NOT_FOUND = "Funcionário não encontrado";
    public static final String POSITION_OR_DEPARTMENT_NOT_FOUND = "Cargo ou setor não encontrado";
    public static final String EMPLOYEE_POSITION_OR_DEPARTMENT_NOT_FOUND =
            "Funcionário, cargo ou setor não encontrado";
    public static final String FILE_NOT_FOUND_WITH_ID = "Arquivo não encontrado. Id: ";
    public static final String FILE_DOES_NOT_BELONG_TO_EMPLOYEE =
            "Arquivo não pertence ao funcionário informado.";
    public static final String EMPLOYEE_NOT_FOUND_WITH_ID = "Funcionário não encontrado. Id: ";
    public static final String IMAGE_READ_ERROR = "Falha ao ler bytes do arquivo.";
    public static final String ID_NOT_FOUND = "Id not found ";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String CHANGE_ACTIVE_STATUS_ERROR = "Error changing employee status.";

    public static final String EMPTY_PHOTO_FILE = "Arquivo de foto vazio.";
    public static final String INVALID_PHOTO_TYPE = "Tipo de arquivo inválido. Use JPG, PNG ou WEBP.";
    public static final String PHOTO_TOO_LARGE = "Foto muito grande. Máximo: 2MB.";
    public static final String FILE_NAME_REQUIRED = "É obrigatório informar o nome do arquivo.";
    public static final String EMPTY_FILE = "É obrigatório enviar um arquivo.";
    public static final String INVALID_FILE_NAME = "Nome original do arquivo é inválido.";
    public static final String FILE_TOO_LARGE = "O arquivo excede o tamanho máximo permitido de 10MB.";
    public static final String INVALID_FILE_TYPE = "Tipo de arquivo não permitido.";
    public static final String FILE_READ_ERROR = "Erro ao ler o arquivo enviado.";

    private EmployeeConstants() {
    }
}
