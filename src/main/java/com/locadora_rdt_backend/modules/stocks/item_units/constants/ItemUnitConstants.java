package com.locadora_rdt_backend.modules.stocks.item_units.constants;

public final class ItemUnitConstants {

    public static final String ASSET_CODE_PREFIX = "ITEM-";

    public static final int NOTES_MAX_LENGTH = 500;
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String NOTES_LENGTH = "As observações devem ter até 500 caracteres";
    public static final String REASON_LENGTH = "O motivo deve ter até 255 caracteres";
    public static final String ITEM_UNIT_NOT_FOUND = "Unidade física não encontrada";
    public static final String DATABASE_INTEGRITY_VIOLATION = "Não foi possível salvar a unidade física por uma restrição do banco de dados";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String DUPLICATE_IDS = "A lista não pode conter IDs repetidos";
    public static final String ITEM_CANNOT_BE_CHANGED = "O item de uma unidade física não pode ser alterado";
    public static final String INACTIVE_ITEM = "Não é possível adicionar unidades a um item ou categoria inativos";
    public static final String INVALID_STATUS = "Informe um status válido";
    public static final String INACTIVE_UNIT = "Não é possível alterar o status de uma unidade inativa";
    public static final String INVALID_MAINTENANCE_CHANGE = "A operação de manutenção exige uma unidade disponível ou em manutenção";

    private ItemUnitConstants() {
    }
}
