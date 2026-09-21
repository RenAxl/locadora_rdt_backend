package com.locadora_rdt_backend.modules.settings.system_settings.mapper;

import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.model.SystemSetting;
import org.springframework.stereotype.Component;

@Component
public class SystemSettingMapper {

    public SystemSettingMapper() {
    }

    public SystemSettingDTO toDTO(SystemSetting entity) {

        SystemSettingDTO dto = new SystemSettingDTO();

        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setAddress(entity.getAddress());

        return dto;
    }

    public void updateEntity(SystemSetting entity, SystemSettingUpdateDTO dto) {

        entity.setCompanyName(dto.getCompanyName());
        entity.setAddress(dto.getAddress());
    }
}
