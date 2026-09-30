package com.locadora_rdt_backend.modules.settings.financial_settings.controller;

import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.dto.FinancialSettingUpdateDTO;
import com.locadora_rdt_backend.modules.settings.financial_settings.service.FinancialSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/financial-settings")
public class FinancialSettingController {

    private final FinancialSettingService service;

    public FinancialSettingController(FinancialSettingService service) {
        this.service = service;
    }

    @PreAuthorize(FINANCIAL_SETTINGS_READ)
    @GetMapping
    public ResponseEntity<FinancialSettingDTO> findCurrent() {
        FinancialSettingDTO financialSettingDto = service.findCurrent();
        return ResponseEntity.ok().body(financialSettingDto);
    }

    @PreAuthorize(FINANCIAL_SETTINGS_WRITE)
    @PutMapping
    public ResponseEntity<FinancialSettingDTO> update(@Valid @RequestBody FinancialSettingUpdateDTO dto) {
        FinancialSettingDTO financialSettingDto = service.update(dto);
        return ResponseEntity.ok().body(financialSettingDto);
    }
}
