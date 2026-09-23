package com.locadora_rdt_backend.modules.identity.customer_account.dto;

import com.locadora_rdt_backend.modules.identity.customer_account.constants.CustomerAccountConstants;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CustomerAccountRegistrationDTO {

    @NotBlank(message = CustomerAccountConstants.NAME_REQUIRED)
    @Size(max = CustomerAccountConstants.NAME_MAX_LENGTH,
            message = CustomerAccountConstants.NAME_MAX_LENGTH_MESSAGE)
    private String name;

    @NotBlank(message = CustomerAccountConstants.CPF_REQUIRED)
    @Pattern(regexp = CustomerAccountConstants.CPF_PATTERN, message = CustomerAccountConstants.CPF_INVALID)
    private String cpf;

    @NotBlank(message = CustomerAccountConstants.EMAIL_REQUIRED)
    @Email(message = CustomerAccountConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = CustomerAccountConstants.PHONE_REQUIRED)
    @Pattern(regexp = CustomerAccountConstants.PHONE_PATTERN, message = CustomerAccountConstants.PHONE_INVALID)
    private String phone;

    @NotBlank(message = CustomerAccountConstants.STREET_REQUIRED)
    private String street;

    @NotBlank(message = CustomerAccountConstants.NUMBER_REQUIRED)
    private String number;

    private String complement;

    @NotBlank(message = CustomerAccountConstants.NEIGHBORHOOD_REQUIRED)
    private String neighborhood;

    @NotBlank(message = CustomerAccountConstants.CITY_REQUIRED)
    private String city;

    @NotBlank(message = CustomerAccountConstants.STATE_REQUIRED)
    @Size(min = CustomerAccountConstants.STATE_LENGTH, max = CustomerAccountConstants.STATE_LENGTH,
            message = CustomerAccountConstants.STATE_LENGTH_MESSAGE)
    private String state;

    @NotBlank(message = CustomerAccountConstants.ZIP_CODE_REQUIRED)
    private String zipCode;

    public CustomerAccountRegistrationDTO() {
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

}
