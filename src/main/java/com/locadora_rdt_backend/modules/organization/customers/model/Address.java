package com.locadora_rdt_backend.modules.organization.customers.model;

import com.locadora_rdt_backend.modules.organization.customers.constants.CustomerConstants;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Embeddable
public class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = CustomerConstants.FIELD_REQUIRED)
    @Column(name = "street", length = 100)
    private String street;

    @NotBlank(message = CustomerConstants.FIELD_REQUIRED)
    @Column(name = "number", length = 20)
    private String number;

    @Column(name = "complement", length = 100)
    private String complement;

    @NotBlank(message = CustomerConstants.FIELD_REQUIRED)
    @Column(name = "neighborhood", length = 80)
    private String neighborhood;

    @NotBlank(message = CustomerConstants.FIELD_REQUIRED)
    @Column(name = "city", length = 80)
    private String city;

    @NotBlank(message = CustomerConstants.FIELD_REQUIRED)
    @Column(name = "state", length = 2)
    private String state;

    @NotBlank(message = CustomerConstants.FIELD_REQUIRED)
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    public Address() {
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
