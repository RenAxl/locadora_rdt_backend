package com.locadora_rdt_backend.modules.reports.financial_reports.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class FinancialReportMonth implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer month;
    private String label;
    private BigDecimal receivableTotal;
    private BigDecimal payableTotal;

    public FinancialReportMonth() {
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getReceivableTotal() {
        return receivableTotal;
    }

    public void setReceivableTotal(BigDecimal receivableTotal) {
        this.receivableTotal = receivableTotal;
    }

    public BigDecimal getPayableTotal() {
        return payableTotal;
    }

    public void setPayableTotal(BigDecimal payableTotal) {
        this.payableTotal = payableTotal;
    }
}
