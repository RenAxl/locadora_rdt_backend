package com.locadora_rdt_backend.modules.stocks.stock_movements.constants;

public final class StockMovementConstants {

    public static final int MINIMUM_QUANTITY = 0;
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String QUANTITY_MINIMUM = "A quantidade não pode ser negativa";
    public static final String REASON_LENGTH = "O motivo deve ter até 255 caracteres";
    public static final String MOVEMENT_CREATION_NOTE = "Unidade criada por movimentação de estoque.";
    public static final String STOCK_MOVEMENT_NOT_FOUND = "Movimentação de estoque não encontrada";
    public static final String INVALID_MOVEMENT_TYPE = "Tipo de movimentação inválido";
    public static final String INVALID_QUANTITY = "Quantidade inválida para esta movimentação";
    public static final String INSUFFICIENT_STOCK = "Não existem unidades disponíveis suficientes para esta saída";
    public static final String UNIT_ITEM_MISMATCH = "A unidade física não pertence ao item informado";
    public static final String INVALID_UNIT_MOVEMENT = "Informe uma unidade e quantidade igual a um para esta operação";

    private StockMovementConstants() {
    }
}
