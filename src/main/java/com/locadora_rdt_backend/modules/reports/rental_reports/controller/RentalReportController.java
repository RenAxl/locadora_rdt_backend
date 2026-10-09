package com.locadora_rdt_backend.modules.reports.rental_reports.controller;

import com.locadora_rdt_backend.modules.reports.rental_reports.dto.*;
import com.locadora_rdt_backend.modules.reports.rental_reports.service.RentalReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.locadora_rdt_backend.shared.constants.PermissionConstants.*;

@RestController
@RequestMapping(value = "/reports/rental-reports")
public class RentalReportController {

    private final RentalReportService service;

    public RentalReportController(RentalReportService service) {
        this.service = service;
    }

    @PreAuthorize(RENTAL_REPORTS_READ)
    @GetMapping(value = "/{reportType}/{format}")
    public ResponseEntity<byte[]> generate(
            @PathVariable String reportType,
            @PathVariable String format,
            @ModelAttribute RentalReportFilterDTO filters
    ) {

        RentalReportFileDTO dto = service.generate(reportType, format, filters);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(dto.getFileName())
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(dto.getContentType()));
        headers.setContentDisposition(disposition);

        return ResponseEntity.ok().headers(headers).body(dto.getData());
    }

    @PreAuthorize(RENTAL_REPORTS_READ)
    @GetMapping(value = "/comparison")
    public ResponseEntity<RentalReportDTO> comparison(
            @ModelAttribute RentalReportFilterDTO filters
    ) {

        RentalReportDTO rentalReportDTO = service.comparison(filters);

        return ResponseEntity.ok().body(rentalReportDTO);
    }
}
