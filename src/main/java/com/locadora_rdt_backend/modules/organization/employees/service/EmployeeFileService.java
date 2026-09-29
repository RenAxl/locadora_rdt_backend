package com.locadora_rdt_backend.modules.organization.employees.service;

import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileViewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeFileService {

    EmployeeFileDTO upload(Long employeeId, String name, MultipartFile file);

    List<EmployeeFileDTO> findAllByEmployee(Long employeeId);

    EmployeeFileViewDTO download(Long employeeId, Long fileId);

    void delete(Long employeeId, Long fileId);
}
