package com.locadora_rdt_backend.modules.financial.receivables.dto;

import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReceivablePaymentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = ReceivableConstants.PAYMENT_AMOUNT_REQUIRED)
    @DecimalMin(value = ReceivableConstants.MINIMUM_AMOUNT, message = ReceivableConstants.PAYMENT_AMOUNT_POSITIVE_VALIDATION)
    private BigDecimal paymentAmount;

    private LocalDate paymentDate;
    private Long paymentMethodId;
    private BigDecimal subtotal;
    private BigDecimal fee;
    private BigDecimal lateInterest;
    private BigDecimal lateFee;

    public ReceivablePaymentDTO() {
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getLateInterest() {
        return lateInterest;
    }

    public void setLateInterest(BigDecimal lateInterest) {
        this.lateInterest = lateInterest;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }
}
