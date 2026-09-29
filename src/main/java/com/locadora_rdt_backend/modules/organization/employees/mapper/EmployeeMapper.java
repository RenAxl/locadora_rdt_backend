package com.locadora_rdt_backend.modules.organization.employees.mapper;

import com.locadora_rdt_backend.modules.organization.departments.dto.DepartmentDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeInsertDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeUpdateDTO;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.positions.dto.PositionDTO;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeMapper() {
    }

    public EmployeeDTO toDTO(Employee entity) {

        EmployeeDTO dto = new EmployeeDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmployeeCode(entity.getEmployeeCode());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setSalary(entity.getSalary());
        dto.setHireDate(entity.getHireDate());
        dto.setTerminationDate(entity.getTerminationDate());
        dto.setEmploymentType(entity.getEmploymentType());
        dto.setActive(entity.getActive());
        dto.setPhotoContentType(entity.getPhotoContentType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        if (entity.getPosition() != null) {
            PositionDTO positionDTO = new PositionDTO();
            positionDTO.setId(entity.getPosition().getId());
            positionDTO.setName(entity.getPosition().getName());
            dto.setPosition(positionDTO);
        }

        if (entity.getDepartment() != null) {
            DepartmentDTO departmentDTO = new DepartmentDTO();
            departmentDTO.setId(entity.getDepartment().getId());
            departmentDTO.setName(entity.getDepartment().getName());
            dto.setDepartment(departmentDTO);
        }

        return dto;
    }

    public Employee toEntity(EmployeeInsertDTO dto) {

        Employee entity = new Employee();

        entity.setName(dto.getName());
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setSalary(dto.getSalary());
        entity.setHireDate(dto.getHireDate());
        entity.setTerminationDate(dto.getTerminationDate());
        entity.setEmploymentType(dto.getEmploymentType());
        entity.setActive(true);

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        return entity;
    }

    public void updateEntity(Employee entity, EmployeeUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setEmployeeCode(dto.getEmployeeCode());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setSalary(dto.getSalary());
        entity.setHireDate(dto.getHireDate());
        entity.setTerminationDate(dto.getTerminationDate());
        entity.setEmploymentType(dto.getEmploymentType());

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }
}
