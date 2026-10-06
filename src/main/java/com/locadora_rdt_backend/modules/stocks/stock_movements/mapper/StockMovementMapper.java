package com.locadora_rdt_backend.modules.stocks.stock_movements.mapper;

import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementInsertDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementMapper() {
    }

    public StockMovementDTO toDTO(StockMovement entity) {

        StockMovementDTO dto = new StockMovementDTO();

        dto.setId(entity.getId());
        dto.setItemId(entity.getItem().getId());
        dto.setItemName(entity.getItem().getName());
        if (entity.getItemUnit() != null) {
            dto.setItemUnitId(entity.getItemUnit().getId());
            dto.setAssetCode(entity.getItemUnit().getAssetCode());
        }
        dto.setPreviousStatus(entity.getPreviousStatus());
        dto.setNewStatus(entity.getNewStatus());
        dto.setType(entity.getType());
        dto.setQuantity(entity.getQuantity());
        dto.setReason(entity.getReason());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());

        return dto;
    }

    public StockMovement toEntity(StockMovementInsertDTO dto) {

        StockMovement entity = new StockMovement();

        entity.setType(dto.getType());
        entity.setQuantity(dto.getQuantity());
        entity.setReason(dto.getReason());

        return entity;
    }

}
