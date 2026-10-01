package com.locadora_rdt_backend.modules.financial.payment_methods.dto;

import com.locadora_rdt_backend.modules.financial.payment_methods.constants.PaymentMethodConstants;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentMethodUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(min = PaymentMethodConstants.NAME_MIN_LENGTH, max = PaymentMethodConstants.NAME_MAX_LENGTH,
            message = PaymentMethodConstants.NAME_LENGTH)
    @NotBlank(message = PaymentMethodConstants.NAME_REQUIRED)
    private String name;

    @DecimalMin(value = PaymentMethodConstants.MINIMUM_FEE, message = PaymentMethodConstants.FEE_MINIMUM)
    @Digits(integer = PaymentMethodConstants.FEE_MAX_INTEGER_DIGITS,
            fraction = PaymentMethodConstants.FEE_MAX_FRACTION_DIGITS,
            message = PaymentMethodConstants.FEE_INVALID)
    private BigDecimal fee;

    public PaymentMethodUpdateDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }
}
