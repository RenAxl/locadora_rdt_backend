package com.locadora_rdt_backend.modules.organization.employees.mapper;

import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileDTO;
import com.locadora_rdt_backend.modules.organization.employees.model.EmployeeFile;
import org.springframework.stereotype.Component;

@Component
public class EmployeeFileMapper {

    public EmployeeFileMapper() {
    }

    public EmployeeFileDTO toDTO(EmployeeFile entity) {

        EmployeeFileDTO dto = new EmployeeFileDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setOriginalFileName(entity.getOriginalFileName());
        dto.setStoredFileName(entity.getStoredFileName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setEmployeeId(entity.getEmployee().getId());

        return dto;
    }
}
