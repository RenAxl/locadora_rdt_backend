package com.locadora_rdt_backend.modules.reports.rental_reports.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class RentalReportSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer quantity = 0;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal paid = BigDecimal.ZERO;

    public RentalReportSummary() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }
}
