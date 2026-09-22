package com.locadora_rdt_backend.modules.organization.customers.dto;

import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import com.locadora_rdt_backend.modules.organization.customers.validation.CustomerInsertValid;
import com.locadora_rdt_backend.modules.organization.customers.model.Address;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@CustomerInsertValid
public class CustomerInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = CustomerConstants.NAME_MIN_LENGTH, max = CustomerConstants.NAME_MAX_LENGTH,
            message = CustomerConstants.NAME_LENGTH)
    @NotBlank(message = CustomerConstants.NAME_REQUIRED)
    private String name;

    @NotBlank(message = CustomerConstants.CPF_REQUIRED)
    private String cpf;

    @Email(message = CustomerConstants.EMAIL_INVALID)
    @Size(max = CustomerConstants.EMAIL_MAX_LENGTH, message = CustomerConstants.EMAIL_MAX_LENGTH_MESSAGE)
    private String email;

    @Size(max = CustomerConstants.PHONE_MAX_LENGTH, message = CustomerConstants.PHONE_MAX_LENGTH_MESSAGE)
    private String phone;

    @Valid
    @NotNull(message = CustomerConstants.FIELD_REQUIRED)
    private Address address;

    private Boolean active;

    public CustomerInsertDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
