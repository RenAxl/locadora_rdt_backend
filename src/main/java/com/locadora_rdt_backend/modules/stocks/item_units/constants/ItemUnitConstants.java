package com.locadora_rdt_backend.modules.stocks.item_units.constants;

public final class ItemUnitConstants {

    // Validações
    public static final int ASSET_CODE_MAX_LENGTH = 60;
    public static final int SERIAL_NUMBER_MAX_LENGTH = 100;
    public static final int NOTES_MAX_LENGTH = 500;
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String ASSET_CODE_LENGTH = "O código patrimonial deve ter até 60 caracteres";
    public static final String SERIAL_NUMBER_LENGTH = "O número de série deve ter até 100 caracteres";
    public static final String NOTES_LENGTH = "As observações devem ter até 500 caracteres";
    public static final String CONDITION_PATTERN = "NEW|GOOD|DAMAGED";
    public static final String INVALID_CONDITION = "Condição inválida. Use NEW, GOOD ou DAMAGED";

    // Unidades físicas
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_RESERVED = "RESERVED";

    // Mensagens de erro
    public static final String ITEM_UNIT_NOT_FOUND = "Unidade física não encontrada";
    public static final String DATABASE_INTEGRITY_VIOLATION =
            "Código patrimonial ou número de série já cadastrado, ou unidade com registros vinculados";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String DUPLICATE_IDS = "A lista não pode conter IDs repetidos";
    public static final String ONLY_AVAILABLE_UNITS_CAN_BE_REMOVED =
            "Somente unidades disponíveis podem ser excluídas ou desativadas";
    public static final String ITEM_CANNOT_BE_CHANGED = "O item de uma unidade física não pode ser alterado";

    private ItemUnitConstants() {
    }
}
