package com.locadora_rdt_backend.modules.stocks.item_units.mapper;

import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitInsertDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.items.mapper.ItemMapper;
import org.springframework.stereotype.Component;

@Component
public class ItemUnitMapper {

    private final ItemMapper itemMapper;

    public ItemUnitMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ItemUnitDTO toDTO(ItemUnit entity) {

        ItemUnitDTO dto = new ItemUnitDTO();

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());

        if (entity.getItem() != null) {
            dto.setItem(itemMapper.toDTO(entity.getItem()));
        }

        dto.setAssetCode(entity.getAssetCode());
        dto.setStatus(entity.getStatus());
        dto.setConditionStatus(entity.getConditionStatus());
        dto.setPurchaseDate(entity.getPurchaseDate());
        dto.setNotes(entity.getNotes());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public ItemUnit toEntity(ItemUnitInsertDTO dto) {

        ItemUnit entity = new ItemUnit();

        entity.setConditionStatus(dto.getConditionStatus());
        entity.setPurchaseDate(dto.getPurchaseDate());
        entity.setNotes(dto.getNotes());
        entity.setStatus(ItemUnitStatus.AVAILABLE);
        entity.setActive(true);

        return entity;
    }

    public void updateEntity(ItemUnit entity, ItemUnitUpdateDTO dto) {

        entity.setConditionStatus(dto.getConditionStatus());
        entity.setPurchaseDate(dto.getPurchaseDate());
        entity.setNotes(dto.getNotes());
    }

}
