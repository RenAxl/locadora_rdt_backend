package com.locadora_rdt_backend.modules.financial.payment_frequencies.controller;

import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.*;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.service.PaymentFrequencyService;
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
@RequestMapping(value = "/payment-frequencies")
public class PaymentFrequencyController {

    private final PaymentFrequencyService service;

    public PaymentFrequencyController(PaymentFrequencyService service) {
        this.service = service;
    }

    @PreAuthorize(FREQUENCY_READ)
    @GetMapping
    public ResponseEntity<Page<PaymentFrequencyDTO>> findAllPaged(
            @RequestParam(value = "frequency", defaultValue = "") String frequency,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "3") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "frequency") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<PaymentFrequencyDTO> list = service.findAllPaged(frequency.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(FREQUENCY_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<PaymentFrequencyDTO> findById(@PathVariable Long id) {
        PaymentFrequencyDTO paymentFrequencyDto = service.findById(id);
        return ResponseEntity.ok().body(paymentFrequencyDto);
    }

    @PreAuthorize(FREQUENCY_WRITE)
    @PostMapping
    public ResponseEntity<PaymentFrequencyDTO> insert(@Valid @RequestBody PaymentFrequencyInsertDTO dto) {
        PaymentFrequencyDTO paymentFrequencyDto = service.insert(dto);
        return ControllerResponseBuilder.created(paymentFrequencyDto.getId(), paymentFrequencyDto);
    }

    @PreAuthorize(FREQUENCY_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<PaymentFrequencyDTO> update(@PathVariable Long id, @Valid @RequestBody PaymentFrequencyUpdateDTO dto) {
        PaymentFrequencyDTO paymentFrequencyDto = service.update(id, dto);
        return ResponseEntity.ok().body(paymentFrequencyDto);
    }

    @PreAuthorize(FREQUENCY_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(FREQUENCY_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }

}
