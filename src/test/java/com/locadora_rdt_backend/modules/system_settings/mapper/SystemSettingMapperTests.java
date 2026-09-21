package com.locadora_rdt_backend.modules.system_settings.mapper;

import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.mapper.SystemSettingMapper;
import com.locadora_rdt_backend.modules.settings.system_settings.model.SystemSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemSettingMapperTests {

    private SystemSettingMapper mapper;
    private SystemSetting systemSetting;

    @BeforeEach
    void setUp() {
        mapper = new SystemSettingMapper();
        systemSetting = new SystemSetting();
        systemSetting.setCompanyName("Locadora RDT");
    }

    @Test
    void toDTOShouldReturnSavedIcon() {
        systemSetting.setIcon("fa-store");

        SystemSettingDTO resultado = mapper.toDTO(systemSetting);

        assertEquals("fa-store", resultado.getIcon());
    }

    @Test
    void toDTOShouldReturnDefaultIconWhenSavedIconIsNull() {
        systemSetting.setIcon(null);

        SystemSettingDTO resultado = mapper.toDTO(systemSetting);

        assertEquals("fa-gamepad", resultado.getIcon());
    }

    @Test
    void updateEntityShouldUpdateSelectedIcon() {
        SystemSettingUpdateDTO dto = new SystemSettingUpdateDTO();
        dto.setIcon("fa-car");

        mapper.updateEntity(systemSetting, dto);

        assertEquals("fa-car", systemSetting.getIcon());
    }

    @Test
    void updateEntityShouldKeepIconWhenRequestDoesNotIncludeIcon() {
        systemSetting.setIcon("fa-film");
        SystemSettingUpdateDTO dto = new SystemSettingUpdateDTO();

        mapper.updateEntity(systemSetting, dto);

        assertEquals("fa-film", systemSetting.getIcon());
    }

    @Test
    void iconShouldAcceptAvailableOption() {
        SystemSettingUpdateDTO dto = new SystemSettingUpdateDTO();
        dto.setIcon("fa-screwdriver-wrench");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validateProperty(dto, "icon").isEmpty());
        }
    }

    @Test
    void iconShouldAcceptNewOption() {
        SystemSettingUpdateDTO dto = new SystemSettingUpdateDTO();
        dto.setIcon("fa-graduation-cap");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validateProperty(dto, "icon").isEmpty());
        }
    }

    @Test
    void iconShouldRejectUnknownOption() {
        SystemSettingUpdateDTO dto = new SystemSettingUpdateDTO();
        dto.setIcon("icone-invalido");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validateProperty(dto, "icon").isEmpty());
        }
    }
}
