package com.locadora_rdt_backend.modules.dashboard.controller;

import com.locadora_rdt_backend.modules.dashboard.dto.DashboardDTO;
import com.locadora_rdt_backend.modules.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.DASHBOARD_READ;

@RestController
@RequestMapping(value = "/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @PreAuthorize(DASHBOARD_READ)
    @GetMapping
    public ResponseEntity<DashboardDTO> getSummary() {
        DashboardDTO dashboardDTO = service.getSummary();
        return ResponseEntity.ok().body(dashboardDTO);
    }
}
