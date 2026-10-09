package com.locadora_rdt_backend.modules.reports.rental_reports.dto;

import java.io.Serializable;
import com.locadora_rdt_backend.modules.reports.rental_reports.model.RentalReportMonth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RentalReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal rentalTotal;
    private BigDecimal paidTotal;
    private Integer rentalCount;
    private Integer paidCount;
    private Integer year;
    private List<RentalReportMonth> months = new ArrayList<>();

    public RentalReportDTO() {
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

    public Integer getRentalCount() {
        return rentalCount;
    }

    public void setRentalCount(Integer rentalCount) {
        this.rentalCount = rentalCount;
    }

    public Integer getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(Integer paidCount) {
        this.paidCount = paidCount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<RentalReportMonth> getMonths() {
        return months;
    }

    public void setMonths(List<RentalReportMonth> months) {
        this.months = months;
    }
}
