package com.locadora_rdt_backend.modules.financial.receivables.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ReceivableDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String description;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private Instant createdDate;
    private Instant createdAt;
    private Instant updatedAt;
    private String note;
    private String fileName;
    private Boolean paid;
    private BigDecimal remainingBalance;
    private BigDecimal lateFee;
    private BigDecimal lateInterest;
    private BigDecimal discount;
    private BigDecimal fee;
    private BigDecimal subtotal;
    private BigDecimal currentAmountWithLateCharges;
    private Long overdueDays;
    private BigDecimal calculatedLateInterest;
    private BigDecimal calculatedLateFee;
    private Boolean residual;
    private Boolean canceled;
    private Long parentReceivableId;
    private Long customerId;
    private String customerName;
    private Long paymentMethodId;
    private String paymentMethodName;
    private Long paymentFrequencyId;
    private String paymentFrequency;
    private Long createdById;
    private String createdByName;
    private Long updatedById;
    private String updatedByName;
    private Long paidById;
    private String paidByName;

    public ReceivableDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public BigDecimal getLateInterest() {
        return lateInterest;
    }

    public void setLateInterest(BigDecimal lateInterest) {
        this.lateInterest = lateInterest;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getCurrentAmountWithLateCharges() {
        return currentAmountWithLateCharges;
    }

    public void setCurrentAmountWithLateCharges(BigDecimal currentAmountWithLateCharges) {
        this.currentAmountWithLateCharges = currentAmountWithLateCharges;
    }

    public Long getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Long overdueDays) {
        this.overdueDays = overdueDays;
    }

    public BigDecimal getCalculatedLateInterest() {
        return calculatedLateInterest;
    }

    public void setCalculatedLateInterest(BigDecimal calculatedLateInterest) {
        this.calculatedLateInterest = calculatedLateInterest;
    }

    public BigDecimal getCalculatedLateFee() {
        return calculatedLateFee;
    }

    public void setCalculatedLateFee(BigDecimal calculatedLateFee) {
        this.calculatedLateFee = calculatedLateFee;
    }

    public Boolean getResidual() {
        return residual;
    }

    public void setResidual(Boolean residual) {
        this.residual = residual;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public Long getParentReceivableId() {
        return parentReceivableId;
    }

    public void setParentReceivableId(Long parentReceivableId) {
        this.parentReceivableId = parentReceivableId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getPaymentMethodName() {
        return paymentMethodName;
    }

    public void setPaymentMethodName(String paymentMethodName) {
        this.paymentMethodName = paymentMethodName;
    }

    public Long getPaymentFrequencyId() {
        return paymentFrequencyId;
    }

    public void setPaymentFrequencyId(Long paymentFrequencyId) {
        this.paymentFrequencyId = paymentFrequencyId;
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = updatedByName;
    }

    public Long getPaidById() {
        return paidById;
    }

    public void setPaidById(Long paidById) {
        this.paidById = paidById;
    }

    public String getPaidByName() {
        return paidByName;
    }

    public void setPaidByName(String paidByName) {
        this.paidByName = paidByName;
    }
}
