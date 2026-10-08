package com.locadora_rdt_backend.modules.rentals.rental_types.constants;

public final class RentalTypeConstants {

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 60;
    public static final int TYPE_MIN_LENGTH = 2;
    public static final int TYPE_MAX_LENGTH = 30;
    public static final int DAYS_MIN_VALUE = 1;
    public static final String FIELD_REQUIRED = "Campo requerido";
    public static final String NAME_LENGTH = "O nome deve ter entre 3 a 60 caracteres";
    public static final String TYPE_LENGTH = "O tipo deve ter entre 2 a 30 caracteres";
    public static final String DAYS_MIN_VALUE_MESSAGE = "A quantidade de dias deve ser maior que zero";

    // Mensagens de erro
    public static final String RENTAL_TYPE_NOT_FOUND = "Tipo de locação não encontrado";
    public static final String DATABASE_INTEGRITY_VIOLATION = "Violação de integridade no banco de dados";
    public static final String EMPTY_ID_LIST = "Lista de ids vazia";
    public static final String ONE_OR_MORE_IDS_NOT_FOUND = "Um ou mais IDs não existem";
    public static final String CHANGE_ACTIVE_STATUS_ERROR = "Error changing rental type status.";

    private RentalTypeConstants() {
    }
}
