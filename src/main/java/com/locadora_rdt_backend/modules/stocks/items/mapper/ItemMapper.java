package com.locadora_rdt_backend.modules.stocks.items.mapper;

import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemInsertDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemMapper() {
    }

    public ItemDTO toDTO(Item entity) {

        ItemDTO dto = new ItemDTO();

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        if (entity.getCategory() != null) {
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setId(entity.getCategory().getId());
            categoryDTO.setName(entity.getCategory().getName());
            categoryDTO.setActive(entity.getCategory().getActive());
            categoryDTO.setImageContentType(entity.getCategory().getImageContentType());
            dto.setCategory(categoryDTO);
        }

        dto.setPrice(entity.getPrice());
        dto.setActive(entity.getActive());
        dto.setImageContentType(entity.getImageContentType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Item toEntity(ItemInsertDTO dto) {

        Item entity = new Item();

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setActive(true);

        return entity;
    }

    public void updateEntity(Item entity, ItemUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
    }

}
