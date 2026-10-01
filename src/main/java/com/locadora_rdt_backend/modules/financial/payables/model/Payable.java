package com.locadora_rdt_backend.modules.financial.payables.model;

import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.identity.users.model.User;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_payable")
public class Payable implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private BigDecimal amount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private String note;

    @Column(name = "file_name")
    private String fileName;

    @Column(nullable = false)
    private String status;

    private String reference;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "late_fee")
    private BigDecimal lateFee;

    @Column(name = "late_interest")
    private BigDecimal lateInterest;

    private BigDecimal discount;

    private BigDecimal fee;

    private BigDecimal subtotal;

    @Column(nullable = false)
    private Boolean paid = false;

    @Column(name = "remaining_balance")
    private BigDecimal remainingBalance;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean residual = false;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean canceled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_frequency_id")
    private PaymentFrequency paymentFrequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frequency_id")
    private PaymentFrequency frequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by")
    private User paidBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_payable_id")
    private Payable parentPayable;

    @OneToMany(mappedBy = "parentPayable")
    private List<Payable> residuals = new ArrayList<>();

    public Payable() {
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdDate = now;
        createdAt = now;

        if (paid == null) {
            paid = false;
        }

        if (residual == null) {
            residual = false;
        }

        if (canceled == null) {
            canceled = false;
        }

        updatePaymentFields();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        updatePaymentFields();
    }

    private void updatePaymentFields() {
        if (frequency == null) {
            frequency = paymentFrequency;
        }

        if (Boolean.TRUE.equals(canceled)) {
            status = PayableStatus.CANCELED.name();
        } else if (Boolean.TRUE.equals(paid)) {
            status = PayableStatus.PAID.name();
        } else {
            status = PayableStatus.PENDING.name();
        }
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
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

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentFrequency getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(PaymentFrequency paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
        this.frequency = paymentFrequency;
    }

    public PaymentFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(PaymentFrequency frequency) {
        this.frequency = frequency;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public User getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(User paidBy) {
        this.paidBy = paidBy;
    }

    public Payable getParentPayable() {
        return parentPayable;
    }

    public void setParentPayable(Payable parentPayable) {
        this.parentPayable = parentPayable;
    }

    public List<Payable> getResiduals() {
        return residuals;
    }

    public void setResiduals(List<Payable> residuals) {
        this.residuals = residuals;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Payable payable = (Payable) o;
        return Objects.equals(id, payable.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
