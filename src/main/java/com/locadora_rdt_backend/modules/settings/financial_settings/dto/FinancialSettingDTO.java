package com.locadora_rdt_backend.modules.settings.financial_settings.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public class FinancialSettingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private BigDecimal defaultLateFeePercent;
    private BigDecimal defaultLateInterestPercent;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    public FinancialSettingDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getDefaultLateFeePercent() {
        return defaultLateFeePercent;
    }

    public void setDefaultLateFeePercent(BigDecimal defaultLateFeePercent) {
        this.defaultLateFeePercent = defaultLateFeePercent;
    }

    public BigDecimal getDefaultLateInterestPercent() {
        return defaultLateInterestPercent;
    }

    public void setDefaultLateInterestPercent(BigDecimal defaultLateInterestPercent) {
        this.defaultLateInterestPercent = defaultLateInterestPercent;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
