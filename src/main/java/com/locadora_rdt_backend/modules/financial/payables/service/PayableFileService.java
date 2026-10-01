package com.locadora_rdt_backend.modules.financial.payables.service;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileViewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PayableFileService {

    PayableFileDTO upload(Long payableId, String name, MultipartFile file);

    List<PayableFileDTO> findAllByPayable(Long payableId);

    PayableFileViewDTO download(Long payableId, Long fileId);

    void delete(Long payableId, Long fileId);
}
