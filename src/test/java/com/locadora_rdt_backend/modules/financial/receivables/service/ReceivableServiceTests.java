package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFilterDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableInsertDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivablePaymentDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableReportDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableUpdateDTO;
import com.locadora_rdt_backend.modules.financial.receivables.mapper.ReceivableMapper;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.locadora_rdt_backend.modules.financial.receivables.repository.ReceivableRepository;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableCalculationService;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableDocumentPdfService;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableFilterService;
import com.locadora_rdt_backend.modules.financial.receivables.service.ReceivableServiceImpl;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.repository.PaymentFrequencyRepository;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.financial.payment_methods.repository.PaymentMethodRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import com.locadora_rdt_backend.modules.settings.financial_settings.repository.FinancialSettingRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReceivableServiceTests {

    @Mock
    private ReceivableRepository repository;

    @Mock
    private ReceivableMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentFrequencyRepository paymentFrequencyRepository;

    @Mock
    private FinancialSettingRepository financialSettingRepository;

    private ReceivableServiceImpl service;

    private Receivable receivable;
    private ReceivableDTO receivableDTO;
    private User user;

    @BeforeEach
    void setUp() {
        ReceivableCalculationService calculationService = new ReceivableCalculationService(financialSettingRepository);
        ReceivableFilterService filterService = new ReceivableFilterService();
        ReceivableDocumentPdfService documentPdfService = new ReceivableDocumentPdfService(calculationService);
        service = new ReceivableServiceImpl(
                repository, mapper, authenticationFacade, userRepository,
                customerRepository, paymentMethodRepository, paymentFrequencyRepository,
                calculationService, filterService, documentPdfService
        );

        receivable = new Receivable();
        receivable.setId(1L);
        receivable.setDescription("Aluguel");
        receivable.setAmount(new BigDecimal("100.00"));
        receivable.setRemainingBalance(new BigDecimal("100.00"));
        receivable.setDueDate(LocalDate.now().plusMonths(2));
        receivable.setPaid(false);
        receivable.setCanceled(false);

        receivableDTO = new ReceivableDTO();
        receivableDTO.setId(1L);
        receivableDTO.setDescription("Aluguel");

        user = new User();
        user.setId(9L);
        user.setName("Usuário Teste");
        user.setEmail("usuario@email.com");
    }

    @Test
    void findAllPagedByDescriptionShouldReturnPageOfReceivables() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Receivable> receivables = new PageImpl<>(Collections.singletonList(receivable));

        when(repository.findWithFilters(
                eq("Aluguel"), eq(ReceivableConstants.FILTER_DATE_DISABLED), eq(ReceivableConstants.FILTER_DATE_DISABLED),
                eq(false), eq(false), eq("ALL"), eq("DUE_DATE"),
                eq(-1L), eq(-1L), eq(-1L),
                eq(ReceivableConstants.FILTER_AMOUNT_DISABLED), eq(ReceivableConstants.FILTER_AMOUNT_DISABLED),
                eq("dueDate"), eq("ASC"), eq(pageRequest)
        )).thenReturn(receivables);
        when(mapper.toDTO(receivable)).thenReturn(receivableDTO);

        Page<ReceivableDTO> resultado = service.findAllPaged(" Aluguel ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Aluguel", resultado.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), resultado.getContent().get(0).getCurrentAmountWithLateCharges());
    }

    @Test
    void findAllPagedByDescriptionShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(pageRequest)
        )).thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Aluguel", pageRequest));
    }

    @Test
    void findAllPagedWithFiltersShouldReturnPageOfReceivables() {
        PageRequest pageRequest = PageRequest.of(1, 5);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        ReceivableFilterDTO filters = new ReceivableFilterDTO();
        filters.setSearch(" Aluguel ");
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setStatus(" open ");
        filters.setPeriodType(" payment ");
        filters.setCustomerId(2L);
        filters.setPaymentMethodId(4L);
        filters.setPaymentFrequencyId(5L);
        filters.setMinimumAmount(new BigDecimal("10.00"));
        filters.setMaximumAmount(new BigDecimal("150.00"));
        filters.setOrderBy("amount");
        filters.setDirection("desc");
        Page<Receivable> receivables = new PageImpl<>(Collections.singletonList(receivable), pageRequest, 6);

        when(repository.findWithFilters(
                eq("Aluguel"), eq(startDate), eq(endDate), eq(true), eq(true), eq("PENDING"), eq("PAYMENT_DATE"),
                eq(2L), eq(4L), eq(5L), eq(new BigDecimal("10.00")), eq(new BigDecimal("150.00")),
                eq("amount"), eq("DESC"), eq(pageRequest)
        )).thenReturn(receivables);
        when(mapper.toDTO(receivable)).thenReturn(receivableDTO);

        Page<ReceivableDTO> resultado = service.findAllPaged(filters, pageRequest);

        assertEquals(6, resultado.getTotalElements());
        assertEquals(1, resultado.getNumber());
        assertEquals(1L, resultado.getContent().get(0).getId());
        assertEquals(" open ", filters.getStatus());
    }

    @Test
    void findAllPagedWithFiltersShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        ReceivableFilterDTO filters = new ReceivableFilterDTO();
        when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(pageRequest)
        )).thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged(filters, pageRequest));
    }

    @Test
    void findByIdShouldReturnReceivableWithLateCharges() {
        receivable.setDueDate(LocalDate.now().minusDays(2));
        receivable.setCreatedBy(user);
        FinancialSetting setting = new FinancialSetting();
        setting.setDefaultLateFeePercent(new BigDecimal("2.00"));
        setting.setDefaultLateInterestPercent(new BigDecimal("1.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(mapper.toDTO(receivable)).thenCallRealMethod();
        when(financialSettingRepository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.of(setting));

        ReceivableDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(9L, resultado.getCreatedById());
        assertEquals("Usuário Teste", resultado.getCreatedByName());
        assertEquals(2L, resultado.getOverdueDays());
        assertEquals(new BigDecimal("2.00"), resultado.getCalculatedLateFee());
        assertEquals(new BigDecimal("2.00"), resultado.getCalculatedLateInterest());
        assertEquals(new BigDecimal("104.00"), resultado.getCurrentAmountWithLateCharges());
    }

    @Test
    void findByIdShouldThrowExceptionWhenReceivableDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSavePaidReceivableWithRelationsAndAudit() {
        ReceivableInsertDTO insertDTO = new ReceivableInsertDTO();
        insertDTO.setCustomerId(2L);
        insertDTO.setPaymentMethodId(4L);
        insertDTO.setPaymentFrequencyId(5L);
        receivable.setPaid(true);
        receivable.setPaymentDate(LocalDate.now());
        Customer customer = new Customer();
        customer.setId(2L);
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(4L);
        paymentMethod.setFee(new BigDecimal("3.00"));
        PaymentFrequency paymentFrequency = new PaymentFrequency();
        paymentFrequency.setId(5L);

        when(mapper.toEntity(insertDTO)).thenReturn(receivable);
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(paymentMethod));
        when(paymentFrequencyRepository.findById(5L)).thenReturn(Optional.of(paymentFrequency));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenReturn(receivableDTO);

        ReceivableDTO resultado = service.insert(insertDTO);

        assertEquals(receivableDTO, resultado);
        assertEquals(customer, receivable.getCustomer());
        assertEquals(paymentMethod, receivable.getPaymentMethod());
        assertEquals(paymentFrequency, receivable.getPaymentFrequency());
        assertEquals(user, receivable.getCreatedBy());
        assertEquals(user, receivable.getPaidBy());
        assertEquals(new BigDecimal("100.00"), receivable.getSubtotal());
        assertEquals(BigDecimal.ZERO, receivable.getRemainingBalance());
        verify(repository).save(receivable);
    }

    @Test
    void insertShouldThrowExceptionWhenCustomerDoesNotExist() {
        ReceivableInsertDTO insertDTO = new ReceivableInsertDTO();
        insertDTO.setCustomerId(2L);
        when(mapper.toEntity(insertDTO)).thenReturn(receivable);
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.insert(insertDTO));

        verify(repository, never()).save(any());
    }

    @Test
    void updateShouldPreservePartialPaymentAndSetAudit() {
        LocalDate previousPaymentDate = LocalDate.now().minusDays(1);
        receivable.setSubtotal(new BigDecimal("40.00"));
        receivable.setRemainingBalance(new BigDecimal("60.00"));
        receivable.setPaymentDate(previousPaymentDate);
        ReceivableUpdateDTO updateDTO = new ReceivableUpdateDTO();
        updateDTO.setDescription("Aluguel atualizado");
        updateDTO.setAmount(new BigDecimal("120.00"));
        updateDTO.setDueDate(receivable.getDueDate());

        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        doCallRealMethod().when(mapper).updateEntity(receivable, updateDTO);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenReturn(receivableDTO);

        ReceivableDTO resultado = service.update(1L, updateDTO);

        assertEquals(receivableDTO, resultado);
        assertEquals("Aluguel atualizado", receivable.getDescription());
        assertEquals(new BigDecimal("120.00"), receivable.getAmount());
        assertEquals(new BigDecimal("60.00"), receivable.getRemainingBalance());
        assertEquals(new BigDecimal("40.00"), receivable.getSubtotal());
        assertEquals(previousPaymentDate, receivable.getPaymentDate());
        assertFalse(receivable.getPaid());
        assertEquals(user, receivable.getUpdatedBy());
        verify(mapper).updateEntity(receivable, updateDTO);
    }

    @Test
    void updateShouldThrowExceptionWhenReceivableIsPaid() {
        receivable.setPaid(true);
        ReceivableUpdateDTO updateDTO = new ReceivableUpdateDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, updateDTO));

        assertEquals(ReceivableConstants.PAID_RECEIVABLE_CANNOT_BE_UPDATED, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteShouldDeleteReceivable() {
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        service.delete(1L);

        verify(repository).delete(receivable);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenIntegrityFails() {
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        doThrow(new DataIntegrityViolationException("Conta com vínculos")).when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> service.delete(1L));

        assertEquals(ReceivableConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
    }

    @Test
    void payShouldSavePartialPaymentWithBalanceAndCharges() {
        receivable.setDueDate(LocalDate.now().minusDays(2));
        LocalDate paymentDate = LocalDate.now().minusDays(1);
        ReceivablePaymentDTO paymentDTO = new ReceivablePaymentDTO();
        paymentDTO.setPaymentAmount(new BigDecimal("40.00"));
        paymentDTO.setPaymentDate(paymentDate);
        paymentDTO.setPaymentMethodId(4L);
        paymentDTO.setFee(new BigDecimal("3.00"));
        paymentDTO.setLateInterest(new BigDecimal("2.00"));
        paymentDTO.setLateFee(new BigDecimal("4.00"));
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(4L);
        paymentMethod.setFee(new BigDecimal("3.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(paymentMethod));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenReturn(receivableDTO);

        ReceivableDTO resultado = service.pay(1L, paymentDTO);

        assertEquals(receivableDTO, resultado);
        assertFalse(receivable.getPaid());
        assertEquals(new BigDecimal("40.00"), receivable.getSubtotal());
        assertEquals(new BigDecimal("60.00"), receivable.getRemainingBalance());
        assertEquals(paymentDate, receivable.getPaymentDate());
        assertEquals(paymentMethod, receivable.getPaymentMethod());
        assertEquals(new BigDecimal("3.00"), receivable.getFee());
        assertEquals(new BigDecimal("2.00"), receivable.getLateInterest());
        assertEquals(new BigDecimal("4.00"), receivable.getLateFee());
        assertEquals(BigDecimal.ZERO, receivable.getDiscount());
        assertEquals(user, receivable.getUpdatedBy());
        assertNull(receivable.getPaidBy());
        verify(repository).save(receivable);
    }

    @Test
    void payShouldThrowExceptionWhenPaymentExceedsLimit() {
        ReceivablePaymentDTO paymentDTO = new ReceivablePaymentDTO();
        paymentDTO.setPaymentAmount(new BigDecimal("101.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.pay(1L, paymentDTO));

        assertEquals(ReceivableConstants.PAYMENT_AMOUNT_EXCEEDS_RECEIVABLE, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void reportShouldReturnTotalsForFilteredReceivables() {
        Receivable paidReceivable = new Receivable();
        paidReceivable.setId(2L);
        paidReceivable.setAmount(new BigDecimal("20.00"));
        paidReceivable.setPaid(true);
        Page<Receivable> receivables = new PageImpl<>(Arrays.asList(receivable, paidReceivable));

        when(repository.findWithFilters(
                eq("Aluguel"), eq(ReceivableConstants.FILTER_DATE_DISABLED), eq(ReceivableConstants.FILTER_DATE_DISABLED),
                eq(false), eq(false), eq("ALL"), eq("DUE_DATE"), eq(-1L), eq(-1L), eq(-1L),
                eq(ReceivableConstants.FILTER_AMOUNT_DISABLED), eq(ReceivableConstants.FILTER_AMOUNT_DISABLED),
                eq("dueDate"), eq("ASC"), eq(Pageable.unpaged())
        )).thenReturn(receivables);

        ReceivableReportDTO resultado = service.report("Aluguel", null, null, "ALL", "DUE");

        assertEquals(2L, resultado.getTotalItems());
        assertEquals(new BigDecimal("120.00"), resultado.getTotalAmount());
        assertEquals(new BigDecimal("20.00"), resultado.getPaidAmount());
        assertEquals(new BigDecimal("100.00"), resultado.getOpenAmount());
    }

    @Test
    void reportShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), eq(Pageable.unpaged())
        )).thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.report("Aluguel", null, null, "ALL", "DUE"));
    }

    @Test
    void receiptShouldReturnPdfForPaidReceivable() throws Exception {
        receivable.setPaid(true);
        receivable.setPaymentDate(LocalDate.of(2026, 1, 15));
        receivable.setSubtotal(new BigDecimal("100.00"));
        receivable.setFee(new BigDecimal("3.00"));
        receivable.setDiscount(new BigDecimal("1.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        byte[] resultado = service.receipt(1L);

        PdfReader reader = new PdfReader(resultado);
        String text = new PdfTextExtractor(reader).getTextFromPage(1);

        assertEquals(1, reader.getNumberOfPages());
        assertTrue(text.contains("RECIBO DE PAGAMENTO"));
        assertTrue(text.contains("2026/1"));
        assertTrue(text.contains("15/01/2026"));
        assertTrue(text.contains("102,00"));
        assertTrue(text.contains("Aluguel"));
        reader.close();
    }

    @Test
    void receiptShouldThrowExceptionWhenReceivableIsNotPaid() {
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.receipt(1L));

        assertEquals(ReceivableConstants.RECEIPT_ONLY_FOR_PAID_RECEIVABLE, exception.getMessage());
    }

    @Test
    void fiscalCouponShouldReturnPdfForPaidReceivable() throws Exception {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setName("Cliente Teste");
        customer.setCpf("12345678900");
        receivable.setCustomer(customer);
        receivable.setPaid(true);
        receivable.setPaidBy(user);
        receivable.setPaymentDate(LocalDate.of(2026, 1, 15));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        byte[] resultado = service.fiscalCoupon(1L);

        PdfReader reader = new PdfReader(resultado);
        String text = new PdfTextExtractor(reader).getTextFromPage(1);

        assertEquals(1, reader.getNumberOfPages());
        assertTrue(text.contains("CUPOM FISCAL"));
        assertTrue(text.contains("2026/1"));
        assertTrue(text.contains("Cliente Teste"));
        assertTrue(text.contains("12345678900"));
        assertTrue(text.contains("100,00"));
        assertTrue(text.contains("Usuário Teste"));
        reader.close();
    }

    @Test
    void fiscalCouponShouldThrowExceptionWhenReceivableIsNotPaid() {
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.fiscalCoupon(1L));

        assertEquals(ReceivableConstants.FISCAL_COUPON_ONLY_FOR_PAID_RECEIVABLE, exception.getMessage());
    }
    @Test
    void fullPaymentShouldReturnCurrentAndPaidAmountsIncludingCharges() {
        receivable.setDueDate(LocalDate.now().minusDays(2));
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("109.00"));
        payment.setFee(new BigDecimal("999.00"));
        payment.setPaymentMethodId(4L);
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(4L);
        paymentMethod.setFee(new BigDecimal("3.00"));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(paymentMethod));
        payment.setLateInterest(new BigDecimal("2.00"));
        payment.setLateFee(new BigDecimal("4.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.pay(1L, payment);

        assertTrue(result.getPaid());
        assertEquals(new BigDecimal("3.00"), result.getFee());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(new BigDecimal("109.00"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void findByIdShouldPreserveDiscountOnPreviouslySettledAccount() {
        receivable.setPaid(true);
        receivable.setSubtotal(new BigDecimal("100.00"));
        receivable.setRemainingBalance(BigDecimal.ZERO);
        receivable.setDiscount(new BigDecimal("5.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.findById(1L);

        assertEquals(new BigDecimal("95.00"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
        assertEquals(new BigDecimal("100.00"), result.getOriginalAmount());
        assertEquals(new BigDecimal("5.00"), result.getDiscount());
    }

    @Test
    void settlementAfterPartialPaymentShouldIncludePreviousPaymentAndFinalCharges() {
        receivable.setDueDate(LocalDate.now().minusDays(2));
        receivable.setPaymentDate(LocalDate.now().minusDays(1));
        receivable.setSubtotal(new BigDecimal("40.00"));
        receivable.setRemainingBalance(new BigDecimal("60.00"));
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("69.00"));
        payment.setLateInterest(new BigDecimal("4.00"));
        payment.setLateFee(new BigDecimal("5.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.pay(1L, payment);

        assertTrue(result.getPaid());
        assertEquals(new BigDecimal("109.00"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Pix", "Boleto Bancário"})
    void payShouldSettlePixAndBoletoWithoutDiscount(String methodName) {
        PaymentMethod method = new PaymentMethod();
        method.setId(1L);
        method.setName(methodName);
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("100.00"));
        payment.setPaymentMethodId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(method));
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.pay(1L, payment);

        assertTrue(result.getPaid());
        assertEquals(BigDecimal.ZERO, result.getDiscount());
        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
        assertEquals(method, receivable.getPaymentMethod());
    }


    @Test
    void payShouldIncludeRegisteredMethodFeeBeforeDueDate() {
        PaymentMethod method = new PaymentMethod();
        method.setId(4L);
        method.setFee(new BigDecimal("3.00"));
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentMethodId(4L);
        payment.setPaymentAmount(new BigDecimal("103.00"));
        payment.setFee(BigDecimal.ZERO);
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(method));
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.pay(1L, payment);
        method.setFee(new BigDecimal("10.00"));
        ReceivableDTO savedResult = service.findById(1L);

        assertTrue(result.getPaid());
        assertEquals(new BigDecimal("3.00"), result.getFee());
        assertEquals(new BigDecimal("103.00"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
        assertEquals(new BigDecimal("3.00"), savedResult.getFee());
        assertEquals(new BigDecimal("103.00"), savedResult.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getDiscount());
    }

    @Test
    void payShouldCalculateMethodFeeOnOutstandingBalanceAfterPartialPayment() {
        receivable.setPaymentDate(LocalDate.now().minusDays(1));
        receivable.setSubtotal(new BigDecimal("40.00"));
        receivable.setRemainingBalance(new BigDecimal("60.00"));
        PaymentMethod method = new PaymentMethod();
        method.setFee(new BigDecimal("5.00"));
        receivable.setPaymentMethod(method);
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("63.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.pay(1L, payment);

        assertTrue(result.getPaid());
        assertEquals(new BigDecimal("3.00"), result.getFee());
        assertEquals(new BigDecimal("103.00"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void payShouldRoundMethodFeeToCents() {
        receivable.setAmount(new BigDecimal("20.10"));
        receivable.setRemainingBalance(new BigDecimal("20.10"));
        PaymentMethod method = new PaymentMethod();
        method.setId(4L);
        method.setFee(new BigDecimal("5.00"));
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentMethodId(4L);
        payment.setPaymentAmount(new BigDecimal("21.11"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(method));
        when(repository.save(receivable)).thenReturn(receivable);
        when(mapper.toDTO(receivable)).thenCallRealMethod();

        ReceivableDTO result = service.pay(1L, payment);

        assertTrue(result.getPaid());
        assertEquals(new BigDecimal("1.01"), result.getFee());
        assertEquals(new BigDecimal("21.11"), result.getSubtotal());
        assertEquals(result.getSubtotal(), result.getCurrentAmountWithLateCharges());
    }

    @Test
    void payShouldRejectAnAmountAboveTheRegisteredMethodFee() {
        PaymentMethod method = new PaymentMethod();
        method.setId(4L);
        method.setFee(new BigDecimal("3.00"));
        ReceivablePaymentDTO payment = new ReceivablePaymentDTO();
        payment.setPaymentMethodId(4L);
        payment.setPaymentAmount(new BigDecimal("104.00"));
        payment.setFee(new BigDecimal("999.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(receivable));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(method));

        assertThrows(IllegalArgumentException.class, () -> service.pay(1L, payment));

        verify(repository, never()).save(any());
    }

}
