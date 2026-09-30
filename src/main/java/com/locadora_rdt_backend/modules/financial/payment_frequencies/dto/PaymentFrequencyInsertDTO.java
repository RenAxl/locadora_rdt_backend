package com.locadora_rdt_backend.modules.financial.payment_frequencies.dto;

import com.locadora_rdt_backend.modules.financial.payment_frequencies.constants.PaymentFrequencyConstants;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

public class PaymentFrequencyInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = PaymentFrequencyConstants.FREQUENCY_MIN_LENGTH, max = PaymentFrequencyConstants.FREQUENCY_MAX_LENGTH,
            message = PaymentFrequencyConstants.FREQUENCY_LENGTH)
    @NotBlank(message = PaymentFrequencyConstants.FREQUENCY_REQUIRED)
    private String frequency;

    @NotNull(message = PaymentFrequencyConstants.DAYS_REQUIRED)
    @Min(value = PaymentFrequencyConstants.MINIMUM_DAYS, message = PaymentFrequencyConstants.DAYS_MINIMUM)
    private Integer days;

    public PaymentFrequencyInsertDTO() {
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}
