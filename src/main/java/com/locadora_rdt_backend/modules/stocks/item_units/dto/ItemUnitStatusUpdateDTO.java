package com.locadora_rdt_backend.modules.stocks.item_units.dto;

import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ItemUnitStatusUpdateDTO {

    @NotNull(message = ItemUnitConstants.FIELD_REQUIRED)
    private ItemUnitStatus status;

    @Size(max = 255, message = ItemUnitConstants.REASON_LENGTH)
    private String reason;

    public ItemUnitStatus getStatus() {
        return status;
    }

    public void setStatus(ItemUnitStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
