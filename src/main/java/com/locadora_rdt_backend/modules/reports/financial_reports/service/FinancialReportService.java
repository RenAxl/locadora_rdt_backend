package com.locadora_rdt_backend.modules.reports.financial_reports.service;

import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportDTO;
import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportFileDTO;
import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportFilterDTO;

public interface FinancialReportService {

    FinancialReportFileDTO generate(String reportType, String format, FinancialReportFilterDTO filters);

    FinancialReportDTO comparison(FinancialReportFilterDTO filters);
}
