package com.locadora_rdt_backend.shared.reports.controller;

import com.locadora_rdt_backend.shared.reports.dto.ListingExportRequestDTO;
import com.locadora_rdt_backend.shared.reports.service.ListingExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/listing-exports")
public class ListingExportController {

    private static final String XLSX_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ListingExportService service;

    public ListingExportController(ListingExportService service) {
        this.service = service;
    }

    @PostMapping(value = "/excel", produces = XLSX_MEDIA_TYPE)
    public ResponseEntity<byte[]> exportExcel(@RequestBody ListingExportRequestDTO request) {
        byte[] file = service.exportExcel(request);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(XLSX_MEDIA_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=listagem.xlsx")
                .body(file);
    }
}
