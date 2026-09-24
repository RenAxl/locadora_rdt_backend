package com.locadora_rdt_backend.modules.organization.customers.mapper;

import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerInsertDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerUpdateDTO;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerMapper() {
    }

    public CustomerDTO toDTO(Customer entity) {

        CustomerDTO dto = new CustomerDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCpf(entity.getCpf());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setActive(entity.getActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public Customer toEntity(CustomerInsertDTO dto) {

        Customer entity = new Customer();

        entity.setName(dto.getName());
        entity.setCpf(dto.getCpf());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setActive(true);

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        return entity;
    }

    public void updateEntity(Customer entity, CustomerUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setCpf(dto.getCpf());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setActive(true);

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

}
