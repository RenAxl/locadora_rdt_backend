package com.locadora_rdt_backend.modules.identity.roles.controller;

import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleDetailsDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RoleInsertDTO;
import com.locadora_rdt_backend.modules.identity.roles.dto.RolePermissionsUpdateDTO;
import com.locadora_rdt_backend.modules.identity.roles.service.RoleService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/roles")
public class RoleController {

    private final RoleService service;

    public RoleController(RoleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<RoleDTO>> findAllPaged(
            @RequestParam(value = "authority", defaultValue = "") String authority,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "12") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "authority") String orderBy
    ) {
        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<RoleDTO> list = service.findAllPaged(authority.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RoleDetailsDTO> findById(@PathVariable Long id) {
        RoleDetailsDTO dto = service.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping(value = "/{id}/permissions")
    public ResponseEntity<RoleDTO> updatePermissions(
            @PathVariable Long id,
            @Valid @RequestBody RolePermissionsUpdateDTO dto
    ) {
        RoleDTO updated = service.updateRolePermissions(id, dto);
        return ResponseEntity.ok().body(updated);
    }

    @PostMapping
    public ResponseEntity<RoleDTO> insert(@Valid @RequestBody RoleInsertDTO dto) {
        RoleDTO created = service.insert(dto);
        return ControllerResponseBuilder.created(created.getId(), created);
    }
}
