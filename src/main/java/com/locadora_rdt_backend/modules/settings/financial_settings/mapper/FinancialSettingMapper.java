package com.locadora_rdt_backend.modules.settings.financial_settings.mapper;

import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import org.springframework.stereotype.Component;

@Component
public class FinancialSettingMapper {

    public FinancialSettingMapper() {
    }

    public FinancialSettingDTO toDTO(FinancialSetting entity) {

        FinancialSettingDTO dto = new FinancialSettingDTO();

        dto.setId(entity.getId());
        dto.setDefaultLateFeePercent(entity.getDefaultLateFeePercent());
        dto.setDefaultLateInterestPercent(entity.getDefaultLateInterestPercent());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setUpdatedBy(entity.getUpdatedBy());

        return dto;
    }

    public FinancialSetting toEntity(FinancialSettingUpdateDTO dto) {

        FinancialSetting entity = new FinancialSetting();

        entity.setDefaultLateFeePercent(dto.getDefaultLateFeePercent());
        entity.setDefaultLateInterestPercent(dto.getDefaultLateInterestPercent());

        return entity;
    }

    public void updateEntity(FinancialSetting entity, FinancialSettingUpdateDTO dto) {

        entity.setDefaultLateFeePercent(dto.getDefaultLateFeePercent());
        entity.setDefaultLateInterestPercent(dto.getDefaultLateInterestPercent());
    }

}
