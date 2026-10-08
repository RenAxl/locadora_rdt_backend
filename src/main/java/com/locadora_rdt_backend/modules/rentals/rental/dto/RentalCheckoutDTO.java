package com.locadora_rdt_backend.modules.rentals.rental.dto;

import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class RentalCheckoutDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = RentalConstants.FIELD_REQUIRED)
    private Long paymentMethodId;

    public RentalCheckoutDTO() {
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
