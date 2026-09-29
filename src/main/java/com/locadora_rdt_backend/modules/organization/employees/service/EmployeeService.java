package com.locadora_rdt_backend.modules.organization.employees.service;

import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeePhotoDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeInsertDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeUpdateDTO;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {

    Page<EmployeeDTO> findAllPaged(String name, PageRequest pageRequest);

    EmployeeDTO findById(Long id);

    EmployeeDTO insert(EmployeeInsertDTO dto);

    EmployeeDTO update(Long id, EmployeeUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    EmployeePhotoDTO getEmployeePhotoById(Long id);

    void updatePhoto(Long id, MultipartFile file);

    Employee findEntityById(Long id);

}
