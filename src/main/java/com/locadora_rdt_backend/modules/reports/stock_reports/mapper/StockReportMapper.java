package com.locadora_rdt_backend.modules.reports.stock_reports.mapper;

import com.locadora_rdt_backend.modules.reports.stock_reports.dto.StockReportDTO;
import com.locadora_rdt_backend.modules.reports.stock_reports.model.StockReportSummary;
import org.springframework.stereotype.Component;

@Component
public class StockReportMapper {

    public StockReportDTO toDTO(StockReportSummary summary) {
        StockReportDTO dto = new StockReportDTO();
        dto.setItemCount(summary.getItemCount());
        dto.setTotalQuantity(summary.getTotalQuantity());
        dto.setAvailableQuantity(summary.getAvailableQuantity());
        dto.setUnavailableQuantity(summary.getUnavailableQuantity());
        dto.setMaintenanceQuantity(summary.getMaintenanceQuantity());
        dto.setDamagedQuantity(summary.getDamagedQuantity());
        dto.setLostQuantity(summary.getLostQuantity());
        dto.setLowStockItemCount(summary.getLowStockItemCount());
        return dto;
    }
}
