package com.locadora_rdt_backend.modules.rentals.rental_history.dto;

import com.locadora_rdt_backend.modules.rentals.rental.dto.RentalItemDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RentalHistoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String rentalNumber;
    private String rentalTypeName;
    private String status;
    private Instant registrationDate;
    private Instant rentalStartDate;
    private Instant returnForecastDate;
    private Instant effectiveReturnDate;
    private BigDecimal totalAmount;
    private Boolean paid;
    private List<RentalItemDTO> items = new ArrayList<>();

    public RentalHistoryDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRentalNumber() {
        return rentalNumber;
    }

    public void setRentalNumber(String rentalNumber) {
        this.rentalNumber = rentalNumber;
    }

    public String getRentalTypeName() {
        return rentalTypeName;
    }

    public void setRentalTypeName(String rentalTypeName) {
        this.rentalTypeName = rentalTypeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Instant registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Instant getRentalStartDate() {
        return rentalStartDate;
    }

    public void setRentalStartDate(Instant rentalStartDate) {
        this.rentalStartDate = rentalStartDate;
    }

    public Instant getReturnForecastDate() {
        return returnForecastDate;
    }

    public void setReturnForecastDate(Instant returnForecastDate) {
        this.returnForecastDate = returnForecastDate;
    }

    public Instant getEffectiveReturnDate() {
        return effectiveReturnDate;
    }

    public void setEffectiveReturnDate(Instant effectiveReturnDate) {
        this.effectiveReturnDate = effectiveReturnDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public List<RentalItemDTO> getItems() {
        return items;
    }

    public void setItems(List<RentalItemDTO> items) {
        this.items = items;
    }
}
