package com.locadora_rdt_backend.modules.rentals.catalog.controller;

import com.locadora_rdt_backend.modules.rentals.catalog.service.CatalogService;
import com.locadora_rdt_backend.modules.stocks.items.dto.*;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.CATALOG_READ;

@RestController
@RequestMapping(value = "/catalog")
public class CatalogController {

    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @PreAuthorize(CATALOG_READ)
    @GetMapping
    public ResponseEntity<Page<ItemDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "8") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<ItemDTO> list = service.findAllPaged(name.trim(), categoryId, pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(CATALOG_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<ItemDTO> findById(@PathVariable Long id) {
        ItemDTO itemDto = service.findById(id);
        return ResponseEntity.ok().body(itemDto);
    }

    @PreAuthorize(CATALOG_READ)
    @GetMapping(value = "/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {

        ItemImageDTO dto = service.getItemImageById(id);

        if (dto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.getContentType()))
                .body(dto.getImage());
    }

}
