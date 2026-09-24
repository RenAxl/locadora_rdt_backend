package com.locadora_rdt_backend.modules.organization.positions.constants;

public final class PositionConstants {

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 60;
    public static final String NAME_REQUIRED = "Campo requerido";
    public static final String NAME_LENGTH = "O nome deve ter entre 3 a 60 caracteres";

    // Mensagens de erro
    public static final String POSITION_NOT_FOUND = "Cargo não encontrado";
    public static final String DATABASE_INTEGRITY_VIOLATION =
            "Violação de integridade no banco de dados";

    private PositionConstants() {
    }
}
