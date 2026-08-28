package com.locadora_rdt_backend.shared.reports.service;

import com.locadora_rdt_backend.shared.reports.dto.ListingExportRequestDTO;
import com.locadora_rdt_backend.shared.reports.generator.JasperReportGenerator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ListingExportService {

    private final JasperReportGenerator reportGenerator;

    public ListingExportService(JasperReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    public byte[] exportExcel(ListingExportRequestDTO request) {
        validate(request);

        List<Map<String, ?>> rows = new ArrayList<>();

        for (Map<String, String> requestRow : request.getRows()) {
            Map<String, Object> row = new LinkedHashMap<>();

            for (int index = 0; index < request.getColumns().size(); index++) {
                row.put("column" + index, requestRow.getOrDefault("column" + index, ""));
            }

            rows.add(row);
        }

        return reportGenerator.generateExcel(
                request.getTitle().trim(),
                request.getColumns(),
                rows
        );
    }

    private void validate(ListingExportRequestDTO request) {
        if (request == null || request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da planilha é obrigatório.");
        }

        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            throw new IllegalArgumentException("A planilha deve possuir ao menos uma coluna.");
        }

        if (request.getRows() == null) {
            throw new IllegalArgumentException("Os dados da planilha são obrigatórios.");
        }
    }
}
