package com.locadora_rdt_backend.modules.rentals.rental.controller;

import com.locadora_rdt_backend.modules.organization.customers.dto.CustomerDTO;
import com.locadora_rdt_backend.modules.rentals.rental.dto.*;
import com.locadora_rdt_backend.modules.rentals.rental.service.RentalService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.List;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/rentals")
public class RentalController {

    private final RentalService service;

    public RentalController(RentalService service) {
        this.service = service;
    }

    @PreAuthorize(RENTAL_READ)
    @GetMapping
    public ResponseEntity<Page<RentalDTO>> findAllPaged(
            @RequestParam(defaultValue = "") String number,
            @RequestParam(defaultValue = "") String customer,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(required = false) Long rentalTypeId,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer linesPerPage,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "rental_date") String orderBy) {
        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<RentalDTO> list = service.findAllPaged(number, customer, status, rentalTypeId,
                dateFrom, dateTo, pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RENTAL_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<RentalDTO> findById(@PathVariable Long id) {
        RentalDTO rentalDTO = service.findById(id);
        return ResponseEntity.ok().body(rentalDTO);
    }

    @PreAuthorize(RENTAL_READ + " or " + RENTAL_CUSTOMER)
    @GetMapping(value = "/current-customer")
    public ResponseEntity<CustomerDTO> findCurrentCustomer() {
        CustomerDTO customerDTO = service.findCurrentCustomer();
        return ResponseEntity.ok().body(customerDTO);
    }

    @PreAuthorize(RENTAL_WRITE + " or " + RENTAL_CUSTOMER)
    @PostMapping
    public ResponseEntity<RentalDTO> insert(@Valid @RequestBody RentalInsertDTO dto) {
        RentalDTO rentalDTO = service.insert(dto);
        return ControllerResponseBuilder.created(rentalDTO.getId(), rentalDTO);
    }

    @PreAuthorize(RENTAL_WRITE)
    @PatchMapping(value = "/{id}/confirm")
    public ResponseEntity<RentalDTO> confirm(@PathVariable Long id) {
        RentalDTO rentalDTO = service.confirm(id);
        return ResponseEntity.ok().body(rentalDTO);
    }

    @PreAuthorize(RENTAL_WRITE)
    @PatchMapping(value = "/{id}/start")
    public ResponseEntity<RentalDTO> start(@PathVariable Long id, @Valid @RequestBody RentalCheckoutDTO dto) {
        RentalDTO rentalDTO = service.start(id, dto);
        return ResponseEntity.ok().body(rentalDTO);
    }

    @PreAuthorize(RENTAL_READ + " or " + RENTAL_CUSTOMER)
    @GetMapping(value = "/availability/items/{itemId}")
    public ResponseEntity<ItemAvailabilityDTO> findAvailability(@PathVariable Long itemId) {
        ItemAvailabilityDTO dto = service.findAvailability(itemId);
        return ResponseEntity.ok().body(dto);
    }

    @PreAuthorize(RENTAL_READ + " or " + RENTAL_CUSTOMER)
    @GetMapping(value = "/availability/items/{itemId}/units")
    public ResponseEntity<List<ItemUnitDTO>> findAvailableUnits(@PathVariable Long itemId) {
        List<ItemUnitDTO> list = service.findAvailableUnits(itemId);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RENTAL_READ + " or " + RENTAL_CUSTOMER)
    @GetMapping(value = "/availability/items/{itemId}/all-units")
    public ResponseEntity<List<ItemUnitDTO>> findItemUnits(@PathVariable Long itemId) {
        List<ItemUnitDTO> list = service.findItemUnits(itemId);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RENTAL_READ + " or " + RENTAL_CUSTOMER)
    @GetMapping(value = "/{id}/units")
    public ResponseEntity<List<RentalItemUnitDTO>> findRentalUnits(@PathVariable Long id) {
        List<RentalItemUnitDTO> list = service.findRentalUnits(id);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RENTAL_READ)
    @GetMapping(value = "/{id}/history")
    public ResponseEntity<List<RentalStatusHistoryDTO>> findHistory(@PathVariable Long id) {
        List<RentalStatusHistoryDTO> list = service.findHistory(id);
        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RENTAL_READ)
    @GetMapping(value = "/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> receipt(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recibo-locacao-" + id + ".pdf")
                .body(service.receipt(id));
    }

    @PreAuthorize(RENTAL_READ)
    @GetMapping(value = "/{id}/fiscal-coupon", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> fiscalCoupon(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=cupom-fiscal-locacao-" + id + ".pdf")
                .body(service.fiscalCoupon(id));
    }

    @PreAuthorize(RENTAL_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
