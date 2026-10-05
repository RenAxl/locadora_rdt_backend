package com.locadora_rdt_backend.modules.stocks.stock_balances.controller;

import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.service.StockBalanceService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/inventory/stock-balances")
public class StockBalanceController {

    private final StockBalanceService service;

    public StockBalanceController(StockBalanceService service) {
        this.service = service;
    }

    @PreAuthorize(STOCK_BALANCES_READ)
    @GetMapping
    public ResponseEntity<Page<StockBalanceDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "item.name") String orderBy) {

        if ("name".equals(orderBy)) {
            orderBy = "item.name";
        }

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<StockBalanceDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(STOCK_BALANCES_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<StockBalanceDTO> findById(@PathVariable Long id) {
        StockBalanceDTO stockBalanceDto = service.findById(id);
        return ResponseEntity.ok().body(stockBalanceDto);
    }

    @PreAuthorize(STOCK_BALANCES_READ)
    @GetMapping(value = "/item/{itemId}")
    public ResponseEntity<StockBalanceDTO> findByItemId(@PathVariable Long itemId) {
        StockBalanceDTO stockBalanceDto = service.findByItemId(itemId);
        return ResponseEntity.ok().body(stockBalanceDto);
    }

    @PreAuthorize(STOCK_BALANCES_WRITE)
    @PatchMapping(value = "/{id}/minimum")
    public ResponseEntity<StockBalanceDTO> updateMinimum(
            @PathVariable Long id,
            @Valid @RequestBody StockBalanceMinimumUpdateDTO dto
    ) {

        StockBalanceDTO stockBalanceDto = service.updateMinimum(id, dto);
        return ResponseEntity.ok().body(stockBalanceDto);
    }

}
