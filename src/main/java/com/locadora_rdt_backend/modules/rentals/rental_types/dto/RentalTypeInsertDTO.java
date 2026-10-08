package com.locadora_rdt_backend.modules.rentals.rental_types.dto;

import com.locadora_rdt_backend.modules.rentals.rental_types.constants.RentalTypeConstants;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class RentalTypeInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = RentalTypeConstants.NAME_MIN_LENGTH, max = RentalTypeConstants.NAME_MAX_LENGTH,
            message = RentalTypeConstants.NAME_LENGTH)
    @NotBlank(message = RentalTypeConstants.FIELD_REQUIRED)
    private String name;

    @Size(min = RentalTypeConstants.TYPE_MIN_LENGTH, max = RentalTypeConstants.TYPE_MAX_LENGTH,
            message = RentalTypeConstants.TYPE_LENGTH)
    @NotBlank(message = RentalTypeConstants.FIELD_REQUIRED)
    private String type;

    @NotNull(message = RentalTypeConstants.FIELD_REQUIRED)
    @Min(value = RentalTypeConstants.DAYS_MIN_VALUE, message = RentalTypeConstants.DAYS_MIN_VALUE_MESSAGE)
    private Integer days;

    public RentalTypeInsertDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
