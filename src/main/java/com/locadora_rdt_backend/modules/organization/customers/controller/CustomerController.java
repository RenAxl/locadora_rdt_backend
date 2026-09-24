package com.locadora_rdt_backend.modules.organization.customers.controller;

import com.locadora_rdt_backend.modules.organization.customers.dto.*;
import com.locadora_rdt_backend.modules.organization.customers.service.CustomerService;
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
@RequestMapping(value = "/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PreAuthorize(CUSTOMER_READ)
    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "3") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<CustomerDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(CUSTOMER_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<CustomerDTO> findById(@PathVariable Long id) {
        CustomerDTO customerDto = service.findById(id);
        return ResponseEntity.ok().body(customerDto);
    }

    @PreAuthorize(CUSTOMER_WRITE)
    @PostMapping
    public ResponseEntity<CustomerDTO> insert(@Valid @RequestBody CustomerInsertDTO dto) {
        CustomerDTO customerDto = service.insert(dto);
        return ControllerResponseBuilder.created(customerDto.getId(), customerDto);
    }

    @PreAuthorize(CUSTOMER_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto) {
        CustomerDTO customerDto = service.update(id, dto);
        return ResponseEntity.ok().body(customerDto);
    }

    @PreAuthorize(CUSTOMER_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(CUSTOMER_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(CUSTOMER_WRITE)
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeActive(@PathVariable Long id, @RequestBody boolean active) {
        service.changeActiveStatus(id, active);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(CUSTOMER_READ)
    @GetMapping(value = "/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {

        CustomerPhotoDTO dto = service.getCustomerPhotoById(id);

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getContentType()))
                .body(dto.getPhoto());
    }

    @PreAuthorize(CUSTOMER_WRITE)
    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        service.updatePhoto(id, file);
        return ResponseEntity.noContent().build();
    }

}
