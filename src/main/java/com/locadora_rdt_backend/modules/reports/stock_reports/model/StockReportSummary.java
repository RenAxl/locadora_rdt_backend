package com.locadora_rdt_backend.modules.reports.stock_reports.model;

public class StockReportSummary {

    private long itemCount;
    private long totalQuantity;
    private long availableQuantity;
    private long unavailableQuantity;
    private long maintenanceQuantity;
    private long damagedQuantity;
    private long lostQuantity;
    private long lowStockItemCount;

    public StockReportSummary() {
    }

    public long getItemCount() {
        return itemCount;
    }

    public void setItemCount(long itemCount) {
        this.itemCount = itemCount;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public long getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(long availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public long getUnavailableQuantity() {
        return unavailableQuantity;
    }

    public void setUnavailableQuantity(long unavailableQuantity) {
        this.unavailableQuantity = unavailableQuantity;
    }

    public long getMaintenanceQuantity() {
        return maintenanceQuantity;
    }

    public void setMaintenanceQuantity(long maintenanceQuantity) {
        this.maintenanceQuantity = maintenanceQuantity;
    }

    public long getDamagedQuantity() {
        return damagedQuantity;
    }

    public void setDamagedQuantity(long damagedQuantity) {
        this.damagedQuantity = damagedQuantity;
    }

    public long getLostQuantity() {
        return lostQuantity;
    }

    public void setLostQuantity(long lostQuantity) {
        this.lostQuantity = lostQuantity;
    }

    public long getLowStockItemCount() {
        return lowStockItemCount;
    }

    public void setLowStockItemCount(long lowStockItemCount) {
        this.lowStockItemCount = lowStockItemCount;
    }
}
