package com.locadora_rdt_backend.modules.reports.financial_reports.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinancialReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal receivableTotal;
    private BigDecimal payableTotal;
    private BigDecimal balance;
    private Integer receivableCount;
    private Integer payableCount;
    private Integer year;
    private List<FinancialReportMonth> months = new ArrayList<>();

    public FinancialReport() {
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getReceivableCount() {
        return receivableCount;
    }

    public void setReceivableCount(Integer receivableCount) {
        this.receivableCount = receivableCount;
    }

    public Integer getPayableCount() {
        return payableCount;
    }

    public void setPayableCount(Integer payableCount) {
        this.payableCount = payableCount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<FinancialReportMonth> getMonths() {
        return months;
    }

    public void setMonths(List<FinancialReportMonth> months) {
        this.months = months;
    }
}
