package com.locadora_rdt_backend.modules.settings.financial_settings.service;

import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;

public interface FinancialSettingService {

    FinancialSettingDTO findCurrent();

    FinancialSettingDTO update(FinancialSettingUpdateDTO dto);

    FinancialSetting findCurrentEntity();
}
