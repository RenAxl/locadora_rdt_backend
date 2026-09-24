package com.locadora_rdt_backend.modules.organization.departments.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.departments.constants.DepartmentConstants;
import com.locadora_rdt_backend.modules.organization.departments.dto.*;
import com.locadora_rdt_backend.modules.organization.departments.mapper.DepartmentMapper;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import com.locadora_rdt_backend.modules.organization.departments.repository.DepartmentRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository repository;
    private final DepartmentMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public DepartmentServiceImpl(
            DepartmentRepository repository,
            DepartmentMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentDTO> findAllPaged(String name, PageRequest pageRequest) {

        String search = "";

        if (name != null) {
            search = name.trim();
        }

        Page<Department> departments = repository.find(search, pageRequest);

        Page<DepartmentDTO> departmentsDTO = departments.map(department -> mapper.toDTO(department));

        return departmentsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDTO findById(Long id) {

        Optional<Department> departmentOptional = repository.findById(id);

        if (!departmentOptional.isPresent()) {
            throw new ResourceNotFoundException(DepartmentConstants.DEPARTMENT_NOT_FOUND);
        }

        Department department = departmentOptional.get();

        DepartmentDTO departmentDTO = mapper.toDTO(department);

        return departmentDTO;
    }

    @Override
    @Transactional
    public DepartmentDTO insert(DepartmentInsertDTO dto) {

        Department department = mapper.toEntity(dto);

        department.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        Department savedDepartment = repository.save(department);

        DepartmentDTO departmentDTO = mapper.toDTO(savedDepartment);

        return departmentDTO;
    }

    @Override
    @Transactional
    public DepartmentDTO update(Long id, DepartmentUpdateDTO dto) {

        try {

            Department department = repository.getOne(id);

            mapper.updateEntity(department, dto);

            department.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            Department savedDepartment = repository.save(department);

            DepartmentDTO departmentDTO = mapper.toDTO(savedDepartment);

            return departmentDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(DepartmentConstants.DEPARTMENT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Optional<Department> departmentOptional = repository.findById(id);

        if (!departmentOptional.isPresent()) {
            throw new ResourceNotFoundException(DepartmentConstants.DEPARTMENT_NOT_FOUND);
        }

        try {

            repository.deleteById(id);
            repository.flush();

        } catch (EmptyResultDataAccessException e) {

            throw new ResourceNotFoundException(DepartmentConstants.DEPARTMENT_NOT_FOUND);

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(DepartmentConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Department findEntityById(Long id) {

        Optional<Department> departmentOptional = repository.findById(id);

        if (!departmentOptional.isPresent()) {
            throw new ResourceNotFoundException(DepartmentConstants.DEPARTMENT_NOT_FOUND);
        }

        Department department = departmentOptional.get();

        return department;
    }
}
