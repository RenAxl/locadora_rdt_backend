package com.locadora_rdt_backend.modules.stocks.item_units.dto;

import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import java.io.Serializable;

public class ItemUnitUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = ItemUnitConstants.FIELD_REQUIRED)
    private Long itemId;

    @NotNull(message = ItemUnitConstants.FIELD_REQUIRED)
    private ItemUnitCondition conditionStatus;

    private LocalDate purchaseDate;

    @Size(max = ItemUnitConstants.NOTES_MAX_LENGTH, message = ItemUnitConstants.NOTES_LENGTH)
    private String notes;

    public ItemUnitUpdateDTO() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public ItemUnitCondition getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(ItemUnitCondition conditionStatus) {
        this.conditionStatus = conditionStatus;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
