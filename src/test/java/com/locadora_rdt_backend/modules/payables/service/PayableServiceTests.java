package com.locadora_rdt_backend.modules.payables.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFilterDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableInsertDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayablePaymentDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableReportDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableFilterService;
import com.locadora_rdt_backend.modules.financial.payables.mapper.PayableMapper;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.payables.repository.PayableRepository;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableCalculationService;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableServiceImpl;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.repository.PaymentFrequencyRepository;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.financial.payment_methods.repository.PaymentMethodRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.organization.employees.model.Employee;
import com.locadora_rdt_backend.modules.organization.employees.repository.EmployeeRepository;
import com.locadora_rdt_backend.modules.organization.suppliers.model.Supplier;
import com.locadora_rdt_backend.modules.organization.suppliers.repository.SupplierRepository;
import com.locadora_rdt_backend.modules.settings.financial_settings.constants.FinancialSettingConstants;
import com.locadora_rdt_backend.modules.settings.financial_settings.model.FinancialSetting;
import com.locadora_rdt_backend.modules.settings.financial_settings.repository.FinancialSettingRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class PayableServiceTests {

    @Mock
    private PayableRepository repository;

    @Mock
    private PayableMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentFrequencyRepository paymentFrequencyRepository;

    @Mock
    private FinancialSettingRepository financialSettingRepository;

    private PayableServiceImpl service;

    private Payable payable;
    private PayableDTO payableDTO;
    private User user;

    @BeforeEach
    void setUp() {
        PayableCalculationService calculationService = new PayableCalculationService(financialSettingRepository);
        service = new PayableServiceImpl(
                repository, mapper, authenticationFacade, userRepository,
                supplierRepository, employeeRepository, paymentMethodRepository,
                paymentFrequencyRepository, calculationService, new PayableFilterService()
        );

        payable = new Payable();
        payable.setId(1L);
        payable.setDescription("Aluguel");
        payable.setAmount(new BigDecimal("100.00"));
        payable.setRemainingBalance(new BigDecimal("100.00"));
        payable.setDueDate(LocalDate.now().plusMonths(2));
        payable.setPaid(false);
        payable.setCanceled(false);

        payableDTO = new PayableDTO();
        payableDTO.setId(1L);
        payableDTO.setDescription("Aluguel");

        user = new User();
        user.setId(9L);
        user.setName("Usuário Teste");
        user.setEmail("usuario@email.com");
    }

    @Test
    void findAllPagedByDescriptionShouldReturnPageOfPayables() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Payable> payables = new PageImpl<>(Collections.singletonList(payable));

        when(repository.findWithFilters(
                eq("Aluguel"), eq(PayableConstants.FILTER_DATE_DISABLED), eq(PayableConstants.FILTER_DATE_DISABLED),
                eq(false), eq(false), eq("ALL"), eq("DUE_DATE"),
                eq(-1L), eq(-1L), eq(-1L), eq(-1L),
                eq(PayableConstants.FILTER_AMOUNT_DISABLED), eq(PayableConstants.FILTER_AMOUNT_DISABLED),
                eq("dueDate"), eq("ASC"), eq(pageRequest)
        )).thenReturn(payables);
        when(mapper.toDTO(payable)).thenReturn(payableDTO);

        Page<PayableDTO> resultado = service.findAllPaged(" Aluguel ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Aluguel", resultado.getContent().get(0).getDescription());
        assertEquals(new BigDecimal("100.00"), resultado.getContent().get(0).getCurrentAmountWithLateCharges());
    }

    @Test
    void findAllPagedByDescriptionShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), eq(pageRequest)
        )).thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Aluguel", pageRequest));
    }

    @Test
    void findAllPagedWithFiltersShouldReturnPageOfPayables() {
        PageRequest pageRequest = PageRequest.of(1, 5);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);
        PayableFilterDTO filters = new PayableFilterDTO();
        filters.setSearch(" Aluguel ");
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setStatus(" open ");
        filters.setPeriodType(" payment ");
        filters.setSupplierId(2L);
        filters.setEmployeeId(3L);
        filters.setPaymentMethodId(4L);
        filters.setPaymentFrequencyId(5L);
        filters.setMinimumAmount(new BigDecimal("10.00"));
        filters.setMaximumAmount(new BigDecimal("150.00"));
        filters.setOrderBy("amount");
        filters.setDirection("desc");
        Page<Payable> payables = new PageImpl<>(Collections.singletonList(payable), pageRequest, 6);

        when(repository.findWithFilters(
                eq("Aluguel"), eq(startDate), eq(endDate), eq(true), eq(true), eq("PENDING"), eq("PAYMENT_DATE"),
                eq(2L), eq(3L), eq(4L), eq(5L), eq(new BigDecimal("10.00")), eq(new BigDecimal("150.00")),
                eq("amount"), eq("DESC"), eq(pageRequest)
        )).thenReturn(payables);
        when(mapper.toDTO(payable)).thenReturn(payableDTO);

        Page<PayableDTO> resultado = service.findAllPaged(filters, pageRequest);

        assertEquals(6, resultado.getTotalElements());
        assertEquals(1, resultado.getNumber());
        assertEquals(1L, resultado.getContent().get(0).getId());
        assertEquals(" open ", filters.getStatus());
    }

    @Test
    void findAllPagedWithFiltersShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        PayableFilterDTO filters = new PayableFilterDTO();
        when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), eq(pageRequest)
        )).thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged(filters, pageRequest));
    }

    @Test
    void findByIdShouldReturnPayableWithLateCharges() {
        payable.setDueDate(LocalDate.now().minusDays(2));
        payable.setCreatedBy(user);
        FinancialSetting setting = new FinancialSetting();
        setting.setDefaultLateFeePercent(new BigDecimal("2.00"));
        setting.setDefaultLateInterestPercent(new BigDecimal("1.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(payable));
        when(mapper.toDTO(payable)).thenCallRealMethod();
        when(financialSettingRepository.findBySingletonKey(FinancialSettingConstants.DEFAULT_SINGLETON_KEY))
                .thenReturn(Optional.of(setting));

        PayableDTO resultado = service.findById(1L);

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
    void findByIdShouldThrowExceptionWhenPayableDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSavePaidPayableWithRelationsAndAudit() {
        PayableInsertDTO insertDTO = new PayableInsertDTO();
        insertDTO.setSupplierId(2L);
        insertDTO.setEmployeeId(3L);
        insertDTO.setPaymentMethodId(4L);
        insertDTO.setPaymentFrequencyId(5L);
        payable.setPaid(true);
        payable.setPaymentDate(LocalDate.now());
        Supplier supplier = new Supplier();
        supplier.setId(2L);
        Employee employee = new Employee();
        employee.setId(3L);
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(4L);
        PaymentFrequency paymentFrequency = new PaymentFrequency();
        paymentFrequency.setId(5L);

        when(mapper.toEntity(insertDTO)).thenReturn(payable);
        when(supplierRepository.findById(2L)).thenReturn(Optional.of(supplier));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(employee));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(paymentMethod));
        when(paymentFrequencyRepository.findById(5L)).thenReturn(Optional.of(paymentFrequency));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);
        when(repository.save(payable)).thenReturn(payable);
        when(mapper.toDTO(payable)).thenReturn(payableDTO);

        PayableDTO resultado = service.insert(insertDTO);

        assertEquals(payableDTO, resultado);
        assertEquals(supplier, payable.getSupplier());
        assertEquals(employee, payable.getEmployee());
        assertEquals(paymentMethod, payable.getPaymentMethod());
        assertEquals(paymentFrequency, payable.getPaymentFrequency());
        assertEquals(user, payable.getCreatedBy());
        assertEquals(user, payable.getPaidBy());
        assertEquals(new BigDecimal("100.00"), payable.getSubtotal());
        assertEquals(BigDecimal.ZERO, payable.getRemainingBalance());
        verify(repository).save(payable);
    }

    @Test
    void insertShouldThrowExceptionWhenSupplierDoesNotExist() {
        PayableInsertDTO insertDTO = new PayableInsertDTO();
        insertDTO.setSupplierId(2L);
        when(mapper.toEntity(insertDTO)).thenReturn(payable);
        when(supplierRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.insert(insertDTO));

        verify(repository, never()).save(any());
    }

    @Test
    void updateShouldPreservePartialPaymentAndSetAudit() {
        LocalDate previousPaymentDate = LocalDate.now().minusDays(1);
        payable.setSubtotal(new BigDecimal("40.00"));
        payable.setRemainingBalance(new BigDecimal("60.00"));
        payable.setPaymentDate(previousPaymentDate);
        PayableUpdateDTO updateDTO = new PayableUpdateDTO();
        updateDTO.setDescription("Aluguel atualizado");
        updateDTO.setAmount(new BigDecimal("120.00"));
        updateDTO.setDueDate(payable.getDueDate());

        when(repository.findById(1L)).thenReturn(Optional.of(payable));
        doCallRealMethod().when(mapper).updateEntity(payable, updateDTO);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);
        when(repository.save(payable)).thenReturn(payable);
        when(mapper.toDTO(payable)).thenReturn(payableDTO);

        PayableDTO resultado = service.update(1L, updateDTO);

        assertEquals(payableDTO, resultado);
        assertEquals("Aluguel atualizado", payable.getDescription());
        assertEquals(new BigDecimal("120.00"), payable.getAmount());
        assertEquals(new BigDecimal("60.00"), payable.getRemainingBalance());
        assertEquals(new BigDecimal("40.00"), payable.getSubtotal());
        assertEquals(previousPaymentDate, payable.getPaymentDate());
        assertFalse(payable.getPaid());
        assertEquals(user, payable.getUpdatedBy());
        verify(mapper).updateEntity(payable, updateDTO);
    }

    @Test
    void updateShouldThrowExceptionWhenPayableIsPaid() {
        payable.setPaid(true);
        PayableUpdateDTO updateDTO = new PayableUpdateDTO();
        when(repository.findById(1L)).thenReturn(Optional.of(payable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, updateDTO));

        assertEquals(PayableConstants.PAID_PAYABLE_CANNOT_BE_UPDATED, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteShouldDeletePayable() {
        when(repository.findById(1L)).thenReturn(Optional.of(payable));

        service.delete(1L);

        verify(repository).delete(payable);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenIntegrityFails() {
        when(repository.findById(1L)).thenReturn(Optional.of(payable));
        doThrow(new DataIntegrityViolationException("Conta com vínculos")).when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> service.delete(1L));

        assertEquals(PayableConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
    }

    @Test
    void payShouldSavePartialPaymentWithBalanceAndCharges() {
        payable.setDueDate(LocalDate.now().minusDays(2));
        LocalDate paymentDate = LocalDate.now().minusDays(1);
        PayablePaymentDTO paymentDTO = new PayablePaymentDTO();
        paymentDTO.setPaymentAmount(new BigDecimal("40.00"));
        paymentDTO.setPaymentDate(paymentDate);
        paymentDTO.setPaymentMethodId(4L);
        paymentDTO.setFee(new BigDecimal("3.00"));
        paymentDTO.setLateInterest(new BigDecimal("2.00"));
        paymentDTO.setLateFee(new BigDecimal("4.00"));
        paymentDTO.setDiscount(new BigDecimal("1.00"));
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(4L);

        when(repository.findById(1L)).thenReturn(Optional.of(payable));
        when(paymentMethodRepository.findById(4L)).thenReturn(Optional.of(paymentMethod));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("usuario@email.com");
        when(userRepository.findByEmail("usuario@email.com")).thenReturn(user);
        when(repository.save(payable)).thenReturn(payable);
        when(mapper.toDTO(payable)).thenReturn(payableDTO);

        PayableDTO resultado = service.pay(1L, paymentDTO);

        assertEquals(payableDTO, resultado);
        assertFalse(payable.getPaid());
        assertEquals(new BigDecimal("40.00"), payable.getSubtotal());
        assertEquals(new BigDecimal("60.00"), payable.getRemainingBalance());
        assertEquals(paymentDate, payable.getPaymentDate());
        assertEquals(paymentMethod, payable.getPaymentMethod());
        assertEquals(new BigDecimal("3.00"), payable.getFee());
        assertEquals(new BigDecimal("2.00"), payable.getLateInterest());
        assertEquals(new BigDecimal("4.00"), payable.getLateFee());
        assertEquals(new BigDecimal("1.00"), payable.getDiscount());
        assertEquals(user, payable.getUpdatedBy());
        assertNull(payable.getPaidBy());
        verify(repository).save(payable);
    }

    @Test
    void payShouldRejectManualChargesForNonOverduePayable() {
        when(repository.findById(1L)).thenReturn(Optional.of(payable));

        for (int field = 0; field < 3; field++) {
            PayablePaymentDTO payment = new PayablePaymentDTO();
            payment.setPaymentAmount(new BigDecimal("10.00"));
            if (field == 0) {
                payment.setLateFee(new BigDecimal("2.00"));
            } else if (field == 1) {
                payment.setLateInterest(new BigDecimal("2.00"));
            } else {
                payment.setDiscount(new BigDecimal("2.00"));
            }

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> service.pay(1L, payment));

            assertEquals(PayableConstants.NON_OVERDUE_CHARGES_CANNOT_BE_EDITED, exception.getMessage());
        }
        verify(repository, never()).save(any());
    }

    @Test
    void payShouldRejectLateChargesOnDueDate() {
        payable.setDueDate(LocalDate.now());
        PayablePaymentDTO payment = new PayablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("10.00"));
        payment.setLateFee(new BigDecimal("1.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(payable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.pay(1L, payment));

        assertEquals(PayableConstants.NON_OVERDUE_CHARGES_CANNOT_BE_EDITED, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void payShouldAcceptAutomaticPixDiscountForNonOverduePayable() {
        PaymentMethod method = new PaymentMethod();
        method.setId(1L);
        method.setName("Pix");
        PayablePaymentDTO payment = new PayablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("95.00"));
        payment.setPaymentMethodId(1L);
        payment.setDiscount(new BigDecimal("5.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(payable));
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(method));
        when(repository.save(payable)).thenReturn(payable);
        when(mapper.toDTO(payable)).thenCallRealMethod();

        PayableDTO result = service.pay(1L, payment);

        assertTrue(result.getPaid());
        assertEquals(new BigDecimal("5.00"), result.getDiscount());
        assertEquals(0, result.getLateFee().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getLateInterest().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void payShouldThrowExceptionWhenPaymentExceedsLimit() {
        PayablePaymentDTO paymentDTO = new PayablePaymentDTO();
        paymentDTO.setPaymentAmount(new BigDecimal("101.00"));
        when(repository.findById(1L)).thenReturn(Optional.of(payable));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.pay(1L, paymentDTO));

        assertEquals(PayableConstants.PAYMENT_AMOUNT_EXCEEDS_PAYABLE, exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void findByIdShouldReturnOriginalAmountForExistingInstallment() {
        Payable installment = new Payable();
        installment.setId(2L);
        installment.setAmount(new BigDecimal("33.33"));
        installment.setRemainingBalance(new BigDecimal("33.33"));
        installment.setParentPayable(payable);

        when(repository.findById(2L)).thenReturn(Optional.of(installment));
        when(mapper.toDTO(installment)).thenCallRealMethod();

        PayableDTO result = service.findById(2L);

        assertEquals(new BigDecimal("100.00"), result.getOriginalAmount());
        assertEquals(new BigDecimal("33.33"), result.getAmount());
        assertEquals(new BigDecimal("33.33"), result.getRemainingBalance());
    }

    @Test
    void payShouldPreserveOriginalAmountAndSettleOnlyTheInstallment() {
        Payable original = new Payable();
        original.setId(2L);
        original.setAmount(new BigDecimal("300.00"));
        payable.setParentPayable(original);

        PayablePaymentDTO payment = new PayablePaymentDTO();
        payment.setPaymentAmount(new BigDecimal("100.00"));

        when(repository.findById(1L)).thenReturn(Optional.of(payable));
        when(repository.save(payable)).thenReturn(payable);
        when(mapper.toDTO(payable)).thenCallRealMethod();

        PayableDTO result = service.pay(1L, payment);

        assertEquals(new BigDecimal("300.00"), original.getAmount());
        assertEquals(new BigDecimal("300.00"), result.getOriginalAmount());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(0, result.getRemainingBalance().compareTo(BigDecimal.ZERO));
        assertTrue(result.getPaid());
    }

    @Test
    void reportShouldReturnTotalsForFilteredPayables() {
        Payable paidPayable = new Payable();
        paidPayable.setId(2L);
        paidPayable.setAmount(new BigDecimal("20.00"));
        paidPayable.setPaid(true);
        Page<Payable> payables = new PageImpl<>(Arrays.asList(payable, paidPayable));

        when(repository.findWithFilters(
                eq("Aluguel"), eq(PayableConstants.FILTER_DATE_DISABLED), eq(PayableConstants.FILTER_DATE_DISABLED),
                eq(false), eq(false), eq("ALL"), eq("DUE_DATE"), eq(-1L), eq(-1L), eq(-1L), eq(-1L),
                eq(PayableConstants.FILTER_AMOUNT_DISABLED), eq(PayableConstants.FILTER_AMOUNT_DISABLED),
                eq("dueDate"), eq("ASC"), eq(Pageable.unpaged())
        )).thenReturn(payables);

        PayableReportDTO resultado = service.report("Aluguel", null, null, "ALL", "DUE");

        assertEquals(2L, resultado.getTotalItems());
        assertEquals(new BigDecimal("120.00"), resultado.getTotalAmount());
        assertEquals(new BigDecimal("20.00"), resultado.getPaidAmount());
        assertEquals(new BigDecimal("100.00"), resultado.getOpenAmount());
    }

    @Test
    void reportShouldThrowExceptionWhenRepositoryFails() {
        when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), eq(Pageable.unpaged())
        )).thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.report("Aluguel", null, null, "ALL", "DUE"));
    }
}
