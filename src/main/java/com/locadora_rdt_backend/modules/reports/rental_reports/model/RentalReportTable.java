package com.locadora_rdt_backend.modules.reports.rental_reports.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RentalReportTable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private List<String> columns = new ArrayList<>();
    private List<Map<String, ?>> rows = new ArrayList<>();

    public RentalReportTable() {
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
