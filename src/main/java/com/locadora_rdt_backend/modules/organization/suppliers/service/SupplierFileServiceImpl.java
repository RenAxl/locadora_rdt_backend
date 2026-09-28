package com.locadora_rdt_backend.modules.organization.suppliers.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.organization.suppliers.constants.SupplierConstants;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.dto.SupplierFileViewDTO;
import com.locadora_rdt_backend.modules.organization.suppliers.mapper.SupplierFileMapper;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import com.locadora_rdt_backend.modules.organization.suppliers.model.SupplierFile;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierFileRepository;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierRepository;
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
public class SupplierFileServiceImpl implements SupplierFileService {

    private final SupplierFileRepository repository;
    private final SupplierRepository supplierRepository;
    private final SupplierFileMapper mapper;

    public SupplierFileServiceImpl(
            SupplierFileRepository repository,
            SupplierRepository supplierRepository,
            SupplierFileMapper mapper
    ) {
        this.repository = repository;
        this.supplierRepository = supplierRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public SupplierFileDTO upload(Long supplierId, String name, MultipartFile file) {

        Supplier supplier = findSupplierById(supplierId);

        if (name == null || name.trim().isEmpty()) {
            throw new FileException(SupplierConstants.FILE_NAME_REQUIRED);
        }

        if (file == null || file.isEmpty()) {
            throw new FileException(SupplierConstants.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new FileException(SupplierConstants.INVALID_FILE_NAME);
        }

        if (file.getSize() > SupplierConstants.MAX_FILE_SIZE_BYTES) {
            throw new FileException(SupplierConstants.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !SupplierConstants.ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileException(SupplierConstants.INVALID_FILE_TYPE);
        }

        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD);
        normalizedFileName = normalizedFileName.replaceAll("[^\\p{ASCII}]", "");
        normalizedFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String storedFileName = UUID.randomUUID() + "-" + normalizedFileName;

        SupplierFile supplierFile = new SupplierFile();
        supplierFile.setSupplier(supplier);
        supplierFile.setName(name.trim());
        supplierFile.setOriginalFileName(originalFileName);
        supplierFile.setStoredFileName(storedFileName);
        supplierFile.setContentType(contentType);
        supplierFile.setSize(file.getSize());

        try {

            byte[] data = file.getBytes();

            supplierFile.setData(data);

        } catch (IOException e) {

            throw new FileException(SupplierConstants.FILE_READ_ERROR, e);
        }

        SupplierFile savedFile = repository.save(supplierFile);

        SupplierFileDTO supplierFileDTO = mapper.toDTO(savedFile);

        return supplierFileDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierFileDTO> findAllBySupplier(Long supplierId) {

        findSupplierById(supplierId);

        List<SupplierFile> files = repository.findBySupplierIdOrderByIdDesc(supplierId);

        List<SupplierFileDTO> filesDTO = new ArrayList<>();

        for (SupplierFile file : files) {
            filesDTO.add(mapper.toDTO(file));
        }

        return filesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierFileViewDTO download(Long supplierId, Long fileId) {

        SupplierFile supplierFile = findFileBelongsToSupplier(supplierId, fileId);

        SupplierFileViewDTO supplierFileDTO = new SupplierFileViewDTO(
                supplierFile.getOriginalFileName(),
                supplierFile.getContentType(),
                supplierFile.getData()
        );

        return supplierFileDTO;
    }

    @Override
    @Transactional
    public void delete(Long supplierId, Long fileId) {

        SupplierFile supplierFile = findFileBelongsToSupplier(supplierId, fileId);

        repository.delete(supplierFile);
    }

    private SupplierFile findFileBelongsToSupplier(Long supplierId, Long fileId) {

        findSupplierById(supplierId);

        Optional<SupplierFile> fileOptional = repository.findById(fileId);

        if (!fileOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.FILE_NOT_FOUND);
        }

        SupplierFile supplierFile = fileOptional.get();

        if (!supplierFile.getSupplier().getId().equals(supplierId)) {
            throw new ResourceNotFoundException(SupplierConstants.FILE_DOES_NOT_BELONG_TO_SUPPLIER);
        }

        return supplierFile;
    }

    private Supplier findSupplierById(Long supplierId) {

        Optional<Supplier> supplierOptional = supplierRepository.findById(supplierId);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(SupplierConstants.SUPPLIER_NOT_FOUND);
        }

        Supplier supplier = supplierOptional.get();

        return supplier;
    }
}
