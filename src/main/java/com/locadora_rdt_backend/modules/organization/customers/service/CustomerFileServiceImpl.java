package com.locadora_rdt_backend.modules.organization.customers.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileDTO;
import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerFileViewDTO;
import com.locadora_rdt_backend.modules.organization.customers.mapper.CustomerFileMapper;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.model.CustomerFile;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerFileRepository;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
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
public class CustomerFileServiceImpl implements CustomerFileService {

    private final CustomerFileRepository repository;
    private final CustomerRepository customerRepository;
    private final CustomerFileMapper mapper;

    public CustomerFileServiceImpl(
            CustomerFileRepository repository,
            CustomerRepository customerRepository,
            CustomerFileMapper mapper
    ) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CustomerFileDTO upload(Long customerId, String name, MultipartFile file) {

        Customer customer = findCustomerById(customerId);

        if (name == null || name.trim().isEmpty()) {
            throw new FileException(CustomerConstants.FILE_NAME_REQUIRED);
        }

        if (file == null || file.isEmpty()) {
            throw new FileException(CustomerConstants.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new FileException(CustomerConstants.INVALID_FILE_NAME);
        }

        if (file.getSize() > CustomerConstants.MAX_FILE_SIZE_BYTES) {
            throw new FileException(CustomerConstants.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !CustomerConstants.ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileException(CustomerConstants.INVALID_FILE_TYPE);
        }

        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD);
        normalizedFileName = normalizedFileName.replaceAll("[^\\p{ASCII}]", "");
        normalizedFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String storedFileName = UUID.randomUUID() + "-" + normalizedFileName;

        CustomerFile customerFile = new CustomerFile();
        customerFile.setCustomer(customer);
        customerFile.setName(name.trim());
        customerFile.setOriginalFileName(originalFileName);
        customerFile.setStoredFileName(storedFileName);
        customerFile.setContentType(contentType);
        customerFile.setSize(file.getSize());

        try {

            byte[] data = file.getBytes();

            customerFile.setData(data);

        } catch (IOException e) {

            throw new FileException(CustomerConstants.FILE_READ_ERROR, e);
        }

        CustomerFile savedFile = repository.save(customerFile);

        CustomerFileDTO customerFileDTO = mapper.toDTO(savedFile);

        return customerFileDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerFileDTO> findAllByCustomer(Long customerId) {

        findCustomerById(customerId);

        List<CustomerFile> files = repository.findByCustomerIdOrderByIdDesc(customerId);

        List<CustomerFileDTO> filesDTO = new ArrayList<>();

        for (CustomerFile file : files) {
            filesDTO.add(mapper.toDTO(file));
        }

        return filesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerFileViewDTO download(Long customerId, Long fileId) {

        CustomerFile customerFile = findFileBelongsToCustomer(customerId, fileId);

        CustomerFileViewDTO customerFileDTO = new CustomerFileViewDTO(
                customerFile.getOriginalFileName(),
                customerFile.getContentType(),
                customerFile.getData()
        );

        return customerFileDTO;
    }

    @Override
    @Transactional
    public void delete(Long customerId, Long fileId) {

        CustomerFile customerFile = findFileBelongsToCustomer(customerId, fileId);

        repository.delete(customerFile);
    }

    private CustomerFile findFileBelongsToCustomer(Long customerId, Long fileId) {

        findCustomerById(customerId);

        Optional<CustomerFile> fileOptional = repository.findById(fileId);

        if (!fileOptional.isPresent()) {
            throw new ResourceNotFoundException(CustomerConstants.FILE_NOT_FOUND_WITH_ID + fileId);
        }

        CustomerFile customerFile = fileOptional.get();

        if (!customerFile.getCustomer().getId().equals(customerId)) {
            throw new ResourceNotFoundException(CustomerConstants.FILE_DOES_NOT_BELONG_TO_CUSTOMER);
        }

        return customerFile;
    }

    private Customer findCustomerById(Long customerId) {

        Optional<Customer> customerOptional = customerRepository.findById(customerId);

        if (!customerOptional.isPresent()) {
            throw new ResourceNotFoundException(CustomerConstants.CUSTOMER_NOT_FOUND_WITH_ID + customerId);
        }

        Customer customer = customerOptional.get();

        return customer;
    }
}
