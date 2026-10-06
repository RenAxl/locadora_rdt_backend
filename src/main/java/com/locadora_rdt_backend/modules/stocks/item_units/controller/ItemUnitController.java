package com.locadora_rdt_backend.modules.stocks.item_units.controller;

import com.locadora_rdt_backend.modules.stocks.item_units.dto.*;
import com.locadora_rdt_backend.modules.stocks.item_units.service.ItemUnitService;
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
@RequestMapping(value = "/inventory/item-units")
public class ItemUnitController {

    private final ItemUnitService service;

    public ItemUnitController(ItemUnitService service) {
        this.service = service;
    }

    @PreAuthorize(ITEM_UNIT_READ)
    @GetMapping
    public ResponseEntity<Page<ItemUnitDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "itemId", defaultValue = "-1") Long itemId,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "assetCode") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<ItemUnitDTO> list = service.findAllPaged(name.trim(), itemId, active, pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(ITEM_UNIT_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<ItemUnitDTO> findById(@PathVariable Long id) {
        ItemUnitDTO itemDto = service.findById(id);
        return ResponseEntity.ok().body(itemDto);
    }

    @PreAuthorize(ITEM_UNIT_WRITE)
    @PostMapping
    public ResponseEntity<ItemUnitDTO> insert(@Valid @RequestBody ItemUnitInsertDTO dto) {
        ItemUnitDTO itemDto = service.insert(dto);
        return ControllerResponseBuilder.created(itemDto.getId(), itemDto);
    }

    @PreAuthorize(ITEM_UNIT_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<ItemUnitDTO> update(@PathVariable Long id, @Valid @RequestBody ItemUnitUpdateDTO dto) {
        ItemUnitDTO itemDto = service.update(id, dto);
        return ResponseEntity.ok().body(itemDto);
    }

    @PreAuthorize(ITEM_UNIT_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(ITEM_UNIT_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(ITEM_UNIT_WRITE)
    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> changeActive(@PathVariable Long id, @RequestBody boolean active) {
        service.changeActiveStatus(id, active);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(ITEM_UNIT_WRITE)
    @PatchMapping("/{id}/status")
    public ResponseEntity<ItemUnitDTO> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody ItemUnitStatusUpdateDTO dto) {
        return ResponseEntity.ok().body(service.updateStatus(id, dto));
    }

    @PreAuthorize(ITEM_UNIT_WRITE)
    @PatchMapping("/{id}/maintenance")
    public ResponseEntity<ItemUnitDTO> changeMaintenance(@PathVariable Long id, @RequestBody boolean maintenance) {
        ItemUnitDTO dto = service.changeMaintenanceStatus(id, maintenance);
        return ResponseEntity.ok().body(dto);
    }
}
