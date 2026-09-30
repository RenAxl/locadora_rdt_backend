package com.locadora_rdt_backend.modules.settings.financial_settings.constants;

public final class FinancialSettingConstants {

    // Validações
    public static final String MINIMUM_PERCENTAGE = "0.0";
    public static final int PERCENTAGE_MAX_INTEGER_DIGITS = 10;
    public static final int PERCENTAGE_MAX_FRACTION_DIGITS = 2;
    public static final String LATE_FEE_PERCENT_REQUIRED =
            "Percentual padrão de multa por atraso é obrigatório";
    public static final String LATE_FEE_PERCENT_MINIMUM =
            "Percentual padrão de multa por atraso deve ser maior ou igual a zero";
    public static final String LATE_FEE_PERCENT_INVALID =
            "Percentual padrão de multa por atraso inválido";
    public static final String LATE_INTEREST_PERCENT_REQUIRED =
            "Percentual padrão de juros por atraso é obrigatório";
    public static final String LATE_INTEREST_PERCENT_MINIMUM =
            "Percentual padrão de juros por atraso deve ser maior ou igual a zero";
    public static final String LATE_INTEREST_PERCENT_INVALID =
            "Percentual padrão de juros por atraso inválido";

    // Configuração única
    public static final String DEFAULT_SINGLETON_KEY = "DEFAULT";

    // Auditoria
    public static final String SYSTEM_USER = "SYSTEM";

    private FinancialSettingConstants() {
    }
}
