package com.locadora_rdt_backend.modules.payables.service;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableDTO;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableCalculationService;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import com.locadora_rdt_backend.modules.settings.financial_settings.repository.FinancialSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PayableCalculationServiceTests {

    @Mock
    private FinancialSettingRepository financialSettingRepository;

    private PayableCalculationService service;
    private Payable payable;

    @BeforeEach
    void setUp() {
        service = new PayableCalculationService(financialSettingRepository);
        payable = new Payable();
        payable.setAmount(new BigDecimal("100.00"));
        payable.setRemainingBalance(new BigDecimal("100.00"));
        payable.setDueDate(LocalDate.now().minusDays(2));
    }

    @Test
    void lateChargesShouldUseOnlyTheOutstandingBalanceAfterPartialPayment() {
        payable.setSubtotal(new BigDecimal("40.00"));
        payable.setRemainingBalance(new BigDecimal("60.00"));
        payable.setPaymentDate(LocalDate.now().minusDays(1));
        FinancialSetting setting = new FinancialSetting();
        setting.setDefaultLateFeePercent(new BigDecimal("2.00"));
        setting.setDefaultLateInterestPercent(new BigDecimal("1.00"));
        when(financialSettingRepository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.of(setting));
        PayableDTO result = new PayableDTO();

        service.fillLateCharges(payable, result);

        assertEquals(new BigDecimal("60.00"), service.getOpenAmount(payable));
        assertEquals(2L, result.getOverdueDays());
        assertEquals(new BigDecimal("1.20"), result.getCalculatedLateFee());
        assertEquals(new BigDecimal("1.20"), result.getCalculatedLateInterest());
        assertEquals(new BigDecimal("62.40"), result.getCurrentAmountWithLateCharges());
    }

    @Test
    void payableDueTodayOrInTheFutureShouldNotHaveLateCharges() {
        for (int daysAhead = 0; daysAhead <= 1; daysAhead++) {
            payable.setDueDate(LocalDate.now().plusDays(daysAhead));
            PayableDTO result = new PayableDTO();

            service.fillLateCharges(payable, result);

            assertFalse(service.isOverdueOpenPayable(payable));
            assertEquals(0L, result.getOverdueDays());
            assertEquals(new BigDecimal("0.00"), result.getCalculatedLateFee());
            assertEquals(new BigDecimal("0.00"), result.getCalculatedLateInterest());
            assertEquals(new BigDecimal("100.00"), result.getCurrentAmountWithLateCharges());
        }
        verifyNoInteractions(financialSettingRepository);
    }

    @Test
    void paidOrCanceledPayableShouldNotAccrueLateCharges() {
        payable.setPaid(true);
        payable.setSubtotal(new BigDecimal("100.00"));
        payable.setRemainingBalance(BigDecimal.ZERO);
        PayableDTO paidResult = new PayableDTO();

        service.fillLateCharges(payable, paidResult);

        assertEquals(new BigDecimal("100.00"), paidResult.getCurrentAmountWithLateCharges());
        assertEquals(new BigDecimal("0.00"), paidResult.getCalculatedLateFee());
        assertEquals(new BigDecimal("0.00"), paidResult.getCalculatedLateInterest());

        payable.setPaid(false);
        payable.setCanceled(true);
        payable.setSubtotal(BigDecimal.ZERO);
        payable.setRemainingBalance(new BigDecimal("100.00"));
        PayableDTO canceledResult = new PayableDTO();

        service.fillLateCharges(payable, canceledResult);

        assertEquals(new BigDecimal("100.00"), canceledResult.getCurrentAmountWithLateCharges());
        assertEquals(new BigDecimal("0.00"), canceledResult.getCalculatedLateFee());
        assertEquals(new BigDecimal("0.00"), canceledResult.getCalculatedLateInterest());
        verifyNoInteractions(financialSettingRepository);
    }

    @Test
    void discountShouldRecognizeBoletoWithAccentsAndRoundToCents() {
        payable.setAmount(new BigDecimal("20.10"));
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setName(" Boleto Bancário ");

        assertEquals(new BigDecimal("1.01"), service.getDefaultDiscount(payable, paymentMethod));

        paymentMethod.setName("Dinheiro");
        assertEquals(BigDecimal.ZERO, service.getDefaultDiscount(payable, paymentMethod));
        assertEquals(BigDecimal.ZERO, service.getDefaultDiscount(payable, null));
    }

    @Test
    void missingFinancialSettingsShouldKeepLateChargesAtZero() {
        when(financialSettingRepository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.empty());
        PayableDTO result = new PayableDTO();

        service.fillLateCharges(payable, result);

        assertEquals(2L, result.getOverdueDays());
        assertEquals(new BigDecimal("0.00"), result.getCalculatedLateFee());
        assertEquals(new BigDecimal("0.00"), result.getCalculatedLateInterest());
        assertEquals(new BigDecimal("100.00"), result.getCurrentAmountWithLateCharges());
    }
}
