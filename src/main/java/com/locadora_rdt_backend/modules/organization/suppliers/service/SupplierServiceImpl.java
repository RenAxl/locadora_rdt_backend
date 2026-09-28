package com.locadora_rdt_backend.modules.organization.suppliers.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.suppliers.constants.SupplierConstants;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.*;
import com.locadora_rdt_backend.modules.organization.suppliers.mapper.SupplierMapper;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository repository;
    private final SupplierMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public SupplierServiceImpl(
            SupplierRepository repository,
            SupplierMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> findAllPaged(String name, PageRequest pageRequest) {

        String normalizedName = "";

        if (name != null) {
            normalizedName = name.trim();
        }

        Page<Supplier> suppliers = repository.findByNameContainingIgnoreCase(normalizedName, pageRequest);

        Page<SupplierDTO> suppliersDTO = suppliers.map(supplier -> mapper.toDTO(supplier));

        return suppliersDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO findById(Long id) {

        Optional<Supplier> supplierOptional = repository.findById(id);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        SupplierDTO supplierDTO = mapper.toDTO(supplier);

        return supplierDTO;
    }

    @Override
    @Transactional
    public SupplierDTO insert(SupplierInsertDTO dto) {

        Supplier supplier = mapper.toEntity(dto);

        if (repository.existsByCnpjAndIdNot(supplier.getCnpj(), SupplierConstants.NEW_ENTITY_ID)) {
            throw new IllegalArgumentException(SupplierConstants.CNPJ_ALREADY_EXISTS);
        }

        if (repository.existsByEmailIgnoreCaseAndIdNot(supplier.getEmail(), SupplierConstants.NEW_ENTITY_ID)) {
            throw new IllegalArgumentException(SupplierConstants.EMAIL_ALREADY_EXISTS);
        }

        if (repository.existsByPhoneNumberAndIdNot(supplier.getPhoneNumber(), SupplierConstants.NEW_ENTITY_ID)) {
            throw new IllegalArgumentException(SupplierConstants.PHONE_ALREADY_EXISTS);
        }

        supplier.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        Supplier savedSupplier = repository.save(supplier);

        SupplierDTO supplierDTO = mapper.toDTO(savedSupplier);

        return supplierDTO;
    }

    @Override
    @Transactional
    public SupplierDTO update(Long id, SupplierUpdateDTO dto) {

        Optional<Supplier> supplierOptional = repository.findById(id);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        mapper.updateEntity(supplier, dto);

        if (repository.existsByCnpjAndIdNot(supplier.getCnpj(), id)) {
            throw new IllegalArgumentException(SupplierConstants.CNPJ_ALREADY_EXISTS);
        }

        if (repository.existsByEmailIgnoreCaseAndIdNot(supplier.getEmail(), id)) {
            throw new IllegalArgumentException(SupplierConstants.EMAIL_ALREADY_EXISTS);
        }

        if (repository.existsByPhoneNumberAndIdNot(supplier.getPhoneNumber(), id)) {
            throw new IllegalArgumentException(SupplierConstants.PHONE_ALREADY_EXISTS);
        }

        supplier.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        Supplier savedSupplier = repository.save(supplier);

        SupplierDTO supplierDTO = mapper.toDTO(savedSupplier);

        return supplierDTO;
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Optional<Supplier> supplierOptional = repository.findById(id);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        try {

            repository.delete(supplier);
            repository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(SupplierConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierImageDTO getSupplierImageById(Long id) {

        Optional<Supplier> supplierOptional = repository.findById(id);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        byte[] image = supplier.getImage();

        if (image == null || image.length == 0) {
            return null;
        }

        String imageContentType = supplier.getImageContentType();

        SupplierImageDTO supplierImageDTO = new SupplierImageDTO(
                image,
                imageContentType
        );

        return supplierImageDTO;
    }

    @Override
    @Transactional
    public void updateImage(Long id, MultipartFile file) {

        Optional<Supplier> supplierOptional = repository.findById(id);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(SupplierConstants.EMPTY_IMAGE_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !SupplierConstants.ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(SupplierConstants.INVALID_IMAGE_TYPE);
        }

        if (file.getSize() > SupplierConstants.MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException(SupplierConstants.IMAGE_TOO_LARGE);
        }

        try {

            byte[] image = file.getBytes();

            supplier.setImage(image);
            supplier.setImageContentType(contentType);

        } catch (IOException e) {

            throw new FileException(SupplierConstants.IMAGE_READ_ERROR, e);
        }

        supplier.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        repository.save(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier findEntityById(Long id) {

        Optional<Supplier> supplierOptional = repository.findById(id);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        return supplier;
    }

}
