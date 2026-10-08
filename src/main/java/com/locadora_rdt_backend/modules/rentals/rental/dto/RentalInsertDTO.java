package com.locadora_rdt_backend.modules.rentals.rental.dto;

import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RentalInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = RentalConstants.FIELD_REQUIRED)
    private Long rentalTypeId;

    @NotNull(message = RentalConstants.FIELD_REQUIRED)
    private Instant rentalStartDate;

    @NotNull(message = RentalConstants.FIELD_REQUIRED)
    private Instant returnForecastDate;

    private BigDecimal shippingFee;

    private BigDecimal additionalFee;

    private BigDecimal downPayment;

    @Valid
    private List<RentalItemInsertDTO> items = new ArrayList<>();

    public RentalInsertDTO() {
    }

    public Long getRentalTypeId() {
        return rentalTypeId;
    }

    public void setRentalTypeId(Long rentalTypeId) {
        this.rentalTypeId = rentalTypeId;
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

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getAdditionalFee() {
        return additionalFee;
    }

    public void setAdditionalFee(BigDecimal additionalFee) {
        this.additionalFee = additionalFee;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public List<RentalItemInsertDTO> getItems() {
        return items;
    }

    public void setItems(List<RentalItemInsertDTO> items) {
        this.items = items;
    }
}
