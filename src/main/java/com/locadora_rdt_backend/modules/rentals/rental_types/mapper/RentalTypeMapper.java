package com.locadora_rdt_backend.modules.rentals.rental_types.mapper;

import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeInsertDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.dto.RentalTypeUpdateDTO;
import com.locadora_rdt_backend.modules.rentals.rental_types.model.RentalType;
import org.springframework.stereotype.Component;

@Component
public class RentalTypeMapper {

    public RentalTypeMapper() {
    }

    public RentalTypeDTO toDTO(RentalType entity) {

        RentalTypeDTO dto = new RentalTypeDTO();

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setDays(entity.getDays());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public RentalType toEntity(RentalTypeInsertDTO dto) {

        RentalType entity = new RentalType();

        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDays(dto.getDays());
        entity.setActive(true);

        return entity;
    }

    public void updateEntity(RentalType entity, RentalTypeUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setDays(dto.getDays());
    }

}
