package com.locadora_rdt_backend.modules.rentals.rental.dto;

import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class RentalItemInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = RentalConstants.FIELD_REQUIRED)
    private Long itemId;
    @NotNull(message = RentalConstants.FIELD_REQUIRED)
    @Min(value = RentalConstants.MIN_ITEM_QUANTITY, message = RentalConstants.INVALID_ITEM_QUANTITY)
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal additionalFee;

    public RentalItemInsertDTO() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(BigDecimal additionalFee) {
        this.additionalFee = additionalFee;
    }
}
