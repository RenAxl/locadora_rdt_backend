package com.locadora_rdt_backend.modules.settings.system_settings.constants;

public final class SystemSettingConstants {

    // Configuração única
    public static final String DEFAULT_SINGLETON_KEY = "DEFAULT";
    public static final String DEFAULT_COMPANY_NAME = "Locadora RDT";
    public static final String SYSTEM_USER = "SYSTEM";

    public static final String DEFAULT_ICON = "fa-gamepad";
    public static final String ICON_PATTERN =
            "fa-(gamepad|store|car|screwdriver-wrench|building|film"
                    + "|house|truck|motorcycle|bicycle|bus|plane"
                    + "|ship|desktop|laptop|mobile-screen-button|camera|music"
                    + "|headphones|tv|book|graduation-cap|utensils|mug-hot"
                    + "|cart-shopping|bag-shopping|gift|star|heart|futbol"
                    + "|dumbbell|tree|leaf|briefcase|gear|box"
                    + "|headset|vr-cardboard|ghost|robot"
                    + "|dragon|dice-d20|dice|trophy"
                    + "|crosshairs|shield-halved|bolt|puzzle-piece"
                    + "|chess-knight|rocket|skull|coins"
                    + "|playstation|xbox)";
    public static final String INVALID_ICON = "Selecione um ícone válido";

    // Validações
    public static final int COMPANY_NAME_MAX_LENGTH = 120;
    public static final String COMPANY_NAME_REQUIRED = "Nome da locadora é obrigatório";
    public static final String COMPANY_NAME_MAX_LENGTH_MESSAGE =
            "Nome da locadora deve ter no máximo 120 caracteres";
    public static final String ADDRESS_REQUIRED = "Endereço é obrigatório";

    public static final String STREET_REQUIRED = "Rua é obrigatória";
    public static final String NUMBER_REQUIRED = "Número é obrigatório";
    public static final String NEIGHBORHOOD_REQUIRED = "Bairro é obrigatório";
    public static final String CITY_REQUIRED = "Cidade é obrigatória";
    public static final String STATE_REQUIRED = "UF é obrigatória";
    public static final int STATE_LENGTH = 2;
    public static final String STATE_LENGTH_MESSAGE = "UF deve possuir 2 caracteres";
    public static final String ZIP_CODE_REQUIRED = "CEP é obrigatório";
    public static final String ZIP_CODE_PATTERN = "\\d{5}-?\\d{3}";
    public static final String INVALID_ZIP_CODE = "CEP inválido";

    private SystemSettingConstants() {
    }
}
