package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivablePaymentDTO;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
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
public class ReceivableCalculationService {

    private final FinancialSettingRepository financialSettingRepository;

    public ReceivableCalculationService(FinancialSettingRepository financialSettingRepository) {
        this.financialSettingRepository = financialSettingRepository;
    }

    public boolean isPartiallyPaid(Receivable receivable) {
        if (Boolean.TRUE.equals(receivable.getPaid())) {
            return false;
        }

        BigDecimal amount = valueOrZero(receivable.getAmount());
        BigDecimal remainingBalance = receivable.getRemainingBalance();

        if (remainingBalance == null || remainingBalance.compareTo(ReceivableConstants.ZERO) <= 0) {
            return false;
        }

        return remainingBalance.compareTo(amount) < 0;
    }

    public BigDecimal getPaymentMethodFee(Receivable receivable, PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return ReceivableConstants.ZERO;
        }

        BigDecimal openAmount = getOpenAmount(receivable);
        return percentageOf(openAmount, paymentMethod.getFee());
    }

    public BigDecimal getCurrentPaymentLimit(Receivable receivable, ReceivablePaymentDTO dto) {
        BigDecimal limit = getOpenAmount(receivable);
        limit = limit.add(valueOrZero(dto.getFee()));
        limit = limit.add(valueOrZero(dto.getLateInterest()));
        limit = limit.add(valueOrZero(dto.getLateFee()));

        return limit;
    }

    public BigDecimal getReceiptAmount(Receivable receivable) {
        BigDecimal subtotal = valueOrZero(receivable.getSubtotal());
        BigDecimal base;

        if (subtotal.compareTo(ReceivableConstants.ZERO) > 0) {
            base = subtotal;
        } else {
            base = valueOrZero(receivable.getAmount());
        }

        BigDecimal total = base;
        total = total.add(valueOrZero(receivable.getFee()));
        total = total.add(valueOrZero(receivable.getLateInterest()));
        total = total.add(valueOrZero(receivable.getLateFee()));
        total = total.subtract(valueOrZero(receivable.getDiscount()));

        return total.setScale(ReceivableConstants.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal getOpenAmount(Receivable receivable) {
        BigDecimal amount = valueOrZero(receivable.getAmount());
        BigDecimal paidAmount;

        if (hasPaymentRecord(receivable)) {
            paidAmount = valueOrZero(receivable.getSubtotal());
        } else {
            paidAmount = ReceivableConstants.ZERO;
        }

        if (amount.compareTo(ReceivableConstants.ZERO) > 0 && paidAmount.compareTo(amount) >= 0) {
            return ReceivableConstants.ZERO;
        }

        if (paidAmount.compareTo(ReceivableConstants.ZERO) > 0 && paidAmount.compareTo(amount) < 0) {
            return amount.subtract(paidAmount);
        }

        BigDecimal remaining = receivable.getRemainingBalance();

        if (remaining != null && remaining.compareTo(ReceivableConstants.ZERO) > 0 && remaining.compareTo(amount) < 0) {
            return remaining;
        }

        return amount;
    }

    public void fillLateCharges(Receivable receivable, ReceivableDTO dto) {
        BigDecimal openAmount = getOpenAmount(receivable).setScale(ReceivableConstants.MONEY_SCALE, RoundingMode.HALF_UP);
        if (Boolean.TRUE.equals(receivable.getPaid())) {
            dto.setCurrentAmountWithLateCharges(getReceiptAmount(receivable));
        } else {
            dto.setCurrentAmountWithLateCharges(openAmount);
        }

        dto.setOverdueDays(0L);
        dto.setCalculatedLateInterest(ReceivableConstants.ZERO.setScale(ReceivableConstants.MONEY_SCALE, RoundingMode.HALF_UP));
        dto.setCalculatedLateFee(ReceivableConstants.ZERO.setScale(ReceivableConstants.MONEY_SCALE, RoundingMode.HALF_UP));

        if (!isOverdueOpenReceivable(receivable)) {
            return;
        }

        long overdueDays = ChronoUnit.DAYS.between(receivable.getDueDate(), LocalDate.now());
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
                .setScale(ReceivableConstants.MONEY_SCALE, RoundingMode.HALF_UP);

        dto.setOverdueDays(overdueDays);
        dto.setCalculatedLateInterest(lateInterest);
        dto.setCalculatedLateFee(lateFee);
        BigDecimal amountWithLateCharges = openAmount.add(lateInterest);
        amountWithLateCharges = amountWithLateCharges.add(lateFee);
        amountWithLateCharges = amountWithLateCharges.setScale(ReceivableConstants.MONEY_SCALE, RoundingMode.HALF_UP);

        dto.setCurrentAmountWithLateCharges(amountWithLateCharges);
    }

    public BigDecimal valueOrZero(BigDecimal value) {
        if (value == null) {
            return ReceivableConstants.ZERO;
        }

        return value;
    }

    public boolean isOverdueOpenReceivable(Receivable receivable) {
        if (Boolean.TRUE.equals(receivable.getPaid())) {
            return false;
        }

        if (Boolean.TRUE.equals(receivable.getCanceled())) {
            return false;
        }

        if (receivable.getDueDate() == null) {
            return false;
        }

        if (!receivable.getDueDate().isBefore(LocalDate.now())) {
            return false;
        }

        return getOpenAmount(receivable).compareTo(ReceivableConstants.ZERO) > 0;
    }

    private BigDecimal percentageOf(BigDecimal amount, BigDecimal percent) {
        BigDecimal value = amount.multiply(valueOrZero(percent));
        return value.divide(
                ReceivableConstants.PERCENT_DIVISOR,
                ReceivableConstants.MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private boolean hasPaymentRecord(Receivable receivable) {
        if (Boolean.TRUE.equals(receivable.getPaid())) {
            return true;
        }

        return receivable.getPaymentDate() != null;
    }

}
