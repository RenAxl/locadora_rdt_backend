package com.locadora_rdt_backend.modules.organization.employees.controller;

import com.locadora_rdt_backend.modules.organization.employees.dto.*;
import com.locadora_rdt_backend.modules.organization.employees.service.EmployeeService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @PreAuthorize(EMPLOYEE_READ)
    @GetMapping
    public ResponseEntity<Page<EmployeeDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "3") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<EmployeeDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(EMPLOYEE_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<EmployeeDTO> findById(@PathVariable Long id) {
        EmployeeDTO employeeDto = service.findById(id);
        return ResponseEntity.ok().body(employeeDto);
    }

    @PreAuthorize(EMPLOYEE_WRITE)
    @PostMapping
    public ResponseEntity<EmployeeDTO> insert(@Valid @RequestBody EmployeeInsertDTO dto) {
        EmployeeDTO employeeDto = service.insert(dto);
        return ControllerResponseBuilder.created(employeeDto.getId(), employeeDto);
    }

    @PreAuthorize(EMPLOYEE_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<EmployeeDTO> update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto) {
        EmployeeDTO employeeDto = service.update(id, dto);
        return ResponseEntity.ok().body(employeeDto);
    }

    @PreAuthorize(EMPLOYEE_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(EMPLOYEE_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(EMPLOYEE_WRITE)
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeActive(@PathVariable Long id, @RequestBody boolean active) {
        service.changeActiveStatus(id, active);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(EMPLOYEE_READ)
    @GetMapping(value = "/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {

        EmployeePhotoDTO dto = service.getEmployeePhotoById(id);

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getContentType()))
                .body(dto.getPhoto());
    }

    @PreAuthorize(EMPLOYEE_WRITE)
    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        service.updatePhoto(id, file);
        return ResponseEntity.noContent().build();
    }

}
