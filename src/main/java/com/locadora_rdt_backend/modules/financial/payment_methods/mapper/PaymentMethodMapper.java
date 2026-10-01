package com.locadora_rdt_backend.modules.financial.payment_methods.mapper;

import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodInsertDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentMethodMapper {

    public PaymentMethodMapper() {
    }

    public PaymentMethodDTO toDTO(PaymentMethod entity) {

        PaymentMethodDTO dto = new PaymentMethodDTO();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setFee(entity.getFee());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public PaymentMethod toEntity(PaymentMethodInsertDTO dto) {

        PaymentMethod entity = new PaymentMethod();

        entity.setName(dto.getName());
        entity.setFee(dto.getFee());

        return entity;
    }

    public void updateEntity(PaymentMethod entity, PaymentMethodUpdateDTO dto) {

        entity.setName(dto.getName());
        entity.setFee(dto.getFee());
    }
}
