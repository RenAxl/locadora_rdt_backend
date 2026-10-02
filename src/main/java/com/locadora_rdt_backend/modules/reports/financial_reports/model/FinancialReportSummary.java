package com.locadora_rdt_backend.modules.reports.financial_reports.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class FinancialReportSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer quantity = 0;
    private BigDecimal total = BigDecimal.ZERO;
    private BigDecimal paid = BigDecimal.ZERO;

    public FinancialReportSummary() {
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
