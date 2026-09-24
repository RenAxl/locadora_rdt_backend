package com.locadora_rdt_backend.modules.organization.departments.constants;

public final class DepartmentConstants {

    // Validações
    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    public static final String REQUIRED_FIELD = "Campo requerido";
    public static final String NAME_LENGTH = "O nome deve ter entre 3 e 100 caracteres";
    public static final String DESCRIPTION_MAX_LENGTH_MESSAGE =
            "A descrição deve ter no máximo 255 caracteres";

    // Mensagens de erro
    public static final String DEPARTMENT_NOT_FOUND = "Setor não encontrado";
    public static final String DATABASE_INTEGRITY_VIOLATION =
            "Violação de integridade no banco de dados";

    private DepartmentConstants() {
    }
}
