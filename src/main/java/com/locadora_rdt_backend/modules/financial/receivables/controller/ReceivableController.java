package com.locadora_rdt_backend.modules.financial.receivables.controller;

import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.dto.*;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableService;
import com.locadora_rdt_backend.shared.web.ControllerResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/receivables")
public class ReceivableController {

    private final ReceivableService service;

    public ReceivableController(ReceivableService service) {
        this.service = service;
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping
    public ResponseEntity<Page<ReceivableDTO>> findAllPaged(
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = ReceivableConstants.STATUS_ALL) String status,
            @RequestParam(value = "dateType", defaultValue = ReceivableConstants.PERIOD_DUE) String dateType,
            @RequestParam(value = "periodType", required = false) String periodType,
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "paymentMethodId", required = false) Long paymentMethodId,
            @RequestParam(value = "paymentFrequencyId", required = false) Long paymentFrequencyId,
            @RequestParam(value = "minimumAmount", required = false) BigDecimal minimumAmount,
            @RequestParam(value = "maximumAmount", required = false) BigDecimal maximumAmount,
            @RequestParam(value = "page", defaultValue = ReceivableConstants.DEFAULT_PAGE) Integer page,
            @RequestParam(value = "linesPerPage", defaultValue = ReceivableConstants.DEFAULT_LINES_PER_PAGE) Integer linesPerPage,
            @RequestParam(value = "direction", defaultValue = ReceivableConstants.DIRECTION_ASC) String direction,
            @RequestParam(value = "orderBy", defaultValue = ReceivableConstants.ORDER_BY_DUE_DATE) String orderBy
    ) {
        PageRequest pageRequest = PageRequest.of(page, linesPerPage);
        ReceivableFilterDTO filters = new ReceivableFilterDTO();

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

        filters.setCustomerId(customerId);
        filters.setPaymentMethodId(paymentMethodId);
        filters.setPaymentFrequencyId(paymentFrequencyId);
        filters.setMinimumAmount(minimumAmount);
        filters.setMaximumAmount(maximumAmount);
        filters.setOrderBy(orderBy);
        filters.setDirection(direction);

        Page<ReceivableDTO> list = service.findAllPaged(filters, pageRequest);

        return ResponseEntity.ok().body(list);
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping(value = "/{id}")
    public ResponseEntity<ReceivableDTO> findById(@PathVariable Long id) {
        ReceivableDTO receivableDto = service.findById(id);
        return ResponseEntity.ok().body(receivableDto);
    }

    @PreAuthorize(RECEIVABLE_WRITE)
    @PostMapping
    public ResponseEntity<ReceivableDTO> insert(@Valid @RequestBody ReceivableInsertDTO dto) {
        ReceivableDTO receivableDto = service.insert(dto);
        return ControllerResponseBuilder.created(receivableDto.getId(), receivableDto);
    }

    @PreAuthorize(RECEIVABLE_WRITE)
    @PutMapping(value = "/{id}")
    public ResponseEntity<ReceivableDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ReceivableUpdateDTO dto
    ) {
        ReceivableDTO receivableDto = service.update(id, dto);
        return ResponseEntity.ok().body(receivableDto);
    }

    @PreAuthorize(RECEIVABLE_DELETE)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(RECEIVABLE_WRITE)
    @PostMapping("/{id}/payments")
    public ResponseEntity<ReceivableDTO> pay(
            @PathVariable Long id,
            @Valid @RequestBody ReceivablePaymentDTO dto
    ) {
        ReceivableDTO receivableDto = service.pay(id, dto);
        return ResponseEntity.ok().body(receivableDto);
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping("/report")
    public ResponseEntity<ReceivableReportDTO> report(
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", defaultValue = ReceivableConstants.STATUS_ALL) String status,
            @RequestParam(value = "dateType", defaultValue = ReceivableConstants.PERIOD_DUE) String dateType
    ) {
        ReceivableReportDTO receivableReportDto = service.report(description, startDate, endDate, status, dateType);
        return ResponseEntity.ok().body(receivableReportDto);
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping(value = "/{id}/receipt", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> receipt(@PathVariable Long id) {
        byte[] data = service.receipt(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recibo-" + id + ".pdf")
                .body(data);
    }

    @PreAuthorize(RECEIVABLE_READ)
    @GetMapping(value = "/{id}/fiscal-coupon", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> fiscalCoupon(@PathVariable Long id) {
        byte[] data = service.fiscalCoupon(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=cupom-fiscal-" + id + ".pdf")
                .body(data);
    }
}
