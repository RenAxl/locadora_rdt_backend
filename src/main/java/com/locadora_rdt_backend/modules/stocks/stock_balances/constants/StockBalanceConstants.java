package com.locadora_rdt_backend.modules.stocks.stock_balances.constants;

public final class StockBalanceConstants {

    // Validações
    public static final int MINIMUM_QUANTITY = 0;
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String TOTAL_QUANTITY_MINIMUM = "A quantidade total não pode ser negativa";
    public static final String RESERVED_QUANTITY_MINIMUM = "A quantidade alugada não pode ser negativa";
    public static final String UNAVAILABLE_QUANTITY_MINIMUM = "A quantidade indisponível não pode ser negativa";
    public static final String MINIMUM_QUANTITY_MINIMUM = "A quantidade mínima não pode ser negativa";

    // Unidades físicas
    public static final String ASSET_CODE_PREFIX = "ITEM-";
    public static final String ASSET_CODE_SEPARATOR = "-";
    public static final int ASSET_CODE_RANDOM_START = 0;
    public static final int ASSET_CODE_RANDOM_END = 8;
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";
    public static final String CONDITION_GOOD = "GOOD";
    public static final String MANUAL_CREATION_NOTE = "Unidade criada pela edição manual do saldo.";

    // Mensagens de erro
    public static final String STOCK_BALANCE_NOT_FOUND = "Saldo de estoque não encontrado";
    public static final String USED_QUANTITY_EXCEEDS_TOTAL =
            "A quantidade alugada e indisponível não pode ser maior que a quantidade total.";
    public static final String INSUFFICIENT_AVAILABLE_UNITS =
            "Não existem unidades disponíveis suficientes para esta alteração.";
    public static final String ACTIVE_RENTAL_UNITS_CANNOT_BE_RELEASED =
            "Não é possível liberar unidades alugadas por uma locação ativa.";
    public static final String LINKED_UNITS_CANNOT_BE_CHANGED =
            "Não é possível alterar unidades alugadas ou vinculadas a uma locação.";
    public static final String ONLY_AVAILABLE_UNITS_CAN_BE_REMOVED =
            "Somente unidades disponíveis podem ser removidas do estoque.";

    private StockBalanceConstants() {
    }
}
