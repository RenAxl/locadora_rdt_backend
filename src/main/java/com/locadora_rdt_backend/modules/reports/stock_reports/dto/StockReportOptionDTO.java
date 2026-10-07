package com.locadora_rdt_backend.modules.reports.stock_reports.dto;

public class StockReportOptionDTO {
    private Long id;
    private String name;
    private Long categoryId;

    public StockReportOptionDTO() {
    }

    public StockReportOptionDTO(Long id, String name, Long categoryId) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

}
