package com.locadora_rdt_backend.modules.rentals.rental_history.controller;

import com.locadora_rdt_backend.modules.rentals.rental_history.dto.RentalHistoryDTO;
import com.locadora_rdt_backend.modules.rentals.rental_history.service.RentalHistoryService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.RENTAL_HISTORY_READ;

@RestController
@RequestMapping(value = "/rental-history")
public class RentalHistoryController {

    private final RentalHistoryService service;

    public RentalHistoryController(RentalHistoryService service) {
        this.service = service;
    }

    @PreAuthorize(RENTAL_HISTORY_READ)
    @GetMapping
    public ResponseEntity<Page<RentalHistoryDTO>> findAllPaged(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "10") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "rentalDate") String orderBy) {

        String sortField = orderBy;
        if ("rentalDate".equals(orderBy) || "registrationDate".equals(orderBy)) {
            sortField = "rental_date";
        }

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, sortField);

        Page<RentalHistoryDTO> list = service.findAllPaged(pageRequest);

        return ResponseEntity.ok().body(list);
    }
}
