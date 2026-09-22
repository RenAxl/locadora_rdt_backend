package com.locadora_rdt_backend.modules.organization.customers.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import com.locadora_rdt_backend.modules.organization.customers.dto.*;
import com.locadora_rdt_backend.modules.organization.customers.mapper.CustomerMapper;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public CustomerServiceImpl(
            CustomerRepository repository,
            CustomerMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerDTO> findAllPaged(String name, PageRequest pageRequest) {

        Page<Customer> customers = repository.find(name, pageRequest);

        Page<CustomerDTO> customersDTO = customers.map(customer -> mapper.toDTO(customer));

        return customersDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailsDTO findById(Long id) {

        Optional<Customer> customerOptional = repository.findById(id);

        if (!customerOptional.isPresent()) {
            throw new ResourceNotFoundException(CustomerConstants.CUSTOMER_NOT_FOUND);
        }

        Customer customer = customerOptional.get();

        CustomerDetailsDTO customerDTO = mapper.toDetailsDTO(customer);

        return customerDTO;
    }

    @Override
    @Transactional
    public CustomerDTO insert(CustomerInsertDTO dto) {

        Customer customer = mapper.toEntity(dto);

        customer.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        Customer savedCustomer = repository.save(customer);

        CustomerDTO customerDTO = mapper.toDTO(savedCustomer);

        return customerDTO;
    }

    @Override
    @Transactional
    public CustomerDTO update(Long id, CustomerUpdateDTO dto) {

        try {

            Customer customer = repository.getOne(id);

            mapper.updateEntity(customer, dto);

            customer.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            Customer savedCustomer = repository.save(customer);

            CustomerDTO customerDTO = mapper.toDTO(savedCustomer);

            return customerDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(CustomerConstants.ID_NOT_FOUND + id);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(CustomerConstants.ID_NOT_FOUND + id);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(CustomerConstants.EMPTY_ID_LIST);
        }

        List<Customer> customers = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (Customer customer : customers) {
            existingIds.add(customer.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(CustomerConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        repository.deleteAllByIds(ids);
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException(CustomerConstants.ID_NOT_FOUND + id);
            }

        } catch (DataAccessException e) {

            throw new DatabaseException(CustomerConstants.CHANGE_ACTIVE_STATUS_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerPhotoDTO getCustomerPhotoById(Long id) {

        Optional<Customer> customerOptional = repository.findById(id);

        if (!customerOptional.isPresent()) {
            throw new ResourceNotFoundException(CustomerConstants.CUSTOMER_NOT_FOUND);
        }

        Customer customer = customerOptional.get();

        byte[] photo = customer.getPhoto();

        if (photo == null || photo.length == 0) {
            return null;
        }

        String photoContentType = customer.getPhotoContentType();

        CustomerPhotoDTO customerPhotoDTO = new CustomerPhotoDTO(
                photo,
                photoContentType
        );

        return customerPhotoDTO;
    }

    @Override
    @Transactional
    public void updatePhoto(Long id, MultipartFile file) {

        Optional<Customer> customerOptional = repository.findById(id);

        if (!customerOptional.isPresent()) {
            throw new ResourceNotFoundException(CustomerConstants.CUSTOMER_NOT_FOUND);
        }

        Customer customer = customerOptional.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(CustomerConstants.EMPTY_PHOTO_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !CustomerConstants.ALLOWED_PHOTO_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(CustomerConstants.INVALID_PHOTO_TYPE);
        }

        if (file.getSize() > CustomerConstants.MAX_PHOTO_SIZE_BYTES) {
            throw new IllegalArgumentException(CustomerConstants.PHOTO_TOO_LARGE);
        }

        try {

            byte[] photo = file.getBytes();

            customer.setPhoto(photo);
            customer.setPhotoContentType(contentType);

        } catch (IOException e) {

            throw new FileException(CustomerConstants.IMAGE_READ_ERROR, e);
        }

        customer.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        repository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Customer findEntityById(Long id) {

        Optional<Customer> customerOptional = repository.findById(id);

        if (!customerOptional.isPresent()) {
            throw new ResourceNotFoundException(CustomerConstants.CUSTOMER_NOT_FOUND);
        }

        Customer customer = customerOptional.get();

        return customer;
    }

}
