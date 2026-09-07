package com.locadora_rdt_backend.modules.identity.permissions.controller;

import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;
import com.locadora_rdt_backend.modules.identity.permissions.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/permissions")
public class PermissionController {

    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    @PreAuthorize(PERMISSION_READ)
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> findAll(
            @RequestParam(value = "groupName", defaultValue = "") String groupName
    ) {
        List<PermissionDTO> list = service.findAll(groupName);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(PERMISSION_READ)
    @GetMapping(value = "/groups")
    public ResponseEntity<List<String>> findAllGroups() {
        List<String> groups = service.findAllGroupNames();
        return ResponseEntity.ok().body(groups);
    }
}
