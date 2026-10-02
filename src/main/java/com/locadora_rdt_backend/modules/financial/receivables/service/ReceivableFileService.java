package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileViewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReceivableFileService {

    ReceivableFileDTO upload(Long receivableId, String name, MultipartFile file);

    List<ReceivableFileDTO> findAllByReceivable(Long receivableId);

    ReceivableFileViewDTO download(Long receivableId, Long fileId);

    void delete(Long receivableId, Long fileId);
}
