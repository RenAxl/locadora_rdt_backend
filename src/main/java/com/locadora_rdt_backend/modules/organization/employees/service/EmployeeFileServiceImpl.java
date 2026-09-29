package com.locadora_rdt_backend.modules.organization.employees.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.employees.constants.EmployeeConstants;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileDTO;
import com.locadora_rdt_backend.modules.organization.employees.dto.EmployeeFileViewDTO;
import com.locadora_rdt_backend.modules.organization.employees.mapper.EmployeeFileMapper;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.employees.model.EmployeeFile;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeFileRepository;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeFileServiceImpl implements EmployeeFileService {

    private final EmployeeFileRepository repository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeFileMapper mapper;

    public EmployeeFileServiceImpl(
            EmployeeFileRepository repository,
            EmployeeRepository employeeRepository,
            EmployeeFileMapper mapper
    ) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EmployeeFileDTO upload(Long employeeId, String name, MultipartFile file) {

        Employee employee = findEmployeeById(employeeId);

        if (name == null || name.trim().isEmpty()) {
            throw new FileException(EmployeeConstants.FILE_NAME_REQUIRED);
        }

        if (file == null || file.isEmpty()) {
            throw new FileException(EmployeeConstants.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new FileException(EmployeeConstants.INVALID_FILE_NAME);
        }

        if (file.getSize() > EmployeeConstants.MAX_FILE_SIZE_BYTES) {
            throw new FileException(EmployeeConstants.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !EmployeeConstants.ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileException(EmployeeConstants.INVALID_FILE_TYPE);
        }

        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD);
        normalizedFileName = normalizedFileName.replaceAll("[^\\p{ASCII}]", "");
        normalizedFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String storedFileName = UUID.randomUUID() + "-" + normalizedFileName;

        EmployeeFile employeeFile = new EmployeeFile();
        employeeFile.setEmployee(employee);
        employeeFile.setName(name.trim());
        employeeFile.setOriginalFileName(originalFileName);
        employeeFile.setStoredFileName(storedFileName);
        employeeFile.setContentType(contentType);
        employeeFile.setSize(file.getSize());

        try {

            byte[] data = file.getBytes();

            employeeFile.setData(data);

        } catch (IOException e) {

            throw new FileException(EmployeeConstants.FILE_READ_ERROR, e);
        }

        EmployeeFile savedFile = repository.save(employeeFile);

        EmployeeFileDTO employeeFileDTO = mapper.toDTO(savedFile);

        return employeeFileDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeFileDTO> findAllByEmployee(Long employeeId) {

        findEmployeeById(employeeId);

        List<EmployeeFile> files = repository.findByEmployeeIdOrderByIdDesc(employeeId);

        List<EmployeeFileDTO> filesDTO = new ArrayList<>();

        for (EmployeeFile file : files) {
            filesDTO.add(mapper.toDTO(file));
        }

        return filesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeFileViewDTO download(Long employeeId, Long fileId) {

        EmployeeFile employeeFile = findFileBelongsToEmployee(employeeId, fileId);

        EmployeeFileViewDTO employeeFileDTO = new EmployeeFileViewDTO(
                employeeFile.getOriginalFileName(),
                employeeFile.getContentType(),
                employeeFile.getData()
        );

        return employeeFileDTO;
    }

    @Override
    @Transactional
    public void delete(Long employeeId, Long fileId) {

        EmployeeFile employeeFile = findFileBelongsToEmployee(employeeId, fileId);

        repository.delete(employeeFile);
    }

    private EmployeeFile findFileBelongsToEmployee(Long employeeId, Long fileId) {

        findEmployeeById(employeeId);

        Optional<EmployeeFile> fileOptional = repository.findById(fileId);

        if (!fileOptional.isPresent()) {
            throw new ResourceNotFoundException(EmployeeConstants.FILE_NOT_FOUND_WITH_ID + fileId);
        }

        EmployeeFile employeeFile = fileOptional.get();

        if (!employeeFile.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException(EmployeeConstants.FILE_DOES_NOT_BELONG_TO_EMPLOYEE);
        }

        return employeeFile;
    }

    private Employee findEmployeeById(Long employeeId) {

        Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);

        if (!employeeOptional.isPresent()) {
            throw new ResourceNotFoundException(EmployeeConstants.EMPLOYEE_NOT_FOUND_WITH_ID + employeeId);
        }

        Employee employee = employeeOptional.get();

        return employee;
    }
}
