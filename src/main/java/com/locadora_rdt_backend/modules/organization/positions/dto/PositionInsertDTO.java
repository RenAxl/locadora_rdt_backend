package com.locadora_rdt_backend.modules.organization.positions.dto;

import com.locadora_rdt_backend.modules.organization.positions.constants.PositionConstants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class PositionInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = PositionConstants.NAME_MIN_LENGTH, max = PositionConstants.NAME_MAX_LENGTH,
            message = PositionConstants.NAME_LENGTH)
    @NotBlank(message = PositionConstants.NAME_REQUIRED)
    private String name;

    public PositionInsertDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
