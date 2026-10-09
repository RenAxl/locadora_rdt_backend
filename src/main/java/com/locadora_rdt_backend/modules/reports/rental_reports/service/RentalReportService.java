package com.locadora_rdt_backend.modules.reports.rental_reports.service;

import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFileDTO;
import com.locadora_rdt_backend.modules.reports.rental_reports.dto.RentalReportFilterDTO;

public interface RentalReportService {

    RentalReportFileDTO generate(String reportType, String format, RentalReportFilterDTO filters);

    RentalReportDTO comparison(RentalReportFilterDTO filters);
}
