package com.locadora_rdt_backend.modules.financial.payables.dto;

import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PayableInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = PayableConstants.DESCRIPTION_REQUIRED)
    @Size(min = PayableConstants.DESCRIPTION_MIN_LENGTH, max = PayableConstants.DESCRIPTION_MAX_LENGTH,
            message = PayableConstants.DESCRIPTION_LENGTH)
    private String description;

    @NotNull(message = PayableConstants.AMOUNT_REQUIRED)
    @DecimalMin(value = PayableConstants.MINIMUM_AMOUNT, message = PayableConstants.AMOUNT_MUST_BE_POSITIVE)
    private BigDecimal amount;

    @NotNull(message = PayableConstants.DUE_DATE_REQUIRED)
    private LocalDate dueDate;
    private LocalDate paymentDate;

    private Long supplierId;
    private Long employeeId;
    private Long paymentMethodId;
    private Long paymentFrequencyId;
    private String note;
    private String fileName;

    public PayableInsertDTO() {
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

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
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
