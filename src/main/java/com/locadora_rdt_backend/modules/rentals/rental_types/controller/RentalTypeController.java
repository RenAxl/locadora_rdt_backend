package com.locadora_rdt_backend.modules.rentals.rental_types.controller;

import com.locadora_rdt_backend.modules.rentals.rental_types.dto.*;
import com.locadora_rdt_backend.modules.rentals.rental_types.service.RentalTypeService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/rental/rentaltypes")
public class RentalTypeController {

    private final RentalTypeService service;

    public RentalTypeController(RentalTypeService service) {
        this.service = service;
    }

    @PreAuthorize(RENTAL_TYPES_READ + " or " + RENTAL_TYPES_CUSTOMERS_READ)
    @GetMapping
    public ResponseEntity<Page<RentalTypeDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<RentalTypeDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RENTAL_TYPES_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<RentalTypeDTO> findById(@PathVariable Long id) {
        RentalTypeDTO rentalTypeDto = service.findById(id);
        return ResponseEntity.ok().body(rentalTypeDto);
    }

    @PreAuthorize(RENTAL_TYPES_WRITE)
    @PostMapping
    public ResponseEntity<RentalTypeDTO> insert(@Valid @RequestBody RentalTypeInsertDTO dto) {
        RentalTypeDTO rentalTypeDto = service.insert(dto);
        return ControllerResponseBuilder.created(rentalTypeDto.getId(), rentalTypeDto);
    }

    @PreAuthorize(RENTAL_TYPES_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<RentalTypeDTO> update(@PathVariable Long id, @Valid @RequestBody RentalTypeUpdateDTO dto) {
        RentalTypeDTO rentalTypeDto = service.update(id, dto);
        return ResponseEntity.ok().body(rentalTypeDto);
    }

    @PreAuthorize(RENTAL_TYPES_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(RENTAL_TYPES_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(RENTAL_TYPES_WRITE)
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeActive(@PathVariable Long id, @RequestBody boolean active) {
        service.changeActiveStatus(id, active);

        return ResponseEntity.noContent().build();
    }

}
