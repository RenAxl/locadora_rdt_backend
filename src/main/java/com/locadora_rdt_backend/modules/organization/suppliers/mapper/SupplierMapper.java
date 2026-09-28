package com.locadora_rdt_backend.modules.organization.suppliers.mapper;

import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierInsertDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierUpdateDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierMapper() {
    }

    public SupplierDTO toDTO(Supplier entity) {

        SupplierDTO dto = new SupplierDTO();

        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setName(entity.getName());
        dto.setTradeName(entity.getTradeName());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCnpj(entity.getCnpj());
        dto.setEmail(entity.getEmail());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setAddress(entity.getAddress());
        dto.setImageContentType(entity.getImageContentType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Supplier toEntity(SupplierInsertDTO dto) {

        Supplier entity = new Supplier();

        entity.setName(dto.getName().trim());
        entity.setTradeName(dto.getTradeName().trim());
        entity.setCompanyName(dto.getCompanyName().trim());
        entity.setCnpj(dto.getCnpj().trim());
        entity.setEmail(dto.getEmail().trim());
        entity.setPhoneNumber(dto.getPhoneNumber().trim());
        entity.setAddress(dto.getAddress());

        return entity;
    }

    public void updateEntity(Supplier entity, SupplierUpdateDTO dto) {

        entity.setName(dto.getName().trim());
        entity.setTradeName(dto.getTradeName().trim());
        entity.setCompanyName(dto.getCompanyName().trim());
        entity.setCnpj(dto.getCnpj().trim());
        entity.setEmail(dto.getEmail().trim());
        entity.setPhoneNumber(dto.getPhoneNumber().trim());
        entity.setAddress(dto.getAddress());
    }

}
