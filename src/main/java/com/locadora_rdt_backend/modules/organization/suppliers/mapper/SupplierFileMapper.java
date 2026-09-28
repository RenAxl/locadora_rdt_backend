package com.locadora_rdt_backend.modules.organization.suppliers.mapper;

import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.model.SupplierFile;
import org.springframework.stereotype.Component;

@Component
public class SupplierFileMapper {

    public SupplierFileMapper() {
    }

    public SupplierFileDTO toDTO(SupplierFile entity) {

        SupplierFileDTO dto = new SupplierFileDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setOriginalFileName(entity.getOriginalFileName());
        dto.setStoredFileName(entity.getStoredFileName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setSupplierId(entity.getSupplier().getId());

        return dto;
    }
}
