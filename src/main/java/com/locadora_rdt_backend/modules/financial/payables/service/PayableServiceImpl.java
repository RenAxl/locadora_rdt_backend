package com.locadora_rdt_backend.modules.financial.payables.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payables.constants.PayableConstants;
import com.locadora_rdt_backend.modules.financial.payables.dto.*;
import com.locadora_rdt_backend.modules.financial.payables.service.PayableFilterService;
import com.locadora_rdt_backend.modules.financial.payables.mapper.PayableMapper;
import com.locadora_rdt_backend.modules.financial.payables.model.Payable;
import com.locadora_rdt_backend.modules.financial.payables.repository.PayableRepository;
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
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PayableServiceImpl implements PayableService {

    private final PayableRepository repository;
    private final PayableMapper mapper;
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentFrequencyRepository paymentFrequencyRepository;
    private final PayableCalculationService calculationService;
    private final PayableFilterService filterService;

    public PayableServiceImpl(
            PayableRepository repository,
            PayableMapper mapper,
            AuthenticationFacade authenticationFacade,
            UserRepository userRepository,
            SupplierRepository supplierRepository,
            EmployeeRepository employeeRepository,
            PaymentMethodRepository paymentMethodRepository,
            PaymentFrequencyRepository paymentFrequencyRepository,
            PayableCalculationService calculationService,
            PayableFilterService filterService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentFrequencyRepository = paymentFrequencyRepository;
        this.calculationService = calculationService;
        this.filterService = filterService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayableDTO> findAllPaged(String description, PageRequest pageRequest) {
        PayableFilterDTO filters = new PayableFilterDTO();
        filters.setSearch(description);
        filters.setStatus(PayableConstants.STATUS_ALL);
        filters.setPeriodType(PayableConstants.PERIOD_DUE_DATE);
        filters.setOrderBy(PayableConstants.ORDER_BY_DUE_DATE);
        filters.setDirection(PayableConstants.DIRECTION_ASC);
        return findAllPaged(filters, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PayableDTO> findAllPaged(PayableFilterDTO filters, PageRequest pageRequest) {

        Page<Payable> payables = findPayables(filters, pageRequest);

        Page<PayableDTO> payablesDTO = payables.map(payable -> toDTOWithLateCharges(payable));

        return payablesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PayableDTO findById(Long id) {

        Payable payable = findPayableById(id);

        PayableDTO payableDTO = toDTOWithLateCharges(payable);

        return payableDTO;
    }

    @Override
    @Transactional
    public PayableDTO insert(PayableInsertDTO dto) {
        Payable payable = mapper.toEntity(dto);
        payable.setSupplier(findSupplier(dto.getSupplierId()));
        payable.setEmployee(findEmployee(dto.getEmployeeId()));
        payable.setPaymentMethod(findPaymentMethod(dto.getPaymentMethodId()));
        payable.setPaymentFrequency(findPaymentFrequency(dto.getPaymentFrequencyId()));
        payable.setCreatedBy(getAuthenticatedUser());

        if (payable.getPaid()) {
            payable.setPaidBy(payable.getCreatedBy());
            payable.setSubtotal(calculationService.valueOrZero(payable.getAmount()));
            payable.setRemainingBalance(PayableConstants.ZERO);
        }

        Payable savedPayable = repository.save(payable);

        PayableDTO payableDTO = toDTOWithLateCharges(savedPayable);

        return payableDTO;
    }

    @Override
    @Transactional
    public PayableDTO update(Long id, PayableUpdateDTO dto) {
        Payable payable = findPayableById(id);

        if (Boolean.TRUE.equals(payable.getPaid())) {
            throw new IllegalArgumentException(PayableConstants.PAID_PAYABLE_CANNOT_BE_UPDATED);
        }

        if (Boolean.TRUE.equals(payable.getCanceled())) {
            throw new IllegalArgumentException(PayableConstants.CANCELED_PAYABLE_CANNOT_BE_UPDATED);
        }

        boolean wasPartiallyPaid = calculationService.isPartiallyPaid(payable);
        BigDecimal previousRemainingBalance = payable.getRemainingBalance();
        LocalDate previousPaymentDate = payable.getPaymentDate();

        mapper.updateEntity(payable, dto);
        payable.setSupplier(findSupplier(dto.getSupplierId()));
        payable.setEmployee(findEmployee(dto.getEmployeeId()));
        payable.setPaymentMethod(findPaymentMethod(dto.getPaymentMethodId()));
        payable.setPaymentFrequency(findPaymentFrequency(dto.getPaymentFrequencyId()));
        payable.setUpdatedBy(getAuthenticatedUser());

        if (wasPartiallyPaid) {
            payable.setPaid(false);
            payable.setRemainingBalance(previousRemainingBalance);

            if (dto.getPaymentDate() == null) {
                payable.setPaymentDate(previousPaymentDate);
            }
        }

        if (payable.getPaid()) {
            if (payable.getPaidBy() == null) {
                payable.setPaidBy(payable.getUpdatedBy());
            }

            payable.setRemainingBalance(PayableConstants.ZERO);
        }

        Payable savedPayable = repository.save(payable);

        PayableDTO payableDTO = toDTOWithLateCharges(savedPayable);

        return payableDTO;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Payable payable = findPayableById(id);

        try {
            repository.delete(payable);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(PayableConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public PayableDTO pay(Long id, PayablePaymentDTO dto) {
        Payable payable = findPayableById(id);

        PaymentMethod requestedPaymentMethod = findPaymentMethod(dto.getPaymentMethodId());
        PaymentMethod paymentMethod = requestedPaymentMethod;

        if (paymentMethod == null) {
            paymentMethod = payable.getPaymentMethod();
        }

        dto.setFee(calculationService.getPaymentMethodFee(payable, paymentMethod));

        BigDecimal paymentAmount = calculationService.valueOrZero(dto.getPaymentAmount());
        validatePayment(payable, dto, paymentAmount);

        User user = getAuthenticatedUser();
        BigDecimal amount = calculationService.valueOrZero(payable.getAmount());
        BigDecimal openAmount = calculationService.getOpenAmount(payable);
        BigDecimal paidAmount = amount.subtract(openAmount);
        validatePaymentCharges(payable, dto);
        BigDecimal paymentLimit = calculationService.getCurrentPaymentLimit(payable, dto);

        if (paymentAmount.compareTo(paymentLimit) == 0 || paymentAmount.compareTo(openAmount) >= 0) {
            payTotal(payable, dto, user, requestedPaymentMethod, paidAmount.add(openAmount));
            Payable savedPayable = repository.save(payable);

            PayableDTO payableDTO = toDTOWithLateCharges(savedPayable);

            return payableDTO;
        }

        BigDecimal remaining = openAmount.subtract(paymentAmount);
        payable.setSubtotal(paidAmount.add(paymentAmount));
        payable.setRemainingBalance(remaining);
        payable.setPaid(false);
        LocalDate paymentDate = dto.getPaymentDate();

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        payable.setPaymentDate(paymentDate);

        if (requestedPaymentMethod != null) {
            payable.setPaymentMethod(requestedPaymentMethod);
        }

        payable.setFee(calculationService.valueOrZero(dto.getFee()));
        payable.setLateInterest(calculationService.valueOrZero(dto.getLateInterest()));
        payable.setLateFee(calculationService.valueOrZero(dto.getLateFee()));
        payable.setDiscount(PayableConstants.ZERO);
        payable.setUpdatedBy(user);

        Payable savedPayable = repository.save(payable);

        PayableDTO payableDTO = toDTOWithLateCharges(savedPayable);

        return payableDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PayableReportDTO report(
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String dateType
    ) {
        PayableFilterDTO filters = new PayableFilterDTO();
        filters.setSearch(description);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setStatus(status);
        filters.setPeriodType(dateType);

        List<Payable> payables = findPayables(filters, Pageable.unpaged()).getContent();
        BigDecimal totalAmount = PayableConstants.ZERO;
        BigDecimal paidAmount = PayableConstants.ZERO;

        for (Payable payable : payables) {
            BigDecimal amount = calculationService.valueOrZero(payable.getAmount());
            totalAmount = totalAmount.add(amount);

            if (Boolean.TRUE.equals(payable.getPaid())) {
                paidAmount = paidAmount.add(amount);
            }
        }

        BigDecimal openAmount = totalAmount.subtract(paidAmount);

        PayableReportDTO payableReportDTO = new PayableReportDTO(
                (long) payables.size(), totalAmount, paidAmount, openAmount
        );

        return payableReportDTO;
    }

    private PayableDTO toDTOWithLateCharges(Payable payable) {
        PayableDTO dto = mapper.toDTO(payable);
        calculationService.fillLateCharges(payable, dto);

        if (Boolean.TRUE.equals(payable.getPaid())) {
            dto.setSubtotal(dto.getCurrentAmountWithLateCharges());
        }

        return dto;
    }

    private User getAuthenticatedUser() {
        String username = authenticationFacade.getAuthenticatedUsername();
        if (username == null) {
            return null;
        }

        return userRepository.findByEmail(username);
    }

    private Payable findPayableById(Long id) {
        Optional<Payable> optionalPayable = repository.findById(id);

        if (!optionalPayable.isPresent()) {
            throw new ResourceNotFoundException(
                    PayableConstants.PAYABLE_NOT_FOUND + PayableConstants.ID_COMPLEMENT + id
            );
        }

        Payable payable = optionalPayable.get();

        return payable;
    }

    private Page<Payable> findPayables(PayableFilterDTO filters, Pageable pageable) {
        PayableFilterDTO normalized = filterService.normalizeFilters(filters);

        Page<Payable> payables = repository.findWithFilters(
                normalized.getSearch(),
                filterService.dateFilterOrDisabled(normalized.getStartDate()),
                filterService.dateFilterOrDisabled(normalized.getEndDate()),
                normalized.getStartDate() != null,
                normalized.getEndDate() != null,
                normalized.getStatus(),
                normalized.getPeriodType(),
                filterService.idFilterOrDisabled(normalized.getSupplierId()),
                filterService.idFilterOrDisabled(normalized.getEmployeeId()),
                filterService.idFilterOrDisabled(normalized.getPaymentMethodId()),
                filterService.idFilterOrDisabled(normalized.getPaymentFrequencyId()),
                filterService.amountFilterOrDisabled(normalized.getMinimumAmount()),
                filterService.amountFilterOrDisabled(normalized.getMaximumAmount()),
                normalized.getOrderBy(),
                normalized.getDirection(),
                pageable
        );

        return payables;
    }

    private Supplier findSupplier(Long id) {
        Long normalizedId = filterService.normalizeId(id);

        if (normalizedId == null) {
            return null;
        }

        Optional<Supplier> supplierOptional = supplierRepository.findById(normalizedId);

        if (!supplierOptional.isPresent()) {
            throw new ResourceNotFoundException(PayableConstants.SUPPLIER_NOT_FOUND + normalizedId);
        }

        Supplier supplier = supplierOptional.get();

        return supplier;
    }

    private Employee findEmployee(Long id) {
        Long normalizedId = filterService.normalizeId(id);

        if (normalizedId == null) {
            return null;
        }

        Optional<Employee> employeeOptional = employeeRepository.findById(normalizedId);

        if (!employeeOptional.isPresent()) {
            throw new ResourceNotFoundException(PayableConstants.EMPLOYEE_NOT_FOUND + normalizedId);
        }

        Employee employee = employeeOptional.get();

        return employee;
    }

    private PaymentMethod findPaymentMethod(Long id) {
        Long normalizedId = filterService.normalizeId(id);

        if (normalizedId == null) {
            return null;
        }

        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById(normalizedId);

        if (!paymentMethodOptional.isPresent()) {
            throw new ResourceNotFoundException(PayableConstants.PAYMENT_METHOD_NOT_FOUND + normalizedId);
        }

        PaymentMethod paymentMethod = paymentMethodOptional.get();

        return paymentMethod;
    }

    private PaymentFrequency findPaymentFrequency(Long id) {
        Long normalizedId = filterService.normalizeId(id);

        if (normalizedId == null) {
            return null;
        }

        Optional<PaymentFrequency> paymentFrequencyOptional = paymentFrequencyRepository.findById(normalizedId);

        if (!paymentFrequencyOptional.isPresent()) {
            throw new ResourceNotFoundException(PayableConstants.PAYMENT_FREQUENCY_NOT_FOUND + normalizedId);
        }

        PaymentFrequency paymentFrequency = paymentFrequencyOptional.get();

        return paymentFrequency;
    }

    private void validatePayment(Payable payable, PayablePaymentDTO dto, BigDecimal paymentAmount) {
        if (Boolean.TRUE.equals(payable.getPaid())) {
            throw new IllegalArgumentException(PayableConstants.PAID_PAYABLE_CANNOT_BE_PAID);
        }

        if (Boolean.TRUE.equals(payable.getCanceled())) {
            throw new IllegalArgumentException(PayableConstants.CANCELED_PAYABLE_CANNOT_BE_PAID);
        }

        if (paymentAmount.compareTo(PayableConstants.ZERO) <= 0) {
            throw new IllegalArgumentException(PayableConstants.PAYMENT_AMOUNT_MUST_BE_POSITIVE);
        }

        if (paymentAmount.compareTo(calculationService.getCurrentPaymentLimit(payable, dto)) > 0) {
            throw new IllegalArgumentException(PayableConstants.PAYMENT_AMOUNT_EXCEEDS_PAYABLE);
        }
    }

    private void validatePaymentCharges(Payable payable, PayablePaymentDTO dto) {
        if (calculationService.isOverdueOpenPayable(payable)) {
            return;
        }

        if (calculationService.valueOrZero(dto.getLateFee()).compareTo(PayableConstants.ZERO) != 0
                || calculationService.valueOrZero(dto.getLateInterest()).compareTo(PayableConstants.ZERO) != 0) {
            throw new IllegalArgumentException(PayableConstants.NON_OVERDUE_CHARGES_CANNOT_BE_EDITED);
        }
    }

    private void payTotal(
            Payable payable,
            PayablePaymentDTO dto,
            User user,
            PaymentMethod requestedPaymentMethod,
            BigDecimal paidAmount
    ) {
        payable.setPaid(true);
        LocalDate paymentDate = dto.getPaymentDate();

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        payable.setPaymentDate(paymentDate);

        if (requestedPaymentMethod != null) {
            payable.setPaymentMethod(requestedPaymentMethod);
        }

        payable.setSubtotal(paidAmount);
        payable.setFee(calculationService.valueOrZero(dto.getFee()));
        payable.setLateInterest(calculationService.valueOrZero(dto.getLateInterest()));
        payable.setLateFee(calculationService.valueOrZero(dto.getLateFee()));
        payable.setDiscount(PayableConstants.ZERO);
        payable.setRemainingBalance(PayableConstants.ZERO);
        payable.setPaidBy(user);
        payable.setUpdatedBy(user);
    }

}
