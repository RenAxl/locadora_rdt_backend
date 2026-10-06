package com.locadora_rdt_backend.modules.stocks.stock_movements.dto;

import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import java.io.Serializable;
import java.time.Instant;

public class StockMovementDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId;
    private String itemName;
    private StockMovementType type;
    private Integer quantity;
    private String reason;
    private Instant createdAt;
    private String createdBy;

    private Long itemUnitId;

    private String assetCode;

    private ItemUnitStatus previousStatus;

    private ItemUnitStatus newStatus;

    public StockMovementDTO() {
    }

    public Long getItemUnitId() {
        return itemUnitId;
    }

    public void setItemUnitId(Long itemUnitId) {
        this.itemUnitId = itemUnitId;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public ItemUnitStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ItemUnitStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ItemUnitStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ItemUnitStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public StockMovementType getType() {
        return type;
    }

    public void setType(StockMovementType type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
