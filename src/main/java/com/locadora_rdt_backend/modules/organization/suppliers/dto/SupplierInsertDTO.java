package com.locadora_rdt_backend.modules.organization.suppliers.dto;

import com.locadora_rdt_backend.modules.organization.suppliers.constants.SupplierConstants;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Address;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class SupplierInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = SupplierConstants.NAME_MIN_LENGTH, max = SupplierConstants.NAME_MAX_LENGTH,
            message = SupplierConstants.NAME_LENGTH)
    @NotBlank(message = SupplierConstants.FIELD_REQUIRED)
    private String name;

    @Size(min = SupplierConstants.TRADE_NAME_MIN_LENGTH, max = SupplierConstants.TRADE_NAME_MAX_LENGTH,
            message = SupplierConstants.TRADE_NAME_LENGTH)
    @NotBlank(message = SupplierConstants.FIELD_REQUIRED)
    private String tradeName;

    @Size(min = SupplierConstants.COMPANY_NAME_MIN_LENGTH, max = SupplierConstants.COMPANY_NAME_MAX_LENGTH,
            message = SupplierConstants.COMPANY_NAME_LENGTH)
    @NotBlank(message = SupplierConstants.FIELD_REQUIRED)
    private String companyName;

    @NotBlank(message = SupplierConstants.FIELD_REQUIRED)
    @Pattern(regexp = SupplierConstants.CNPJ_PATTERN, message = SupplierConstants.CNPJ_LENGTH)
    private String cnpj;

    @Valid
    @NotNull(message = SupplierConstants.FIELD_REQUIRED)
    private Address address;

    @NotBlank(message = SupplierConstants.FIELD_REQUIRED)
    @Email(message = SupplierConstants.EMAIL_INVALID)
    @Size(max = SupplierConstants.EMAIL_MAX_LENGTH, message = SupplierConstants.EMAIL_MAX_LENGTH_MESSAGE)
    private String email;

    @NotBlank(message = SupplierConstants.FIELD_REQUIRED)
    @Size(max = SupplierConstants.PHONE_MAX_LENGTH, message = SupplierConstants.PHONE_MAX_LENGTH_MESSAGE)
    private String phoneNumber;

    public SupplierInsertDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
