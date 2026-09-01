package com.locadora_rdt_backend.modules.identity.permissions.controller;

import com.locadora_rdt_backend.modules.identity.permissions.dto.PermissionDTO;
import com.locadora_rdt_backend.modules.identity.permissions.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/permissions")
public class PermissionController {

    private final PermissionService service;

    public PermissionController(PermissionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PermissionDTO>> findAll(
            @RequestParam(value = "groupName", defaultValue = "") String groupName
    ) {
        List<PermissionDTO> list = service.findAll(groupName);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/groups")
    public ResponseEntity<List<String>> findAllGroups() {
        List<String> groups = service.findAllGroupNames();
        return ResponseEntity.ok().body(groups);
    }
}
