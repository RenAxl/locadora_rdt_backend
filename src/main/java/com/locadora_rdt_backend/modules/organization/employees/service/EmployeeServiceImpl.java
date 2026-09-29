package com.locadora_rdt_backend.modules.organization.employees.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.employees.constants.EmployeeConstants;
import com.locadora_rdt_backend.modules.organization.employees.dto.*;
import com.locadora_rdt_backend.modules.organization.employees.mapper.EmployeeMapper;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import com.locadora_rdt_backend.modules.organization.positions.model.Position;
import com.locadora_rdt_backend.modules.organization.positions.service.PositionService;
import com.locadora_rdt_backend.modules.organization.departments.model.Department;
import com.locadora_rdt_backend.modules.organization.departments.service.DepartmentService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final EmployeeMapper mapper;
    private final PositionService positionService;
    private final DepartmentService departmentService;
    private final AuthenticationFacade authenticationFacade;

    public EmployeeServiceImpl(
            EmployeeRepository repository,
            EmployeeMapper mapper,
            PositionService positionService,
            DepartmentService departmentService,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.positionService = positionService;
        this.departmentService = departmentService;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> findAllPaged(String name, PageRequest pageRequest) {

        Page<Employee> employees = repository.find(name, pageRequest);

        Page<EmployeeDTO> employeesDTO = employees.map(employee -> mapper.toDTO(employee));

        return employeesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO findById(Long id) {

        Optional<Employee> employeeOptional = repository.findById(id);

        if (!employeeOptional.isPresent()) {
            throw new ResourceNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND);
        }

        Employee employee = employeeOptional.get();

        EmployeeDTO employeeDTO = mapper.toDTO(employee);

        return employeeDTO;
    }

    @Override
    @Transactional
    public EmployeeDTO insert(EmployeeInsertDTO dto) {

        try {

            Employee employee = mapper.toEntity(dto);

            Position position = positionService.findEntityById(dto.getPositionId());
            Department department = departmentService.findEntityById(dto.getDepartmentId());

            employee.setPosition(position);
            employee.setDepartment(department);
            employee.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

            Employee savedEmployee = repository.save(employee);

            EmployeeDTO employeeDTO = mapper.toDTO(savedEmployee);

            return employeeDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(EmployeeConstants.POSITION_OR_DEPARTMENT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public EmployeeDTO update(Long id, EmployeeUpdateDTO dto) {

        try {

            Employee employee = repository.getOne(id);

            mapper.updateEntity(employee, dto);

            Position position = positionService.findEntityById(dto.getPositionId());
            Department department = departmentService.findEntityById(dto.getDepartmentId());

            employee.setPosition(position);
            employee.setDepartment(department);

            employee.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            Employee savedEmployee = repository.save(employee);

            EmployeeDTO employeeDTO = mapper.toDTO(savedEmployee);

            return employeeDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(EmployeeConstants.EMPLOYEE_POSITION_OR_DEPARTMENT_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(EmployeeConstants.ID_NOT_FOUND + id);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(EmployeeConstants.EMPTY_ID_LIST);
        }

        List<Employee> employees = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (Employee employee : employees) {
            existingIds.add(employee.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(EmployeeConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        repository.deleteAllByIds(ids);
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException(EmployeeConstants.ID_NOT_FOUND + id);
            }

        } catch (DataAccessException e) {

            throw new DatabaseException(EmployeeConstants.CHANGE_ACTIVE_STATUS_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePhotoDTO getEmployeePhotoById(Long id) {

        Optional<Employee> employeeOptional = repository.findById(id);

        if (!employeeOptional.isPresent()) {
            throw new ResourceNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND);
        }

        Employee employee = employeeOptional.get();

        byte[] photo = employee.getPhoto();

        if (photo == null || photo.length == 0) {
            return null;
        }

        String photoContentType = employee.getPhotoContentType();

        EmployeePhotoDTO employeePhotoDTO = new EmployeePhotoDTO(
                photo,
                photoContentType
        );

        return employeePhotoDTO;
    }

    @Override
    @Transactional
    public void updatePhoto(Long id, MultipartFile file) {

        Optional<Employee> employeeOptional = repository.findById(id);

        if (!employeeOptional.isPresent()) {
            throw new ResourceNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND);
        }

        Employee employee = employeeOptional.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(EmployeeConstants.EMPTY_PHOTO_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !EmployeeConstants.ALLOWED_PHOTO_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(EmployeeConstants.INVALID_PHOTO_TYPE);
        }

        if (file.getSize() > EmployeeConstants.MAX_PHOTO_SIZE_BYTES) {
            throw new IllegalArgumentException(EmployeeConstants.PHOTO_TOO_LARGE);
        }

        try {

            byte[] photo = file.getBytes();

            employee.setPhoto(photo);
            employee.setPhotoContentType(contentType);

        } catch (IOException e) {

            throw new FileException(EmployeeConstants.IMAGE_READ_ERROR, e);
        }

        employee.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        repository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Employee findEntityById(Long id) {

        Optional<Employee> employeeOptional = repository.findById(id);

        if (!employeeOptional.isPresent()) {
            throw new ResourceNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND);
        }

        Employee employee = employeeOptional.get();

        return employee;
    }

}
