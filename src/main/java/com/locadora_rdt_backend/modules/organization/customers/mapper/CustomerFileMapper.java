package com.locadora_rdt_backend.modules.organization.customers.mapper;

import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileDTO;
import com.locadora_rdt_backend.modules.organization.customers.model.CustomerFile;
import org.springframework.stereotype.Component;

@Component
public class CustomerFileMapper {

    public CustomerFileMapper() {
    }

    public CustomerFileDTO toDTO(CustomerFile entity) {

        CustomerFileDTO dto = new CustomerFileDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setOriginalFileName(entity.getOriginalFileName());
        dto.setStoredFileName(entity.getStoredFileName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCustomerId(entity.getCustomer().getId());

        return dto;
    }
}
