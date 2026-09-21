package com.locadora_rdt_backend.modules.settings.system_settings.model;

import com.locadora_rdt_backend.modules.settings.system_settings.constants.SystemSettingConstants;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "tb_system_setting", uniqueConstraints =
        @UniqueConstraint(name = "uk_system_setting_singleton_key", columnNames = "singleton_key"))
public class SystemSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "singleton_key", nullable = false, unique = true, length = 30)
    private String singletonKey = SystemSettingConstants.DEFAULT_SINGLETON_KEY;

    @Column(name = "company_name", nullable = false, length = 120)
    private String companyName;

    @Column(name = "icon", length = 40)
    private String icon = SystemSettingConstants.DEFAULT_ICON;

    @Embedded
    private Address address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public SystemSetting() {
    }

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();

        if (singletonKey == null) {
            singletonKey = SystemSettingConstants.DEFAULT_SINGLETON_KEY;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
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
