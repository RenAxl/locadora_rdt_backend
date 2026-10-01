package com.locadora_rdt_backend.modules.financial.payables.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileViewDTO;
import com.locadora_rdt_backend.modules.financial.payables.mapper.PayableFileMapper;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.payables.model.PayableFile;
import com.locadora_rdt_backend.modules.financial.payables.repository.PayableFileRepository;
import com.locadora_rdt_backend.modules.financial.payables.repository.PayableRepository;
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
public class PayableFileServiceImpl implements PayableFileService {

    private final PayableFileRepository repository;
    private final PayableRepository payableRepository;
    private final PayableFileMapper mapper;

    public PayableFileServiceImpl(
            PayableFileRepository repository,
            PayableRepository payableRepository,
            PayableFileMapper mapper
    ) {
        this.repository = repository;
        this.payableRepository = payableRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public PayableFileDTO upload(Long payableId, String name, MultipartFile file) {

        Payable payable = findPayableById(payableId);

        if (name == null || name.trim().isEmpty()) {
            throw new FileException(PayableConstants.FILE_NAME_REQUIRED);
        }

        if (file == null || file.isEmpty()) {
            throw new FileException(PayableConstants.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new FileException(PayableConstants.INVALID_FILE_NAME);
        }

        if (file.getSize() > PayableConstants.MAX_FILE_SIZE_BYTES) {
            throw new FileException(PayableConstants.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !PayableConstants.ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileException(PayableConstants.INVALID_FILE_TYPE);
        }

        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD);
        normalizedFileName = normalizedFileName.replaceAll("[^\\p{ASCII}]", "");
        normalizedFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String storedFileName = UUID.randomUUID() + "-" + normalizedFileName;

        PayableFile payableFile = new PayableFile();
        payableFile.setPayable(payable);
        payableFile.setName(name.trim());
        payableFile.setOriginalFileName(originalFileName);
        payableFile.setStoredFileName(storedFileName);
        payableFile.setContentType(contentType);
        payableFile.setSize(file.getSize());

        try {

            byte[] data = file.getBytes();

            payableFile.setData(data);

        } catch (IOException e) {

            throw new FileException(PayableConstants.FILE_READ_ERROR, e);
        }

        payable.setFileName(originalFileName);

        PayableFile savedFile = repository.save(payableFile);

        PayableFileDTO payableFileDTO = mapper.toDTO(savedFile);

        return payableFileDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayableFileDTO> findAllByPayable(Long payableId) {

        findPayableById(payableId);

        List<PayableFile> files = repository.findByPayableIdOrderByIdDesc(payableId);

        List<PayableFileDTO> filesDTO = new ArrayList<>();

        for (PayableFile file : files) {
            filesDTO.add(mapper.toDTO(file));
        }

        return filesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PayableFileViewDTO download(Long payableId, Long fileId) {

        PayableFile payableFile = findFileBelongsToPayable(payableId, fileId);

        PayableFileViewDTO payableFileDTO = new PayableFileViewDTO(
                payableFile.getOriginalFileName(),
                payableFile.getContentType(),
                payableFile.getData()
        );

        return payableFileDTO;
    }

    @Override
    @Transactional
    public void delete(Long payableId, Long fileId) {

        PayableFile payableFile = findFileBelongsToPayable(payableId, fileId);

        repository.delete(payableFile);
    }

    private PayableFile findFileBelongsToPayable(Long payableId, Long fileId) {

        findPayableById(payableId);

        Optional<PayableFile> fileOptional = repository.findById(fileId);

        if (!fileOptional.isPresent()) {
            throw new ResourceNotFoundException(PayableConstants.FILE_NOT_FOUND + fileId);
        }

        PayableFile payableFile = fileOptional.get();

        if (!payableFile.getPayable().getId().equals(payableId)) {
            throw new ResourceNotFoundException(PayableConstants.FILE_DOES_NOT_BELONG_TO_PAYABLE);
        }

        return payableFile;
    }

    private Payable findPayableById(Long payableId) {

        Optional<Payable> payableOptional = payableRepository.findById(payableId);

        if (!payableOptional.isPresent()) {
            throw new ResourceNotFoundException(PayableConstants.PAYABLE_NOT_FOUND + PayableConstants.ID_COMPLEMENT + payableId);
        }

        Payable payable = payableOptional.get();

        return payable;
    }
}
