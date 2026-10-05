package com.locadora_rdt_backend.modules.stocks.item_units.dto;

import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import java.io.Serializable;

public class ItemUnitInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = ItemUnitConstants.FIELD_REQUIRED)
    private Long itemId;

    @NotBlank(message = ItemUnitConstants.FIELD_REQUIRED)
    @Size(max = ItemUnitConstants.ASSET_CODE_MAX_LENGTH, message = ItemUnitConstants.ASSET_CODE_LENGTH)
    private String assetCode;

    @Size(max = ItemUnitConstants.SERIAL_NUMBER_MAX_LENGTH, message = ItemUnitConstants.SERIAL_NUMBER_LENGTH)
    private String serialNumber;

    @NotBlank(message = ItemUnitConstants.FIELD_REQUIRED)
    @Pattern(regexp = ItemUnitConstants.CONDITION_PATTERN, message = ItemUnitConstants.INVALID_CONDITION)
    private String conditionStatus;

    private LocalDate purchaseDate;

    @Size(max = ItemUnitConstants.NOTES_MAX_LENGTH, message = ItemUnitConstants.NOTES_LENGTH)
    private String notes;

    public ItemUnitInsertDTO() {
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getConditionStatus() {
        return conditionStatus;
    }

    public void setConditionStatus(String conditionStatus) {
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
