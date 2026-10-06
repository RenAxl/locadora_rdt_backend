package com.locadora_rdt_backend.modules.stocks.stock_balances.mapper;

import com.locadora_rdt_backend.modules.stocks.item_units.repository.StockQuantitySummary;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import org.springframework.stereotype.Component;

@Component
public class StockBalanceMapper {

    public StockBalanceDTO toDTO(StockBalance entity, StockQuantitySummary quantities) {
        StockBalanceDTO dto = new StockBalanceDTO();
        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setItemId(entity.getItem().getId());
        dto.setItemName(entity.getItem().getName());
        dto.setTotalQuantity(Math.toIntExact(quantities.getTotalQuantity()));
        dto.setAvailableQuantity(Math.toIntExact(quantities.getAvailableQuantity()));
        dto.setUnavailableQuantity(Math.toIntExact(quantities.getUnavailableQuantity()));
        dto.setMaintenanceQuantity(Math.toIntExact(quantities.getMaintenanceQuantity()));
        dto.setDamagedQuantity(Math.toIntExact(quantities.getDamagedQuantity()));
        dto.setLostQuantity(Math.toIntExact(quantities.getLostQuantity()));
        dto.setMinimumQuantity(entity.getMinimumQuantity());
        dto.setLowStock(dto.getAvailableQuantity() < dto.getMinimumQuantity());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    public void updateEntity(StockBalance entity, StockBalanceMinimumUpdateDTO dto) {
        entity.setMinimumQuantity(dto.getMinimumQuantity());
    }
}
