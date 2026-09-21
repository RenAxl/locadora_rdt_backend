package com.locadora_rdt_backend.modules.settings.system_settings.dto;

import com.locadora_rdt_backend.modules.settings.system_settings.model.Address;

import java.io.Serializable;

public class SystemSettingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String companyName;
    private Address address;

    public SystemSettingDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
