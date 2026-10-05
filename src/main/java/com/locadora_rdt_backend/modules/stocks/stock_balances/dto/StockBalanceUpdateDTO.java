package com.locadora_rdt_backend.modules.stocks.stock_balances.dto;

import com.locadora_rdt_backend.modules.stocks.stock_balances.constants.StockBalanceConstants;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class StockBalanceUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = StockBalanceConstants.FIELD_REQUIRED)
    @Min(value = StockBalanceConstants.MINIMUM_QUANTITY, message = StockBalanceConstants.TOTAL_QUANTITY_MINIMUM)
    private Integer totalQuantity;

    @NotNull(message = StockBalanceConstants.FIELD_REQUIRED)
    @Min(value = StockBalanceConstants.MINIMUM_QUANTITY, message = StockBalanceConstants.RESERVED_QUANTITY_MINIMUM)
    private Integer reservedQuantity;

    @NotNull(message = StockBalanceConstants.FIELD_REQUIRED)
    @Min(value = StockBalanceConstants.MINIMUM_QUANTITY, message = StockBalanceConstants.UNAVAILABLE_QUANTITY_MINIMUM)
    private Integer unavailableQuantity;

    @NotNull(message = StockBalanceConstants.FIELD_REQUIRED)
    @Min(value = StockBalanceConstants.MINIMUM_QUANTITY, message = StockBalanceConstants.MINIMUM_QUANTITY_MINIMUM)
    private Integer minimumQuantity;

    public StockBalanceUpdateDTO() {
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getUnavailableQuantity() {
        return unavailableQuantity;
    }

    public void setUnavailableQuantity(Integer unavailableQuantity) {
        this.unavailableQuantity = unavailableQuantity;
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }
}
