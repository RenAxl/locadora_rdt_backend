package com.locadora_rdt_backend.modules.organization.departments.controller;

import com.locadora_rdt_backend.modules.organization.departments.dto.*;
import com.locadora_rdt_backend.modules.organization.departments.service.DepartmentService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @PreAuthorize(DEPARTMENT_READ)
    @GetMapping
    public ResponseEntity<Page<DepartmentDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "3") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<DepartmentDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(DEPARTMENT_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<DepartmentDTO> findById(@PathVariable Long id) {
        DepartmentDTO departmentDto = service.findById(id);
        return ResponseEntity.ok().body(departmentDto);
    }

    @PreAuthorize(DEPARTMENT_WRITE)
    @PostMapping
    public ResponseEntity<DepartmentDTO> insert(@Valid @RequestBody DepartmentInsertDTO dto) {
        DepartmentDTO departmentDto = service.insert(dto);
        return ControllerResponseBuilder.created(departmentDto.getId(), departmentDto);
    }

    @PreAuthorize(DEPARTMENT_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<DepartmentDTO> update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateDTO dto) {
        DepartmentDTO departmentDto = service.update(id, dto);
        return ResponseEntity.ok().body(departmentDto);
    }

    @PreAuthorize(DEPARTMENT_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
