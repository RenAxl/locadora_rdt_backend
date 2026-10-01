package com.locadora_rdt_backend.modules.financial.payment_methods.constants;

public final class PaymentMethodConstants {

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 60;
    public static final int FEE_MAX_INTEGER_DIGITS = 10;
    public static final int FEE_MAX_FRACTION_DIGITS = 2;
    public static final String MINIMUM_FEE = "0.0";
    public static final String NAME_REQUIRED = "Nome é obrigatório";
    public static final String NAME_LENGTH = "Nome deve ter entre 3 e 60 caracteres";
    public static final String FEE_MINIMUM = "Taxa deve ser maior ou igual a zero";
    public static final String FEE_INVALID = "Taxa inválida";

    // Mensagens de erro
    public static final String PAYMENT_METHOD_NOT_FOUND =
            "Forma de pagamento não encontrada";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String DATABASE_INTEGRITY_VIOLATION =
            "Violação de integridade no banco de dados";

    private PaymentMethodConstants() {
    }
}
