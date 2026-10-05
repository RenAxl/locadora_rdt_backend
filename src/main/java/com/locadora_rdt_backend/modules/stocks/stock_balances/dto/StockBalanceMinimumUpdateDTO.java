package com.locadora_rdt_backend.modules.stocks.stock_balances.dto;

import com.locadora_rdt_backend.modules.stocks.stock_balances.constants.StockBalanceConstants;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class StockBalanceMinimumUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = StockBalanceConstants.FIELD_REQUIRED)
    @Min(value = StockBalanceConstants.MINIMUM_QUANTITY, message = StockBalanceConstants.MINIMUM_QUANTITY_MINIMUM)
    private Integer minimumQuantity;

    public StockBalanceMinimumUpdateDTO() {
    }

    public Integer getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(Integer minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }
}
