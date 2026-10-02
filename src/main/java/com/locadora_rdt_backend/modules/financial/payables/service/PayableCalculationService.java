package com.locadora_rdt_backend.modules.financial.payables.service;

import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayablePaymentDTO;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import com.locadora_rdt_backend.modules.settings.financial_settings.repository.FinancialSettingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class PayableCalculationService {

    private final FinancialSettingRepository financialSettingRepository;

    public PayableCalculationService(FinancialSettingRepository financialSettingRepository) {
        this.financialSettingRepository = financialSettingRepository;
    }

    public boolean isPartiallyPaid(Payable payable) {
        if (Boolean.TRUE.equals(payable.getPaid())) {
            return false;
        }

        BigDecimal amount = valueOrZero(payable.getAmount());
        BigDecimal remainingBalance = payable.getRemainingBalance();

        if (remainingBalance == null || remainingBalance.compareTo(PayableConstants.ZERO) <= 0) {
            return false;
        }

        return remainingBalance.compareTo(amount) < 0;
    }

    public BigDecimal getPaymentMethodFee(Payable payable, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return PayableConstants.ZERO;
        }

        BigDecimal openAmount = getOpenAmount(payable);
        return percentageOf(openAmount, paymentMethod.getFee());
    }

    public BigDecimal getCurrentPaymentLimit(Payable payable, PayablePaymentDTO dto) {
        BigDecimal limit = getOpenAmount(payable);
        limit = limit.add(valueOrZero(dto.getFee()));
        limit = limit.add(valueOrZero(dto.getLateInterest()));
        limit = limit.add(valueOrZero(dto.getLateFee()));

        return limit;
    }

    public BigDecimal getOpenAmount(Payable payable) {
        BigDecimal amount = valueOrZero(payable.getAmount());
        BigDecimal paidAmount;

        if (hasPaymentRecord(payable)) {
            paidAmount = valueOrZero(payable.getSubtotal());
        } else {
            paidAmount = PayableConstants.ZERO;
        }

        if (amount.compareTo(PayableConstants.ZERO) > 0 && paidAmount.compareTo(amount) >= 0) {
            return PayableConstants.ZERO;
        }

        if (paidAmount.compareTo(PayableConstants.ZERO) > 0 && paidAmount.compareTo(amount) < 0) {
            return amount.subtract(paidAmount);
        }

        BigDecimal remaining = payable.getRemainingBalance();

        if (remaining != null && remaining.compareTo(PayableConstants.ZERO) > 0 && remaining.compareTo(amount) < 0) {
            return remaining;
        }

        return amount;
    }

    public BigDecimal getPaidAmount(Payable payable) {
        BigDecimal subtotal = valueOrZero(payable.getSubtotal());
        BigDecimal total = valueOrZero(payable.getAmount());

        if (subtotal.compareTo(PayableConstants.ZERO) > 0) {
            total = subtotal;
        }

        total = total.add(valueOrZero(payable.getFee()));
        total = total.add(valueOrZero(payable.getLateInterest()));
        total = total.add(valueOrZero(payable.getLateFee()));
        total = total.subtract(valueOrZero(payable.getDiscount()));

        return total.setScale(PayableConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public void fillLateCharges(Payable payable, PayableDTO dto) {
        BigDecimal openAmount = getOpenAmount(payable).setScale(PayableConstants.MONEY_SCALE, RoundingMode.HALF_UP);
        if (Boolean.TRUE.equals(payable.getPaid())) {
            dto.setCurrentAmountWithLateCharges(getPaidAmount(payable));
        } else {
            dto.setCurrentAmountWithLateCharges(openAmount);
        }

        dto.setOverdueDays(0L);
        dto.setCalculatedLateInterest(PayableConstants.ZERO.setScale(PayableConstants.MONEY_SCALE, RoundingMode.HALF_UP));
        dto.setCalculatedLateFee(PayableConstants.ZERO.setScale(PayableConstants.MONEY_SCALE, RoundingMode.HALF_UP));

        if (!isOverdueOpenPayable(payable)) {
            return;
        }

        long overdueDays = ChronoUnit.DAYS.between(payable.getDueDate(), LocalDate.now());
        Optional<FinancialSetting> settingOptional = financialSettingRepository.findBySingletonKey(
                FinancialSettingConstants.DEFAULT_SINGLETON_KEY
        );
        FinancialSetting setting = new FinancialSetting();

        if (settingOptional.isPresent()) {
            setting = settingOptional.get();
        }

        BigDecimal lateFee = percentageOf(openAmount, setting.getDefaultLateFeePercent());
        BigDecimal lateInterest = percentageOf(openAmount, setting.getDefaultLateInterestPercent())
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(PayableConstants.MONEY_SCALE, RoundingMode.HALF_UP);

        dto.setOverdueDays(overdueDays);
        dto.setCalculatedLateInterest(lateInterest);
        dto.setCalculatedLateFee(lateFee);
        BigDecimal amountWithLateCharges = openAmount.add(lateInterest);
        amountWithLateCharges = amountWithLateCharges.add(lateFee);
        amountWithLateCharges = amountWithLateCharges.setScale(PayableConstants.MONEY_SCALE, RoundingMode.HALF_UP);

        dto.setCurrentAmountWithLateCharges(amountWithLateCharges);
    }

    public BigDecimal valueOrZero(BigDecimal value) {
        if (value == null) {
            return PayableConstants.ZERO;
        }

        return value;
    }

    public boolean isOverdueOpenPayable(Payable payable) {
        if (Boolean.TRUE.equals(payable.getPaid())) {
            return false;
        }

        if (Boolean.TRUE.equals(payable.getCanceled())) {
            return false;
        }

        if (payable.getDueDate() == null) {
            return false;
        }

        if (!payable.getDueDate().isBefore(LocalDate.now())) {
            return false;
        }

        return getOpenAmount(payable).compareTo(PayableConstants.ZERO) > 0;
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percent) {
        BigDecimal value = amount.multiply(valueOrZero(percent));
        return value.divide(
                PayableConstants.PERCENT_DIVISOR,
                PayableConstants.MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private boolean hasPaymentRecord(Payable payable) {
        if (Boolean.TRUE.equals(payable.getPaid())) {
            return true;
        }

        return payable.getPaymentDate() != null;
    }

}
