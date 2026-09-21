package com.locadora_rdt_backend.modules.settings.system_settings.service;

import com.locadora_rdt_backend.modules.settings.system_settings.constants.SystemSettingConstants;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.mapper.SystemSettingMapper;
import com.locadora_rdt_backend.modules.settings.system_settings.model.SystemSetting;
import com.locadora_rdt_backend.modules.settings.system_settings.repository.SystemSettingRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository repository;
    private final SystemSettingMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public SystemSettingServiceImpl(
            SystemSettingRepository repository,
            SystemSettingMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional
    public SystemSettingDTO findCurrent() {

        SystemSetting systemSetting = getOrCreateDefault();

        SystemSettingDTO systemSettingDTO = mapper.toDTO(systemSetting);

        return systemSettingDTO;
    }

    @Override
    @Transactional
    public SystemSettingDTO update(SystemSettingUpdateDTO dto) {

        SystemSetting systemSetting = getOrCreateDefault();

        mapper.updateEntity(systemSetting, dto);

        systemSetting.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        SystemSetting savedSystemSetting = repository.save(systemSetting);

        SystemSettingDTO systemSettingDTO = mapper.toDTO(savedSystemSetting);

        return systemSettingDTO;
    }

    @Override
    @Transactional
    public SystemSetting findCurrentEntity() {

        SystemSetting systemSetting = getOrCreateDefault();

        return systemSetting;
    }

    private SystemSetting getOrCreateDefault() {

        Optional<SystemSetting> systemSettingOptional = repository.findBySingletonKey(
                SystemSettingConstants.DEFAULT_SINGLETON_KEY
        );

        if (systemSettingOptional.isPresent()) {
            return systemSettingOptional.get();
        }

        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setSingletonKey(SystemSettingConstants.DEFAULT_SINGLETON_KEY);
        systemSetting.setCompanyName(SystemSettingConstants.DEFAULT_COMPANY_NAME);

        String username = authenticationFacade.getAuthenticatedUsername();

        if (username == null || username.trim().isEmpty()) {
            username = SystemSettingConstants.SYSTEM_USER;
        }

        systemSetting.setCreatedBy(username);

        SystemSetting savedSystemSetting = repository.save(systemSetting);

        return savedSystemSetting;
    }
}
