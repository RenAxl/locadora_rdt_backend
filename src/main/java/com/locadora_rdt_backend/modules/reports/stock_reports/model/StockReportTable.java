package com.locadora_rdt_backend.modules.reports.stock_reports.model;

import java.util.List;
import java.util.Map;

public class StockReportTable {

    private String title;
    private List<String> columns;
    private List<Map<String, ?>> rows;

    public StockReportTable() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Map<String, ?>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, ?>> rows) {
        this.rows = rows;
    }
}
