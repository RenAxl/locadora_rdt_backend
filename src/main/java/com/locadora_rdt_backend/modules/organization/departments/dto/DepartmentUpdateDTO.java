package com.locadora_rdt_backend.modules.organization.departments.dto;

import com.locadora_rdt_backend.modules.organization.departments.constants.DepartmentConstants;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class DepartmentUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = DepartmentConstants.NAME_MIN_LENGTH, max = DepartmentConstants.NAME_MAX_LENGTH,
            message = DepartmentConstants.NAME_LENGTH)
    @NotBlank(message = DepartmentConstants.REQUIRED_FIELD)
    private String name;

    @Size(max = DepartmentConstants.DESCRIPTION_MAX_LENGTH,
            message = DepartmentConstants.DESCRIPTION_MAX_LENGTH_MESSAGE)
    private String description;

    public DepartmentUpdateDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
