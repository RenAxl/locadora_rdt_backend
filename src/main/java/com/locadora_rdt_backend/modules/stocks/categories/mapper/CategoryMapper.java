package com.locadora_rdt_backend.modules.stocks.categories.mapper;

import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryInsertDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryMapper() {
    }

    public CategoryDTO toDTO(Category entity) {

        CategoryDTO dto = new CategoryDTO();

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setName(entity.getName());
        dto.setActive(entity.getActive());
        dto.setImageContentType(entity.getImageContentType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Category toEntity(CategoryInsertDTO dto) {

        Category entity = new Category();

        entity.setName(dto.getName());
        entity.setActive(true);

        return entity;
    }

    public void updateEntity(Category entity, CategoryUpdateDTO dto) {

        entity.setName(dto.getName());
    }

}
