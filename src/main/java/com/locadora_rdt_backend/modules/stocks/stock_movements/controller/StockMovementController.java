package com.locadora_rdt_backend.modules.stocks.stock_movements.controller;

import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.*;
import com.locadora_rdt_backend.modules.stocks.stock_movements.service.StockMovementService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/inventory/stock-movements")
public class StockMovementController {

    private final StockMovementService service;

    public StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @PreAuthorize(STOCK_MOVEMENTS_READ)
    @GetMapping
    public ResponseEntity<Page<StockMovementDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "createdAt") String orderBy) {

        if ("name".equals(orderBy)) {
            orderBy = "item.name";
        }

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<StockMovementDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(STOCK_MOVEMENTS_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<StockMovementDTO> findById(@PathVariable Long id) {
        StockMovementDTO stockMovementDto = service.findById(id);
        return ResponseEntity.ok().body(stockMovementDto);
    }

    @PreAuthorize(STOCK_MOVEMENTS_WRITE)
    @PostMapping
    public ResponseEntity<StockMovementDTO> insert(@Valid @RequestBody StockMovementInsertDTO dto) {
        StockMovementDTO stockMovementDto = service.insert(dto);
        return ControllerResponseBuilder.created(stockMovementDto.getId(), stockMovementDto);
    }
}
