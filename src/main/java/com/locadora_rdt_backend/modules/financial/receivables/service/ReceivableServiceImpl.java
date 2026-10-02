package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.receivables.constants.ReceivableConstants;
import com.locadora_rdt_backend.modules.financial.receivables.dto.*;
import com.locadora_rdt_backend.modules.financial.receivables.mapper.ReceivableMapper;
import com.locadora_rdt_backend.modules.financial.receivables.model.Receivable;
import com.locadora_rdt_backend.modules.financial.receivables.repository.ReceivableRepository;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.repository.PaymentFrequencyRepository;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.financial.payment_methods.repository.PaymentMethodRepository;
import com.locadora_rdt_backend.modules.identity.users.model.User;
import com.locadora_rdt_backend.modules.identity.users.repository.UserRepository;
import com.locadora_rdt_backend.modules.organization.customers.model.Customer;
import com.locadora_rdt_backend.modules.organization.customers.repository.CustomerRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import com.lowagie.text.DocumentException;
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
public class ReceivableServiceImpl implements ReceivableService {

    private final ReceivableRepository repository;
    private final ReceivableMapper mapper;
    private final AuthenticationFacade authenticationFacade;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentFrequencyRepository paymentFrequencyRepository;
    private final ReceivableCalculationService calculationService;
    private final ReceivableFilterService filterService;
    private final ReceivableDocumentPdfService documentPdfService;

    public ReceivableServiceImpl(
            ReceivableRepository repository,
            ReceivableMapper mapper,
            AuthenticationFacade authenticationFacade,
            UserRepository userRepository,
            CustomerRepository customerRepository,
            PaymentMethodRepository paymentMethodRepository,
            PaymentFrequencyRepository paymentFrequencyRepository,
            ReceivableCalculationService calculationService,
            ReceivableFilterService filterService,
            ReceivableDocumentPdfService documentPdfService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentFrequencyRepository = paymentFrequencyRepository;
        this.calculationService = calculationService;
        this.filterService = filterService;
        this.documentPdfService = documentPdfService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceivableDTO> findAllPaged(String description, PageRequest pageRequest) {
        ReceivableFilterDTO filters = new ReceivableFilterDTO();
        filters.setSearch(description);
        filters.setStatus(ReceivableConstants.STATUS_ALL);
        filters.setPeriodType(ReceivableConstants.PERIOD_DUE_DATE);
        filters.setOrderBy(ReceivableConstants.ORDER_BY_DUE_DATE);
        filters.setDirection(ReceivableConstants.DIRECTION_ASC);
        return findAllPaged(filters, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReceivableDTO> findAllPaged(ReceivableFilterDTO filters, PageRequest pageRequest) {

        Page<Receivable> receivables = findReceivables(filters, pageRequest);

        Page<ReceivableDTO> receivablesDTO = receivables.map(receivable -> toDTOWithLateCharges(receivable));

        return receivablesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivableDTO findById(Long id) {

        Receivable receivable = findReceivableById(id);

        ReceivableDTO receivableDTO = toDTOWithLateCharges(receivable);

        return receivableDTO;
    }

    @Override
    @Transactional
    public ReceivableDTO insert(ReceivableInsertDTO dto) {
        validateCustomerOrDescription(dto.getCustomerId(), dto.getDescription());

        Receivable receivable = mapper.toEntity(dto);
        receivable.setCustomer(findCustomer(dto.getCustomerId()));
        receivable.setPaymentMethod(findPaymentMethod(dto.getPaymentMethodId()));
        receivable.setPaymentFrequency(findPaymentFrequency(dto.getPaymentFrequencyId()));
        receivable.setCreatedBy(getAuthenticatedUser());

        if (receivable.getPaid()) {
            receivable.setPaidBy(receivable.getCreatedBy());
            receivable.setSubtotal(calculationService.valueOrZero(receivable.getAmount()));
            receivable.setRemainingBalance(ReceivableConstants.ZERO);
        }

        Receivable savedReceivable = repository.save(receivable);

        ReceivableDTO receivableDTO = toDTOWithLateCharges(savedReceivable);

        return receivableDTO;
    }

    @Override
    @Transactional
    public void createFromRental(
            Long rentalId,
            String rentalNumber,
            BigDecimal remainingAmount,
            Customer customer,
            PaymentMethod paymentMethod
    ) {
        if (repository.existsByReferenceAndReferenceId(ReceivableConstants.RENTAL_REFERENCE, rentalId)) {
            return;
        }

        LocalDate paymentDate = LocalDate.now();
        BigDecimal amount = calculationService.valueOrZero(remainingAmount);
        User user = getAuthenticatedUser();

        Receivable receivable = new Receivable();
        receivable.setDescription(ReceivableConstants.RENTAL_DESCRIPTION_PREFIX + rentalNumber);
        receivable.setAmount(amount);
        receivable.setDueDate(paymentDate);
        receivable.setPaymentDate(paymentDate);
        receivable.setCustomer(customer);
        receivable.setPaymentMethod(paymentMethod);
        receivable.setPaymentFrequency(findCashPaymentFrequency());
        receivable.setReference(ReceivableConstants.RENTAL_REFERENCE);
        receivable.setReferenceId(rentalId);
        receivable.setNote(ReceivableConstants.RENTAL_NOTE);
        receivable.setPaid(true);
        receivable.setSubtotal(amount);
        receivable.setRemainingBalance(ReceivableConstants.ZERO);
        receivable.setCreatedBy(user);
        receivable.setPaidBy(user);

        repository.save(receivable);
    }

    @Override
    @Transactional
    public ReceivableDTO update(Long id, ReceivableUpdateDTO dto) {
        Receivable receivable = findReceivableById(id);

        if (Boolean.TRUE.equals(receivable.getPaid())) {
            throw new IllegalArgumentException(ReceivableConstants.PAID_RECEIVABLE_CANNOT_BE_UPDATED);
        }

        if (Boolean.TRUE.equals(receivable.getCanceled())) {
            throw new IllegalArgumentException(ReceivableConstants.CANCELED_RECEIVABLE_CANNOT_BE_UPDATED);
        }

        validateCustomerOrDescription(dto.getCustomerId(), dto.getDescription());

        boolean wasPartiallyPaid = calculationService.isPartiallyPaid(receivable);
        BigDecimal previousRemainingBalance = receivable.getRemainingBalance();
        LocalDate previousPaymentDate = receivable.getPaymentDate();

        mapper.updateEntity(receivable, dto);
        receivable.setCustomer(findCustomer(dto.getCustomerId()));
        receivable.setPaymentMethod(findPaymentMethod(dto.getPaymentMethodId()));
        receivable.setPaymentFrequency(findPaymentFrequency(dto.getPaymentFrequencyId()));
        receivable.setUpdatedBy(getAuthenticatedUser());

        if (wasPartiallyPaid) {
            receivable.setPaid(false);
            receivable.setRemainingBalance(previousRemainingBalance);

            if (dto.getPaymentDate() == null) {
                receivable.setPaymentDate(previousPaymentDate);
            }
        }

        if (receivable.getPaid()) {
            if (receivable.getPaidBy() == null) {
                receivable.setPaidBy(receivable.getUpdatedBy());
            }

            receivable.setRemainingBalance(ReceivableConstants.ZERO);
        }

        Receivable savedReceivable = repository.save(receivable);

        ReceivableDTO receivableDTO = toDTOWithLateCharges(savedReceivable);

        return receivableDTO;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Receivable receivable = findReceivableById(id);

        try {
            repository.delete(receivable);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(ReceivableConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public ReceivableDTO pay(Long id, ReceivablePaymentDTO dto) {
        Receivable receivable = findReceivableById(id);

        PaymentMethod requestedPaymentMethod = findPaymentMethod(dto.getPaymentMethodId());
        PaymentMethod paymentMethod = requestedPaymentMethod;

        if (paymentMethod == null) {
            paymentMethod = receivable.getPaymentMethod();
        }

        dto.setFee(calculationService.getPaymentMethodFee(receivable, paymentMethod));

        BigDecimal paymentAmount = calculationService.valueOrZero(dto.getPaymentAmount());
        validatePayment(receivable, dto, paymentAmount);

        User user = getAuthenticatedUser();
        BigDecimal amount = calculationService.valueOrZero(receivable.getAmount());
        BigDecimal openAmount = calculationService.getOpenAmount(receivable);
        BigDecimal paidAmount = amount.subtract(openAmount);
        BigDecimal paymentLimit = calculationService.getCurrentPaymentLimit(receivable, dto);

        if (paymentAmount.compareTo(paymentLimit) == 0 || paymentAmount.compareTo(openAmount) >= 0) {
            payTotal(receivable, dto, user, requestedPaymentMethod, paidAmount.add(openAmount));
            Receivable savedReceivable = repository.save(receivable);

            ReceivableDTO receivableDTO = toDTOWithLateCharges(savedReceivable);

            return receivableDTO;
        }

        BigDecimal remaining = openAmount.subtract(paymentAmount);
        receivable.setSubtotal(paidAmount.add(paymentAmount));
        receivable.setRemainingBalance(remaining);
        receivable.setPaid(false);
        LocalDate paymentDate = dto.getPaymentDate();

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        receivable.setPaymentDate(paymentDate);

        if (requestedPaymentMethod != null) {
            receivable.setPaymentMethod(requestedPaymentMethod);
        }

        receivable.setFee(calculationService.valueOrZero(dto.getFee()));
        receivable.setLateInterest(calculationService.valueOrZero(dto.getLateInterest()));
        receivable.setLateFee(calculationService.valueOrZero(dto.getLateFee()));
        receivable.setDiscount(ReceivableConstants.ZERO);
        receivable.setUpdatedBy(user);

        Receivable savedReceivable = repository.save(receivable);

        ReceivableDTO receivableDTO = toDTOWithLateCharges(savedReceivable);

        return receivableDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ReceivableReportDTO report(
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            String dateType
    ) {
        ReceivableFilterDTO filters = new ReceivableFilterDTO();
        filters.setSearch(description);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setStatus(status);
        filters.setPeriodType(dateType);

        List<Receivable> receivables = findReceivables(filters, Pageable.unpaged()).getContent();
        BigDecimal totalAmount = ReceivableConstants.ZERO;
        BigDecimal paidAmount = ReceivableConstants.ZERO;

        for (Receivable receivable : receivables) {
            BigDecimal amount = calculationService.valueOrZero(receivable.getAmount());
            totalAmount = totalAmount.add(amount);

            if (Boolean.TRUE.equals(receivable.getPaid())) {
                paidAmount = paidAmount.add(amount);
            }
        }

        BigDecimal openAmount = totalAmount.subtract(paidAmount);

        ReceivableReportDTO receivableReportDTO = new ReceivableReportDTO(
                (long) receivables.size(), totalAmount, paidAmount, openAmount
        );

        return receivableReportDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] receipt(Long id) {
        Receivable receivable = findReceivableById(id);

        if (!Boolean.TRUE.equals(receivable.getPaid())) {
            throw new IllegalArgumentException(ReceivableConstants.RECEIPT_ONLY_FOR_PAID_RECEIVABLE);
        }

        try {
            byte[] receipt = documentPdfService.buildReceiptPdf(receivable);

            return receipt;
        } catch (DocumentException e) {
            throw new IllegalStateException(ReceivableConstants.RECEIPT_GENERATION_ERROR, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] fiscalCoupon(Long id) {
        Receivable receivable = findReceivableById(id);

        if (!Boolean.TRUE.equals(receivable.getPaid())) {
            throw new IllegalArgumentException(ReceivableConstants.FISCAL_COUPON_ONLY_FOR_PAID_RECEIVABLE);
        }

        try {
            byte[] fiscalCoupon = documentPdfService.buildFiscalCouponPdf(receivable);

            return fiscalCoupon;
        } catch (DocumentException e) {
            throw new IllegalStateException(ReceivableConstants.FISCAL_COUPON_GENERATION_ERROR, e);
        }
    }

    private ReceivableDTO toDTOWithLateCharges(Receivable receivable) {
        ReceivableDTO dto = mapper.toDTO(receivable);
        calculationService.fillLateCharges(receivable, dto);

        if (Boolean.TRUE.equals(receivable.getPaid())) {
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

    private Receivable findReceivableById(Long id) {
        Optional<Receivable> optionalReceivable = repository.findById(id);

        if (!optionalReceivable.isPresent()) {
            throw new ResourceNotFoundException(
                    ReceivableConstants.RECEIVABLE_NOT_FOUND + ReceivableConstants.ID_COMPLEMENT + id
            );
        }

        Receivable receivable = optionalReceivable.get();

        return receivable;
    }

    private Page<Receivable> findReceivables(ReceivableFilterDTO filters, Pageable pageable) {
        ReceivableFilterDTO normalized = filterService.normalizeFilters(filters);

        Page<Receivable> receivables = repository.findWithFilters(
                normalized.getSearch(),
                filterService.dateFilterOrDisabled(normalized.getStartDate()),
                filterService.dateFilterOrDisabled(normalized.getEndDate()),
                normalized.getStartDate() != null,
                normalized.getEndDate() != null,
                normalized.getStatus(),
                normalized.getPeriodType(),
                filterService.idFilterOrDisabled(normalized.getCustomerId()),
                filterService.idFilterOrDisabled(normalized.getPaymentMethodId()),
                filterService.idFilterOrDisabled(normalized.getPaymentFrequencyId()),
                filterService.amountFilterOrDisabled(normalized.getMinimumAmount()),
                filterService.amountFilterOrDisabled(normalized.getMaximumAmount()),
                normalized.getOrderBy(),
                normalized.getDirection(),
                pageable
        );

        return receivables;
    }

    private Customer findCustomer(Long id) {
        Long normalizedId = filterService.normalizeId(id);

        if (normalizedId == null) {
            return null;
        }

        Optional<Customer> customerOptional = customerRepository.findById(normalizedId);

        if (!customerOptional.isPresent()) {
            throw new ResourceNotFoundException(ReceivableConstants.CUSTOMER_NOT_FOUND + normalizedId);
        }

        Customer customer = customerOptional.get();

        return customer;
    }

    private PaymentMethod findPaymentMethod(Long id) {
        Long normalizedId = filterService.normalizeId(id);

        if (normalizedId == null) {
            return null;
        }

        Optional<PaymentMethod> paymentMethodOptional = paymentMethodRepository.findById(normalizedId);

        if (!paymentMethodOptional.isPresent()) {
            throw new ResourceNotFoundException(ReceivableConstants.PAYMENT_METHOD_NOT_FOUND + normalizedId);
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
            throw new ResourceNotFoundException(ReceivableConstants.PAYMENT_FREQUENCY_NOT_FOUND + normalizedId);
        }

        PaymentFrequency paymentFrequency = paymentFrequencyOptional.get();

        return paymentFrequency;
    }

    private void validateCustomerOrDescription(Long customerId, String description) {
        boolean hasCustomer = filterService.normalizeId(customerId) != null;
        boolean hasDescription = description != null && !description.trim().isEmpty();

        if (!hasCustomer && !hasDescription) {
            throw new IllegalArgumentException(ReceivableConstants.CUSTOMER_OR_DESCRIPTION_REQUIRED);
        }
    }

    private PaymentFrequency findCashPaymentFrequency() {
        List<PaymentFrequency> frequencies = paymentFrequencyRepository.find(
                ReceivableConstants.CASH_PAYMENT_FREQUENCY, Pageable.unpaged()
        ).getContent();

        for (PaymentFrequency frequency : frequencies) {
            if (ReceivableConstants.CASH_PAYMENT_FREQUENCY.equalsIgnoreCase(frequency.getFrequency())) {
                return frequency;
            }
        }

        throw new ResourceNotFoundException(ReceivableConstants.CASH_PAYMENT_FREQUENCY_NOT_FOUND);
    }

    private void validatePayment(Receivable receivable, ReceivablePaymentDTO dto, BigDecimal paymentAmount) {
        if (Boolean.TRUE.equals(receivable.getPaid())) {
            throw new IllegalArgumentException(ReceivableConstants.PAID_RECEIVABLE_CANNOT_BE_PAID);
        }

        if (Boolean.TRUE.equals(receivable.getCanceled())) {
            throw new IllegalArgumentException(ReceivableConstants.CANCELED_RECEIVABLE_CANNOT_BE_PAID);
        }

        if (paymentAmount.compareTo(ReceivableConstants.ZERO) <= 0) {
            throw new IllegalArgumentException(ReceivableConstants.PAYMENT_AMOUNT_MUST_BE_POSITIVE);
        }

        if (paymentAmount.compareTo(calculationService.getCurrentPaymentLimit(receivable, dto)) > 0) {
            throw new IllegalArgumentException(ReceivableConstants.PAYMENT_AMOUNT_EXCEEDS_RECEIVABLE);
        }
    }

    private void payTotal(
            Receivable receivable,
            ReceivablePaymentDTO dto,
            User user,
            PaymentMethod requestedPaymentMethod,
            BigDecimal paidAmount
    ) {
        receivable.setPaid(true);
        LocalDate paymentDate = dto.getPaymentDate();

        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }

        receivable.setPaymentDate(paymentDate);

        if (requestedPaymentMethod != null) {
            receivable.setPaymentMethod(requestedPaymentMethod);
        }

        receivable.setSubtotal(paidAmount);
        receivable.setFee(calculationService.valueOrZero(dto.getFee()));
        receivable.setLateInterest(calculationService.valueOrZero(dto.getLateInterest()));
        receivable.setLateFee(calculationService.valueOrZero(dto.getLateFee()));
        receivable.setDiscount(ReceivableConstants.ZERO);
        receivable.setRemainingBalance(ReceivableConstants.ZERO);
        receivable.setPaidBy(user);
        receivable.setUpdatedBy(user);
    }

}
