package com.locadora_rdt_backend.modules.financial.payment_methods.controller;

import com.locadora_rdt_backend.modules.financial.payment_methods.dto.*;
import com.locadora_rdt_backend.modules.financial.payment_methods.service.PaymentMethodService;
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
@RequestMapping(value = "/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService service;

    public PaymentMethodController(PaymentMethodService service) {
        this.service = service;
    }

    @PreAuthorize(METHODS_READ)
    @GetMapping
    public ResponseEntity<Page<PaymentMethodDTO>> findAllPaged(
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = "3") Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = "ASC") String direction,
            @RequestParam(value = "orderBy", defaultValue = "name") String orderBy) {

        PageRequest pageRequest = ControllerResponseBuilder.pageRequest(page, linesPerPage, direction, orderBy);

        Page<PaymentMethodDTO> list = service.findAllPaged(name.trim(), pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(METHODS_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<PaymentMethodDTO> findById(@PathVariable Long id) {
        PaymentMethodDTO paymentMethodDto = service.findById(id);
        return ResponseEntity.ok().body(paymentMethodDto);
    }

    @PreAuthorize(METHODS_WRITE)
    @PostMapping
    public ResponseEntity<PaymentMethodDTO> insert(@Valid @RequestBody PaymentMethodInsertDTO dto) {
        PaymentMethodDTO paymentMethodDto = service.insert(dto);
        return ControllerResponseBuilder.created(paymentMethodDto.getId(), paymentMethodDto);
    }

    @PreAuthorize(METHODS_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<PaymentMethodDTO> update(@PathVariable Long id, @Valid @RequestBody PaymentMethodUpdateDTO dto) {
        PaymentMethodDTO paymentMethodDto = service.update(id, dto);
        return ResponseEntity.ok().body(paymentMethodDto);
    }

    @PreAuthorize(METHODS_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(METHODS_DELETE)
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> ids) {
        service.deleteAll(ids);
        return ResponseEntity.noContent().build();
    }
}
