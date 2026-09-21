package com.locadora_rdt_backend.modules.settings.system_settings.controller;

import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.dto.SystemSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.system_settings.service.SystemSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/system-settings")
public class SystemSettingController {

    private final SystemSettingService service;

    public SystemSettingController(SystemSettingService service) {
        this.service = service;
    }

    @PreAuthorize(SYSTEM_SETTING_READ)
    @GetMapping
    public ResponseEntity<SystemSettingDTO> findCurrent() {
        SystemSettingDTO systemSettingDTO = service.findCurrent();
        return ResponseEntity.ok().body(systemSettingDTO);
    }

    @PreAuthorize(SYSTEM_SETTING_WRITE)
    @PutMapping
    public ResponseEntity<SystemSettingDTO> update(@Valid @RequestBody SystemSettingUpdateDTO dto) {
        SystemSettingDTO systemSettingDTO = service.update(dto);
        return ResponseEntity.ok().body(systemSettingDTO);
    }
}
