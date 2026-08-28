package com.locadora_rdt_backend.shared.reports.dto;

import java.util.List;
import java.util.Map;

public class ListingExportRequestDTO {

    private String title;
    private List<String> columns;
    private List<Map<String, String>> rows;

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

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, String>> rows) {
        this.rows = rows;
    }
}
