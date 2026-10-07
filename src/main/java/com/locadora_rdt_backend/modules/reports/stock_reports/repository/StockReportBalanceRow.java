package com.locadora_rdt_backend.modules.reports.stock_reports.repository;

public interface StockReportBalanceRow {
    Long getItemId();
    String getItemName();
    String getCategoryName();
    Long getTotalQuantity();
    Long getAvailableQuantity();
    Long getUnavailableQuantity();
    Long getMaintenanceQuantity();
    Long getDamagedQuantity();
    Long getLostQuantity();
    Integer getMinimumQuantity();
}
