package com.locadora_rdt_backend.modules.reports.financial_reports.mapper;

import com.locadora_rdt_backend.modules.reports.financial_reports.dto.FinancialReportDTO;
import com.locadora_rdt_backend.modules.reports.financial_reports.model.FinancialReport;
import org.springframework.stereotype.Component;

@Component
public class FinancialReportMapper {

    public FinancialReportMapper() {
    }

    public FinancialReportDTO toDTO(FinancialReport entity) {

        FinancialReportDTO dto = new FinancialReportDTO();

        dto.setReceivableTotal(entity.getReceivableTotal());
        dto.setPayableTotal(entity.getPayableTotal());
        dto.setBalance(entity.getBalance());
        dto.setReceivableCount(entity.getReceivableCount());
        dto.setPayableCount(entity.getPayableCount());
        dto.setYear(entity.getYear());
        dto.setMonths(entity.getMonths());

        return dto;
    }
}
