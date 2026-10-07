package com.locadora_rdt_backend.modules.reports.stock_reports.controller;

import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportOptionsDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFileDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFilterDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.service.StockReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.STOCK_REPORTS_READ;

@RestController
@RequestMapping("/reports/stock-reports")
public class StockReportController {

    private final StockReportService service;

    public StockReportController(StockReportService service) {
        this.service = service;
    }

    @PreAuthorize(STOCK_REPORTS_READ)
    @GetMapping("/{reportType}/{format}")
    public ResponseEntity<byte[]> generate(@PathVariable String reportType, @PathVariable String format,
                                           @ModelAttribute StockReportFilterDTO filters) {
        StockReportFileDTO file = service.generate(reportType, format, filters);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(file.getFileName()).build());
        return ResponseEntity.ok().headers(headers).body(file.getData());
    }

    @PreAuthorize(STOCK_REPORTS_READ)
    @GetMapping("/options")
    public ResponseEntity<StockReportOptionsDTO> options() {
        return ResponseEntity.ok(service.options());
    }

    @PreAuthorize(STOCK_REPORTS_READ)
    @GetMapping("/summary")
    public ResponseEntity<StockReportDTO> summary(@ModelAttribute StockReportFilterDTO filters) {
        return ResponseEntity.ok(service.summary(filters));
    }
}
