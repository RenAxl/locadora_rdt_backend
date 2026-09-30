package com.locadora_rdt_backend.modules.settings.financial_settings.service;

import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.mapper.FinancialSettingMapper;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import com.locadora_rdt_backend.modules.settings.financial_settings.repository.FinancialSettingRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class FinancialSettingServiceImpl implements FinancialSettingService {

    private final FinancialSettingRepository repository;
    private final FinancialSettingMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public FinancialSettingServiceImpl(
            FinancialSettingRepository repository,
            FinancialSettingMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional
    public FinancialSettingDTO findCurrent() {

        FinancialSetting financialSetting = getOrCreateDefault();

        FinancialSettingDTO financialSettingDTO = mapper.toDTO(financialSetting);

        return financialSettingDTO;
    }

    @Override
    @Transactional
    public FinancialSettingDTO update(FinancialSettingUpdateDTO dto) {

        FinancialSetting financialSetting = getOrCreateDefault();

        mapper.updateEntity(financialSetting, dto);

        financialSetting.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        FinancialSetting savedFinancialSetting = repository.save(financialSetting);

        FinancialSettingDTO financialSettingDTO = mapper.toDTO(savedFinancialSetting);

        return financialSettingDTO;
    }

    @Override
    @Transactional
    public FinancialSetting findCurrentEntity() {

        FinancialSetting financialSetting = getOrCreateDefault();

        return financialSetting;
    }

    private FinancialSetting getOrCreateDefault() {

        Optional<FinancialSetting> financialSettingOptional = repository.findBySingletonKey(
                FinancialSettingConstants.DEFAULT_SINGLETON_KEY
        );

        if (financialSettingOptional.isPresent()) {
            return financialSettingOptional.get();
        }

        FinancialSettingUpdateDTO dto = new FinancialSettingUpdateDTO();
        dto.setDefaultLateFeePercent(BigDecimal.ZERO);
        dto.setDefaultLateInterestPercent(BigDecimal.ZERO);

        FinancialSetting financialSetting = mapper.toEntity(dto);
        financialSetting.setSingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY);

        String username = authenticationFacade.getAuthenticatedUsername();

        if (username == null || username.trim().isEmpty()) {
            username = FinancialSettingConstants.SYSTEM_USER;
        }

        financialSetting.setCreatedBy(username);

        FinancialSetting savedFinancialSetting = repository.save(financialSetting);

        return savedFinancialSetting;
    }
}
