package com.locadora_rdt_backend.modules.reports.financial_reports.controller;

import com.locadora_rdt_backend.modules.reports.financial_reports.dto.*;
import com.locadora_rdt_backend.modules.reports.financial_reports.service.FinancialReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/reports/financial-reports")
public class FinancialReportController {

    private final FinancialReportService service;

    public FinancialReportController(FinancialReportService service) {
        this.service = service;
    }

    @PreAuthorize(FINANCIAL_REPORTS_READ)
    @GetMapping(value = "/{reportType}/{format}")
    public ResponseEntity<byte[]> generate(
            @PathVariable String reportType,
            @PathVariable String format,
            @ModelAttribute FinancialReportFilterDTO filters
    ) {

        FinancialReportFileDTO dto = service.generate(reportType, format, filters);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(FINANCIAL_REPORTS_READ)
    @GetMapping(value = "/comparison")
    public ResponseEntity<FinancialReportDTO> comparison(
            @ModelAttribute FinancialReportFilterDTO filters
    ) {

        FinancialReportDTO financialReportDTO = service.comparison(filters);

        return ResponseEntity.ok().body(financialReportDTO);
    }
}
