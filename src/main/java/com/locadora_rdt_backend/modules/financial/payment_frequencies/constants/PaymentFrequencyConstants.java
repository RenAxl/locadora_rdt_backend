package com.locadora_rdt_backend.modules.financial.payment_frequencies.constants;

public final class PaymentFrequencyConstants {

    // Validações
    public static final int FREQUENCY_MIN_LENGTH = 3;
    public static final int FREQUENCY_MAX_LENGTH = 60;
    public static final long MINIMUM_DAYS = 0L;
    public static final String FREQUENCY_REQUIRED = "Frequência é obrigatória";
    public static final String FREQUENCY_LENGTH = "Frequência deve ter entre 3 e 60 caracteres";
    public static final String DAYS_REQUIRED = "Dias é obrigatório";
    public static final String DAYS_MINIMUM = "Dias deve ser maior ou igual a zero";

    // Mensagens de erro
    public static final String PAYMENT_FREQUENCY_NOT_FOUND = "Frequência de pagamento não encontrada";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String DATABASE_INTEGRITY_VIOLATION = "Violação de integridade no banco de dados";

    private PaymentFrequencyConstants() {
    }
}
