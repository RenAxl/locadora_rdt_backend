package com.locadora_rdt_backend.modules.reports.rental_reports.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class RentalReportMonth implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer month;
    private String label;
    private BigDecimal rentalTotal;
    private BigDecimal paidTotal;

    public RentalReportMonth() {
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

    public BigDecimal getRentalTotal() {
        return rentalTotal;
    }

    public void setRentalTotal(BigDecimal rentalTotal) {
        this.rentalTotal = rentalTotal;
    }

    public BigDecimal getPaidTotal() {
        return paidTotal;
    }

    public void setPaidTotal(BigDecimal paidTotal) {
        this.paidTotal = paidTotal;
    }
}
