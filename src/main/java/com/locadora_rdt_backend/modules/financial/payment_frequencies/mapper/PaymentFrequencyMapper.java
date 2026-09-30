package com.locadora_rdt_backend.modules.financial.payment_frequencies.mapper;

import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyInsertDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import org.springframework.stereotype.Component;

@Component
public class PaymentFrequencyMapper {

    public PaymentFrequencyMapper() {
    }

    public PaymentFrequencyDTO toDTO(PaymentFrequency entity) {

        PaymentFrequencyDTO dto = new PaymentFrequencyDTO();

        dto.setId(entity.getId());
        dto.setFrequency(entity.getFrequency());
        dto.setDays(entity.getDays());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public PaymentFrequency toEntity(PaymentFrequencyInsertDTO dto) {

        PaymentFrequency entity = new PaymentFrequency();

        entity.setFrequency(dto.getFrequency());
        entity.setDays(dto.getDays());

        return entity;
    }

    public void updateEntity(PaymentFrequency entity, PaymentFrequencyUpdateDTO dto) {

        entity.setFrequency(dto.getFrequency());
        entity.setDays(dto.getDays());
    }

}
