package com.locadora_rdt_backend.modules.stocks.item_units.dto;

import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;

import java.time.Instant;
import java.time.LocalDate;

import java.io.Serializable;

public class ItemUnitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long version;

    private ItemDTO item;

    private String assetCode;

    private ItemUnitStatus status;

    private ItemUnitCondition conditionStatus;

    private LocalDate purchaseDate;

    private String notes;

    private Boolean active = true;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    public ItemUnitDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public ItemUnitStatus getStatus() {
        return status;
    }

    public void setStatus(ItemUnitStatus status) {
        this.status = status;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
