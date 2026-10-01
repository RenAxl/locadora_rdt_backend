package com.locadora_rdt_backend.modules.financial.payables.mapper;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFileDTO;
import com.locadora_rdt_backend.modules.financial.payables.model.PayableFile;
import org.springframework.stereotype.Component;

@Component
public class PayableFileMapper {

    public PayableFileMapper() {
    }

    public PayableFileDTO toDTO(PayableFile entity) {

        PayableFileDTO dto = new PayableFileDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setOriginalFileName(entity.getOriginalFileName());
        dto.setStoredFileName(entity.getStoredFileName());
        dto.setContentType(entity.getContentType());
        dto.setSize(entity.getSize());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setPayableId(entity.getPayable().getId());

        return dto;
    }
}
