package com.locadora_rdt_backend.modules.settings.financial_settings.model;

import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "tb_financial_setting",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_financial_setting_singleton_key",
                columnNames = "singleton_key"
        )
)
public class FinancialSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "singleton_key", nullable = false, unique = true, length = 30)
    private String singletonKey = FinancialSettingConstants.DEFAULT_SINGLETON_KEY;

    @Column(name = "default_late_fee_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultLateFeePercent = BigDecimal.ZERO;

    @Column(name = "default_late_interest_percent", nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultLateInterestPercent = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public FinancialSetting() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();

        if (singletonKey == null) {
            singletonKey = FinancialSettingConstants.DEFAULT_SINGLETON_KEY;
        }

        if (defaultLateFeePercent == null) {
            defaultLateFeePercent = BigDecimal.ZERO;
        }

        if (defaultLateInterestPercent == null) {
            defaultLateInterestPercent = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSingletonKey() {
        return singletonKey;
    }

    public void setSingletonKey(String singletonKey) {
        this.singletonKey = singletonKey;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FinancialSetting financialSetting = (FinancialSetting) o;
        return Objects.equals(id, financialSetting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
