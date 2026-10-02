package com.locadora_rdt_backend.modules.financial.receivables.dto;

import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ReceivableInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = ReceivableConstants.DESCRIPTION_REQUIRED)
    @Size(min = ReceivableConstants.DESCRIPTION_MIN_LENGTH, max = ReceivableConstants.DESCRIPTION_MAX_LENGTH,
            message = ReceivableConstants.DESCRIPTION_LENGTH)
    private String description;

    @NotNull(message = ReceivableConstants.AMOUNT_REQUIRED)
    @DecimalMin(value = ReceivableConstants.MINIMUM_AMOUNT, message = ReceivableConstants.AMOUNT_MUST_BE_POSITIVE)
    private BigDecimal amount;

    @NotNull(message = ReceivableConstants.DUE_DATE_REQUIRED)
    private LocalDate dueDate;
    private LocalDate paymentDate;

    @NotNull(message = ReceivableConstants.CUSTOMER_REQUIRED)
    private Long customerId;
    private Long paymentMethodId;
    private Long paymentFrequencyId;
    private String note;
    private String fileName;

    public ReceivableInsertDTO() {
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getPaymentFrequencyId() {
        return paymentFrequencyId;
    }

    public void setPaymentFrequencyId(Long paymentFrequencyId) {
        this.paymentFrequencyId = paymentFrequencyId;
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
}
