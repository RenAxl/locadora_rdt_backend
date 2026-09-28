package com.locadora_rdt_backend.modules.organization.suppliers.controller;

import com.locadora_rdt_backend.modules.organization.suppliers.dto.*;
import com.locadora_rdt_backend.modules.organization.suppliers.service.SupplierService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/suppliers")
public class SupplierController {

    private final SupplierService service;

    public SupplierController(SupplierService service) {
        this.service = service;
    }

    @PreAuthorize(SUPPLIER_READ)
    @GetMapping
    public ResponseEntity<Page<SupplierDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<SupplierDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(SUPPLIER_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<SupplierDTO> findById(@PathVariable Long id) {
        SupplierDTO supplierDto = service.findById(id);
        return ResponseEntity.ok().body(supplierDto);
    }

    @PreAuthorize(SUPPLIER_WRITE)
    @PostMapping
    public ResponseEntity<SupplierDTO> insert(@Valid @RequestBody SupplierInsertDTO dto) {
        SupplierDTO supplierDto = service.insert(dto);
        return ControllerResponseBuilder.created(supplierDto.getId(), supplierDto);
    }

    @PreAuthorize(SUPPLIER_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<SupplierDTO> update(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDTO dto) {
        SupplierDTO supplierDto = service.update(id, dto);
        return ResponseEntity.ok().body(supplierDto);
    }

    @PreAuthorize(SUPPLIER_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(SUPPLIER_READ)
    @GetMapping(value = "/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {

        SupplierImageDTO dto = service.getSupplierImageById(id);

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getContentType()))
                .body(dto.getImage());
    }

    @PreAuthorize(SUPPLIER_WRITE)
    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        service.updateImage(id, file);
        return ResponseEntity.noContent().build();
    }

}
