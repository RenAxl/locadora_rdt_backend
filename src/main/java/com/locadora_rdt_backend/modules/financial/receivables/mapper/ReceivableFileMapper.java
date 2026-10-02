package com.locadora_rdt_backend.modules.financial.receivables.mapper;

import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFileDTO;
import com.locadora_rdt_backend.modules.financial.receivables.model.ReceivableFile;
import org.springframework.stereotype.Component;

@Component
public class ReceivableFileMapper {

    public ReceivableFileMapper() {
    }

    public ReceivableFileDTO toDTO(ReceivableFile entity) {

        ReceivableFileDTO dto = new ReceivableFileDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setOriginalFileName(entity.getOriginalFileName());
        dto.setStoredFileName(entity.getStoredFileName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setReceivableId(entity.getReceivable().getId());

        return dto;
    }
}
