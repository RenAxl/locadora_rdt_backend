package com.locadora_rdt_backend.modules.financial.payables.constants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public final class PayableConstants {

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

    // Valores financeiros
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal FILTER_AMOUNT_DISABLED = BigDecimal.valueOf(-1);
    public static final BigDecimal PERCENT_DIVISOR = BigDecimal.valueOf(100);
    public static final BigDecimal DEFAULT_DISCOUNT_PERCENT = new BigDecimal("5.00");
    public static final int MONEY_SCALE = 2;
    public static final String MINIMUM_AMOUNT = "0.01";

    // Filtros e paginação
    public static final LocalDate FILTER_DATE_DISABLED = LocalDate.of(1970, 1, 1);
    public static final long FILTER_ID_DISABLED = -1L;
    public static final String STATUS_ALL = "ALL";
    public static final String STATUS_OPEN = "OPEN";
    public static final String PERIOD_DUE = "DUE";
    public static final String PERIOD_PAYMENT = "PAYMENT";
    public static final String PERIOD_CREATED = "CREATED";
    public static final String PERIOD_DUE_DATE = "DUE_DATE";
    public static final String PERIOD_PAYMENT_DATE = "PAYMENT_DATE";
    public static final String PERIOD_CREATED_DATE = "CREATED_DATE";
    public static final String ORDER_BY_DUE_DATE = "dueDate";
    public static final String ORDER_BY_PAYMENT_DATE = "paymentDate";
    public static final String ORDER_BY_CREATED_DATE = "createdDate";
    public static final String ORDER_BY_AMOUNT = "amount";
    public static final String ORDER_BY_DESCRIPTION = "description";
    public static final String DIRECTION_ASC = "ASC";
    public static final String DIRECTION_DESC = "DESC";
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_LINES_PER_PAGE = "10";

    // Validações
    public static final int DESCRIPTION_MIN_LENGTH = 3;
    public static final int DESCRIPTION_MAX_LENGTH = 120;
    public static final String DESCRIPTION_REQUIRED = "Descrição é obrigatória";
    public static final String DESCRIPTION_LENGTH = "Descrição deve ter entre 3 e 120 caracteres";
    public static final String AMOUNT_REQUIRED = "Valor é obrigatório";
    public static final String AMOUNT_MUST_BE_POSITIVE = "O valor deve ser maior que zero";
    public static final String DUE_DATE_REQUIRED = "Vencimento é obrigatório";
    public static final String PAYMENT_AMOUNT_REQUIRED = "Informe o valor da baixa";
    public static final String PAYMENT_AMOUNT_POSITIVE_VALIDATION = "Valor de baixa deve ser maior que zero";

    // Mensagens de erro
    public static final String PAYABLE_NOT_FOUND = "Conta a pagar não encontrada.";
    public static final String DATABASE_INTEGRITY_VIOLATION = "Não foi possível excluir a conta a pagar.";
    public static final String PAID_PAYABLE_CANNOT_BE_UPDATED = "Conta a pagar paga não pode ser atualizada.";
    public static final String CANCELED_PAYABLE_CANNOT_BE_UPDATED = "Conta a pagar cancelada não pode ser atualizada.";
    public static final String CANCELED_PAYABLE_CANNOT_BE_PAID = "Conta a pagar cancelada não pode receber baixa.";
    public static final String PAID_PAYABLE_CANNOT_BE_PAID = "Conta já está paga.";
    public static final String PAYMENT_AMOUNT_MUST_BE_POSITIVE = "Valor de baixa deve ser maior que zero.";
    public static final String PAYMENT_AMOUNT_EXCEEDS_PAYABLE =
            "Valor de baixa não pode ser maior que o valor da conta.";
    public static final String NON_OVERDUE_CHARGES_CANNOT_BE_EDITED =
            "Multa, juros e desconto não podem ser alterados em contas não vencidas.";
    public static final String SUPPLIER_NOT_FOUND = "Fornecedor não encontrado. Id: ";
    public static final String EMPLOYEE_NOT_FOUND = "Funcionário não encontrado. Id: ";
    public static final String PAYMENT_METHOD_NOT_FOUND = "Forma de pagamento não encontrada. Id: ";
    public static final String PAYMENT_FREQUENCY_NOT_FOUND = "Frequência não encontrada. Id: ";
    public static final String FILE_NOT_FOUND = "Arquivo não encontrado. Id: ";
    public static final String FILE_DOES_NOT_BELONG_TO_PAYABLE = "Arquivo não pertence à conta informada.";
    public static final String ID_COMPLEMENT = " Id: ";

    public static final String FILE_NAME_REQUIRED = "É obrigatório informar o nome do arquivo.";
    public static final String EMPTY_FILE = "É obrigatório enviar um arquivo.";
    public static final String INVALID_FILE_NAME = "Nome original do arquivo é inválido.";
    public static final String FILE_TOO_LARGE = "O arquivo excede o tamanho máximo permitido de 10MB.";
    public static final String INVALID_FILE_TYPE = "Tipo de arquivo não permitido.";
    public static final String FILE_READ_ERROR = "Erro ao ler o arquivo enviado.";

    private PayableConstants() {
    }
}
