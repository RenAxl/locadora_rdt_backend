package com.locadora_rdt_backend.modules.stocks.categories.dto;

import com.locadora_rdt_backend.modules.stocks.categories.constants.CategoryConstants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class CategoryInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = CategoryConstants.NAME_MIN_LENGTH, max = CategoryConstants.NAME_MAX_LENGTH,
            message = CategoryConstants.NAME_LENGTH)
    @NotBlank(message = CategoryConstants.FIELD_REQUIRED)
    private String name;

    public CategoryInsertDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
