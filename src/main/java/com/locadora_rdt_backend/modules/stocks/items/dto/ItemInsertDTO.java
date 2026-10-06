package com.locadora_rdt_backend.modules.stocks.items.dto;

import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

public class ItemInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = ItemConstants.NAME_MIN_LENGTH, max = ItemConstants.NAME_MAX_LENGTH,
            message = ItemConstants.NAME_LENGTH)
    @NotBlank(message = ItemConstants.FIELD_REQUIRED)
    private String name;

    @Size(min = ItemConstants.DESCRIPTION_MIN_LENGTH, max = ItemConstants.DESCRIPTION_MAX_LENGTH,
            message = ItemConstants.DESCRIPTION_LENGTH)
    @NotBlank(message = ItemConstants.FIELD_REQUIRED)
    private String description;

    @NotNull(message = ItemConstants.FIELD_REQUIRED)
    private Long categoryId;

    @DecimalMin(value = ItemConstants.MINIMUM_PRICE, message = ItemConstants.PRICE_MINIMUM)
    private BigDecimal price;

    public ItemInsertDTO() {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
