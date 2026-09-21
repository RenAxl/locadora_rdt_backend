package com.locadora_rdt_backend.modules.settings.system_settings.dto;

import com.locadora_rdt_backend.modules.settings.system_settings.constants.SystemSettingConstants;
import com.locadora_rdt_backend.modules.settings.system_settings.model.Address;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

public class SystemSettingUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = SystemSettingConstants.COMPANY_NAME_MAX_LENGTH,
            message = SystemSettingConstants.COMPANY_NAME_MAX_LENGTH_MESSAGE)
    @NotBlank(message = SystemSettingConstants.COMPANY_NAME_REQUIRED)
    private String companyName;

    @Pattern(regexp = SystemSettingConstants.ICON_PATTERN, message = SystemSettingConstants.INVALID_ICON)
    private String icon;

    @Valid
    @NotNull(message = SystemSettingConstants.ADDRESS_REQUIRED)
    private Address address;

    public SystemSettingUpdateDTO() {
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
}
