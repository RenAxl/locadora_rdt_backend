package com.locadora_rdt_backend.modules.stocks.stock_balances.mapper;

import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import org.springframework.stereotype.Component;

@Component
public class StockBalanceMapper {

    public StockBalanceMapper() {
    }

    public StockBalanceDTO toDTO(StockBalance entity) {

        StockBalanceDTO dto = new StockBalanceDTO();

        int totalQuantity = 0;
        int reservedQuantity = 0;
        int unavailableQuantity = 0;
        int minimumQuantity = 0;

        if (entity.getTotalQuantity() != null) {
            totalQuantity = entity.getTotalQuantity();
        }

        if (entity.getReservedQuantity() != null) {
            reservedQuantity = entity.getReservedQuantity();
        }

        if (entity.getUnavailableQuantity() != null) {
            unavailableQuantity = entity.getUnavailableQuantity();
        }

        if (entity.getMinimumQuantity() != null) {
            minimumQuantity = entity.getMinimumQuantity();
        }

        int availableQuantity = totalQuantity - reservedQuantity - unavailableQuantity;

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());

        if (entity.getItem() != null) {
            dto.setItemId(entity.getItem().getId());
            dto.setItemName(entity.getItem().getName());
        }

        dto.setTotalQuantity(totalQuantity);
        dto.setReservedQuantity(reservedQuantity);
        dto.setUnavailableQuantity(unavailableQuantity);
        dto.setAvailableQuantity(availableQuantity);
        dto.setMinimumQuantity(minimumQuantity);
        dto.setLowStock(availableQuantity <= minimumQuantity);
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public void updateEntity(StockBalance entity, StockBalanceMinimumUpdateDTO dto) {

        entity.setMinimumQuantity(dto.getMinimumQuantity());
    }

    public void updateEntity(StockBalance entity, StockBalanceUpdateDTO dto) {

        entity.setTotalQuantity(dto.getTotalQuantity());
        entity.setReservedQuantity(dto.getReservedQuantity());
        entity.setUnavailableQuantity(dto.getUnavailableQuantity());
        entity.setMinimumQuantity(dto.getMinimumQuantity());
    }

}
