package com.locadora_rdt_backend.modules.settings.system_settings.service;

import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.model.SystemSetting;

public interface SystemSettingService {

    SystemSettingDTO findCurrent();

    SystemSettingDTO update(SystemSettingUpdateDTO dto);

    SystemSetting findCurrentEntity();
}
