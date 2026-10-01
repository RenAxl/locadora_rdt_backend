package com.locadora_rdt_backend.modules.financial.payables.controller;

import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;
import com.locadora_rdt_backend.modules.financial.payables.dto.*;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/payables")
public class PayableController {

    private final PayableService service;

    public PayableController(PayableService service) {
        this.service = service;
    }

    @PreAuthorize(PAYABLE_READ)
    @GetMapping
    public ResponseEntity<Page<PayableDTO>> findAllPaged(
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = PayableConstants.STATUS_ALL) String status,
            @RequestParam(value = "dateType", defaultValue = PayableConstants.PERIOD_DUE) String dateType,
            @RequestParam(value = "periodType", required = false) String periodType,
            @RequestParam(value = "supplierId", required = false) Long supplierId,
            @RequestParam(value = "employeeId", required = false) Long employeeId,
            @RequestParam(value = "paymentMethodId", required = false) Long paymentMethodId,
            @RequestParam(value = "paymentFrequencyId", required = false) Long paymentFrequencyId,
            @RequestParam(value = "minimumAmount", required = false) BigDecimal minimumAmount,
            @RequestParam(value = "maximumAmount", required = false) BigDecimal maximumAmount,
            @RequestParam(value = "page", defaultValue = PayableConstants.DEFAULT_PAGE) Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = PayableConstants.DEFAULT_LINES_PER_PAGE) Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = PayableConstants.DIRECTION_ASC) String direction,
            @RequestParam(value = "orderBy", defaultValue = PayableConstants.ORDER_BY_DUE_DATE) String orderBy
    ) {
        PageRequest pageRequest = PageRequest.of(page, linesPerPage);
        PayableFilterDTO filters = new PayableFilterDTO();

        if (search == null || search.trim().isEmpty()) {
            filters.setSearch(description.trim());
        } else {
            filters.setSearch(search.trim());
        }

        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setStatus(status);

        if (periodType == null || periodType.trim().isEmpty()) {
            filters.setPeriodType(dateType);
        } else {
            filters.setPeriodType(periodType);
        }

        filters.setSupplierId(supplierId);
        filters.setEmployeeId(employeeId);
        filters.setPaymentMethodId(paymentMethodId);
        filters.setPaymentFrequencyId(paymentFrequencyId);
        filters.setMinimumAmount(minimumAmount);
        filters.setMaximumAmount(maximumAmount);
        filters.setOrderBy(orderBy);
        filters.setDirection(direction);

        Page<PayableDTO> list = service.findAllPaged(filters, pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(PAYABLE_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<PayableDTO> findById(@PathVariable Long id) {
        PayableDTO payableDto = service.findById(id);
        return ResponseEntity.ok().body(payableDto);
    }

    @PreAuthorize(PAYABLE_WRITE)
    @PostMapping
    public ResponseEntity<PayableDTO> insert(@Valid @RequestBody PayableInsertDTO dto) {
        PayableDTO payableDto = service.insert(dto);
        return ControllerResponseBuilder.created(payableDto.getId(), payableDto);
    }

    @PreAuthorize(PAYABLE_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<PayableDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PayableUpdateDTO dto
    ) {
        PayableDTO payableDto = service.update(id, dto);
        return ResponseEntity.ok().body(payableDto);
    }

    @PreAuthorize(PAYABLE_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(PAYABLE_WRITE)
    @PostMapping("/{id}/payments")
    public ResponseEntity<PayableDTO> pay(
            @PathVariable Long id,
            @Valid @RequestBody PayablePaymentDTO dto
    ) {
        PayableDTO payableDto = service.pay(id, dto);
        return ResponseEntity.ok().body(payableDto);
    }

    @PreAuthorize(PAYABLE_READ)
    @GetMapping("/report")
    public ResponseEntity<PayableReportDTO> report(
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = PayableConstants.STATUS_ALL) String status,
            @RequestParam(value = "dateType", defaultValue = PayableConstants.PERIOD_DUE) String dateType
    ) {
        PayableReportDTO payableReportDto = service.report(description, startDate, endDate, status, dateType);
        return ResponseEntity.ok().body(payableReportDto);
    }

}
