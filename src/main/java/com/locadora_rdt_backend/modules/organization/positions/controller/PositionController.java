package com.locadora_rdt_backend.modules.organization.positions.controller;

import com.locadora_rdt_backend.modules.organization.positions.dto.*;
import com.locadora_rdt_backend.modules.organization.positions.service.PositionService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/positions")
public class PositionController {

    private final PositionService service;

    public PositionController(PositionService service) {
        this.service = service;
    }

    @PreAuthorize(POSITION_READ)
    @GetMapping
    public ResponseEntity<Page<PositionDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<PositionDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(POSITION_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<PositionDTO> findById(@PathVariable Long id) {
        PositionDTO positionDto = service.findById(id);
        return ResponseEntity.ok().body(positionDto);
    }

    @PreAuthorize(POSITION_WRITE)
    @PostMapping
    public ResponseEntity<PositionDTO> insert(@Valid @RequestBody PositionInsertDTO dto) {
        PositionDTO positionDto = service.insert(dto);
        return ControllerResponseBuilder.created(positionDto.getId(), positionDto);
    }

    @PreAuthorize(POSITION_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<PositionDTO> update(@PathVariable Long id, @Valid @RequestBody PositionUpdateDTO dto) {
        PositionDTO positionDto = service.update(id, dto);
        return ResponseEntity.ok().body(positionDto);
    }

    @PreAuthorize(POSITION_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
