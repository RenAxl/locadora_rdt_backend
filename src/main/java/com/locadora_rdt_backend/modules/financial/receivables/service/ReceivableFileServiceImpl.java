package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileViewDTO;
import com.locadora_rdt_backend.modules.financial.receivables.mapper.ReceivableFileMapper;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.locadora_rdt_backend.modules.financial.receivables.model.ReceivableFile;
import com.locadora_rdt_backend.modules.financial.receivables.repository.ReceivableFileRepository;
import com.locadora_rdt_backend.modules.financial.receivables.repository.ReceivableRepository;
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
public class ReceivableFileServiceImpl implements ReceivableFileService {

    private final ReceivableFileRepository repository;
    private final ReceivableRepository receivableRepository;
    private final ReceivableFileMapper mapper;

    public ReceivableFileServiceImpl(
            ReceivableFileRepository repository,
            ReceivableRepository receivableRepository,
            ReceivableFileMapper mapper
    ) {
        this.repository = repository;
        this.receivableRepository = receivableRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ReceivableFileDTO upload(Long receivableId, String name, MultipartFile file) {

        Receivable receivable = findReceivableById(receivableId);

        if (name == null || name.trim().isEmpty()) {
            throw new FileException(ReceivableConstants.FILE_NAME_REQUIRED);
        }

        if (file == null || file.isEmpty()) {
            throw new FileException(ReceivableConstants.EMPTY_FILE);
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new FileException(ReceivableConstants.INVALID_FILE_NAME);
        }

        if (file.getSize() > ReceivableConstants.MAX_FILE_SIZE_BYTES) {
            throw new FileException(ReceivableConstants.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !ReceivableConstants.ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new FileException(ReceivableConstants.INVALID_FILE_TYPE);
        }

        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD);
        normalizedFileName = normalizedFileName.replaceAll("[^\\p{ASCII}]", "");
        normalizedFileName = normalizedFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
        String storedFileName = UUID.randomUUID() + "-" + normalizedFileName;

        ReceivableFile receivableFile = new ReceivableFile();
        receivableFile.setReceivable(receivable);
        receivableFile.setName(name.trim());
        receivableFile.setOriginalFileName(originalFileName);
        receivableFile.setStoredFileName(storedFileName);
        receivableFile.setContentType(contentType);
        receivableFile.setSize(file.getSize());

        try {

            byte[] data = file.getBytes();

            receivableFile.setData(data);

        } catch (IOException e) {

            throw new FileException(ReceivableConstants.FILE_READ_ERROR, e);
        }

        receivable.setFileName(originalFileName);

        ReceivableFile savedFile = repository.save(receivableFile);

        ReceivableFileDTO receivableFileDTO = mapper.toDTO(savedFile);

        return receivableFileDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceivableFileDTO> findAllByReceivable(Long receivableId) {

        findReceivableById(receivableId);

        List<ReceivableFile> files = repository.findByReceivableIdOrderByIdDesc(receivableId);

        List<ReceivableFileDTO> filesDTO = new ArrayList<>();

        for (ReceivableFile file : files) {
            filesDTO.add(mapper.toDTO(file));
        }

        return filesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivableFileViewDTO download(Long receivableId, Long fileId) {

        ReceivableFile receivableFile = findFileBelongsToReceivable(receivableId, fileId);

        ReceivableFileViewDTO receivableFileDTO = new ReceivableFileViewDTO(
                receivableFile.getOriginalFileName(),
                receivableFile.getContentType(),
                receivableFile.getData()
        );

        return receivableFileDTO;
    }

    @Override
    @Transactional
    public void delete(Long receivableId, Long fileId) {

        ReceivableFile receivableFile = findFileBelongsToReceivable(receivableId, fileId);

        repository.delete(receivableFile);
    }

    private ReceivableFile findFileBelongsToReceivable(Long receivableId, Long fileId) {

        findReceivableById(receivableId);

        Optional<ReceivableFile> fileOptional = repository.findById(fileId);

        if (!fileOptional.isPresent()) {
            throw new ResourceNotFoundException(ReceivableConstants.FILE_NOT_FOUND + fileId);
        }

        ReceivableFile receivableFile = fileOptional.get();

        if (!receivableFile.getReceivable().getId().equals(receivableId)) {
            throw new ResourceNotFoundException(ReceivableConstants.FILE_DOES_NOT_BELONG_TO_RECEIVABLE);
        }

        return receivableFile;
    }

    private Receivable findReceivableById(Long receivableId) {

        Optional<Receivable> receivableOptional = receivableRepository.findById(receivableId);

        if (!receivableOptional.isPresent()) {
            throw new ResourceNotFoundException(ReceivableConstants.RECEIVABLE_NOT_FOUND + ReceivableConstants.ID_COMPLEMENT + receivableId);
        }

        Receivable receivable = receivableOptional.get();

        return receivable;
    }
}
