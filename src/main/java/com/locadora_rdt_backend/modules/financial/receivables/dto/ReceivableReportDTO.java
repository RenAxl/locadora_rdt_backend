package com.locadora_rdt_backend.modules.financial.receivables.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ReceivableReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long totalItems;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal openAmount;

    public ReceivableReportDTO() {
    }

    public ReceivableReportDTO(Long totalItems, BigDecimal totalAmount, BigDecimal paidAmount, BigDecimal openAmount) {
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.openAmount = openAmount;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getOpenAmount() {
        return openAmount;
    }

    public void setOpenAmount(BigDecimal openAmount) {
        this.openAmount = openAmount;
    }
}
