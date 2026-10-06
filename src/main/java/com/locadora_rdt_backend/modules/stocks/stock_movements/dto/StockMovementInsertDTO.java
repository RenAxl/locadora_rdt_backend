package com.locadora_rdt_backend.modules.stocks.stock_movements.dto;

import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.constants.StockMovementConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.locadora_rdt_backend.common.json.IntegerDeserializer;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class StockMovementInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = StockMovementConstants.FIELD_REQUIRED)
    private Long itemId;

    @NotNull(message = StockMovementConstants.FIELD_REQUIRED)
    private StockMovementType type;

    @NotNull(message = StockMovementConstants.FIELD_REQUIRED)
    @Min(value = StockMovementConstants.MINIMUM_QUANTITY, message = StockMovementConstants.QUANTITY_MINIMUM)
    @JsonDeserialize(using = IntegerDeserializer.class)
    private Integer quantity;

    @Size(max = 255, message = StockMovementConstants.REASON_LENGTH)
    private String reason;

    private Long itemUnitId;

    private ItemUnitStatus status;

    public StockMovementInsertDTO() {
    }

    @JsonIgnore
    @AssertTrue(message = "Entradas e saídas exigem quantidade positiva; alteração de status exige uma unidade")
    public boolean isQuantityValid() {
        if (quantity == null || type == null) {
            return true;
        }

        if (type == StockMovementType.ADJUSTMENT) {
            return quantity >= 0;
        }

        if (type == StockMovementType.STATUS_CHANGE) {
            return quantity == 1;
        }

        return quantity > 0;
    }

    public Long getItemUnitId() {
        return itemUnitId;
    }

    public void setItemUnitId(Long itemUnitId) {
        this.itemUnitId = itemUnitId;
    }

    public ItemUnitStatus getStatus() {
        return status;
    }

    public void setStatus(ItemUnitStatus status) {
        this.status = status;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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
}
