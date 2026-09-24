package com.locadora_rdt_backend.modules.organization.departments.service;

import com.locadora_rdt_backend.modules.organization.departments.dto.*;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface DepartmentService {

    Page<DepartmentDTO> findAllPaged(String name, PageRequest pageRequest);

    DepartmentDTO findById(Long id);

    DepartmentDTO insert(DepartmentInsertDTO dto);

    DepartmentDTO update(Long id, DepartmentUpdateDTO dto);

    void delete(Long id);

    Department findEntityById(Long id);

}
