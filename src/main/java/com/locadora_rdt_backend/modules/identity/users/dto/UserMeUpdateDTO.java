package com.locadora_rdt_backend.modules.identity.users.dto;

import com.locadora_rdt_backend.modules.identity.users.constants.UserConstants;
import com.locadora_rdt_backend.modules.identity.users.model.Address;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.Valid;

public class UserMeUpdateDTO {

    @NotBlank(message = UserConstants.REQUIRED_FIELD)
    private String name;

    @NotBlank(message = UserConstants.EMAIL_REQUIRED)
    @Email(message = UserConstants.EMAIL_INVALID)
    private String email;

    private String telephone;
    @Valid
    @NotNull(message = UserConstants.REQUIRED_FIELD)
    private Address address;

    public UserMeUpdateDTO() {
    }

    public UserMeUpdateDTO(String name, String email, String telephone, Address address) {
        this.name = name;
        this.email = email;
        this.telephone = telephone;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}

