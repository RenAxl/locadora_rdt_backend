package com.locadora_rdt_backend.modules.settings.financial_settings.dto;

import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

public class FinancialSettingUpdateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = FinancialSettingConstants.LATE_FEE_PERCENT_REQUIRED)
    @DecimalMin(value = FinancialSettingConstants.MINIMUM_PERCENTAGE,
            message = FinancialSettingConstants.LATE_FEE_PERCENT_MINIMUM)
    @Digits(integer = FinancialSettingConstants.PERCENTAGE_MAX_INTEGER_DIGITS,
            fraction = FinancialSettingConstants.PERCENTAGE_MAX_FRACTION_DIGITS,
            message = FinancialSettingConstants.LATE_FEE_PERCENT_INVALID)
    private BigDecimal defaultLateFeePercent;

    @NotNull(message = FinancialSettingConstants.LATE_INTEREST_PERCENT_REQUIRED)
    @DecimalMin(value = FinancialSettingConstants.MINIMUM_PERCENTAGE,
            message = FinancialSettingConstants.LATE_INTEREST_PERCENT_MINIMUM)
    @Digits(integer = FinancialSettingConstants.PERCENTAGE_MAX_INTEGER_DIGITS,
            fraction = FinancialSettingConstants.PERCENTAGE_MAX_FRACTION_DIGITS,
            message = FinancialSettingConstants.LATE_INTEREST_PERCENT_INVALID)
    private BigDecimal defaultLateInterestPercent;

    public FinancialSettingUpdateDTO() {
    }

    public BigDecimal getDefaultLateFeePercent() {
        return defaultLateFeePercent;
    }

    public void setDefaultLateFeePercent(BigDecimal defaultLateFeePercent) {
        this.defaultLateFeePercent = defaultLateFeePercent;
    }

    public BigDecimal getDefaultLateInterestPercent() {
        return defaultLateInterestPercent;
    }

    public void setDefaultLateInterestPercent(BigDecimal defaultLateInterestPercent) {
        this.defaultLateInterestPercent = defaultLateInterestPercent;
    }
}
