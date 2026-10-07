package com.locadora_rdt_backend.modules.reports.stock_reports.dto;

import java.util.ArrayList;
import java.util.List;

public class StockReportOptionsDTO {
    private List<StockReportOptionDTO> categories = new ArrayList<>();
    private List<StockReportOptionDTO> items = new ArrayList<>();

    public List<StockReportOptionDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<StockReportOptionDTO> categories) {
        this.categories = categories;
    }

    public List<StockReportOptionDTO> getItems() {
        return items;
    }

    public void setItems(List<StockReportOptionDTO> items) {
        this.items = items;
    }

}
