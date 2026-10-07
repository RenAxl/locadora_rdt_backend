package com.locadora_rdt_backend.modules.reports.stock_reports.service;

import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportOptionsDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFileDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportFilterDTO;

public interface StockReportService {
    StockReportFileDTO generate(String reportType, String format, StockReportFilterDTO filters);
    StockReportOptionsDTO options();
    StockReportDTO summary(StockReportFilterDTO filters);
}
