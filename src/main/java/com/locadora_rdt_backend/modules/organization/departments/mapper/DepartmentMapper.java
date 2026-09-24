package com.locadora_rdt_backend.modules.organization.departments.mapper;

import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentDTO;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentDetailsDTO;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentInsertDTO;
import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentUpdateDTO;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentMapper() {
    }

    public DepartmentDTO toDTO(Department entity) {

        DepartmentDTO dto = new DepartmentDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public DepartmentDetailsDTO toDetailsDTO(Department entity) {

        DepartmentDetailsDTO dto = new DepartmentDetailsDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Department toEntity(DepartmentInsertDTO dto) {

        Department entity = new Department();

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        return entity;
    }

    public void updateEntity(Department entity, DepartmentUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
    }
}
