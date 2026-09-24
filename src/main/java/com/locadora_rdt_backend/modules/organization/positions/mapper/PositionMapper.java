package com.locadora_rdt_backend.modules.organization.positions.mapper;

import com.locadora_rdt_backend.modules.organization.positions.dto.PositionDTO;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionInsertDTO;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionUpdateDTO;
import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public PositionMapper() {
    }

    public PositionDTO toDTO(Position entity) {

        PositionDTO dto = new PositionDTO();

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Position toEntity(PositionInsertDTO dto) {

        Position entity = new Position();

        entity.setName(dto.getName());

        return entity;
    }

    public void updateEntity(Position entity, PositionUpdateDTO dto) {

        entity.setName(dto.getName());

    }

}
