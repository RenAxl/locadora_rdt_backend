package com.locadora_rdt_backend.modules.financial.payment_methods.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payment_methods.constants.PaymentMethodConstants;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.*;
import com.locadora_rdt_backend.modules.financial.payment_methods.mapper.PaymentMethodMapper;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import com.locadora_rdt_backend.modules.financial.payment_methods.repository.PaymentMethodRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository repository;
    private final PaymentMethodMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public PaymentMethodServiceImpl(
            PaymentMethodRepository repository,
            PaymentMethodMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentMethodDTO> findAllPaged(String name, PageRequest pageRequest) {

        String filterName = "";

        if (name != null) {
            filterName = name.trim();
        }

        Page<PaymentMethod> paymentMethods = repository.find(filterName, pageRequest);

        Page<PaymentMethodDTO> paymentMethodsDTO = paymentMethods.map(paymentMethod -> mapper.toDTO(paymentMethod));

        return paymentMethodsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodDTO findById(Long id) {

        Optional<PaymentMethod> paymentMethodOptional = repository.findById(id);

        if (!paymentMethodOptional.isPresent()) {
            throw new ResourceNotFoundException(PaymentMethodConstants.PAYMENT_METHOD_NOT_FOUND);
        }

        PaymentMethod paymentMethod = paymentMethodOptional.get();

        PaymentMethodDTO paymentMethodDTO = mapper.toDTO(paymentMethod);

        return paymentMethodDTO;
    }

    @Override
    @Transactional
    public PaymentMethodDTO insert(PaymentMethodInsertDTO dto) {

        PaymentMethod paymentMethod = mapper.toEntity(dto);

        paymentMethod.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        PaymentMethod savedPaymentMethod = repository.save(paymentMethod);

        PaymentMethodDTO paymentMethodDTO = mapper.toDTO(savedPaymentMethod);

        return paymentMethodDTO;
    }

    @Override
    @Transactional
    public PaymentMethodDTO update(Long id, PaymentMethodUpdateDTO dto) {

        try {

            PaymentMethod paymentMethod = repository.getOne(id);

            mapper.updateEntity(paymentMethod, dto);

            paymentMethod.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            PaymentMethod savedPaymentMethod = repository.save(paymentMethod);

            PaymentMethodDTO paymentMethodDTO = mapper.toDTO(savedPaymentMethod);

            return paymentMethodDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(PaymentMethodConstants.PAYMENT_METHOD_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(PaymentMethodConstants.PAYMENT_METHOD_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(PaymentMethodConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(PaymentMethodConstants.EMPTY_ID_LIST);
        }

        List<PaymentMethod> paymentMethods = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (PaymentMethod paymentMethod : paymentMethods) {
            existingIds.add(paymentMethod.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(PaymentMethodConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        try {
            repository.deleteAllByIds(ids);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(PaymentMethodConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethod findEntityById(Long id) {

        Optional<PaymentMethod> paymentMethodOptional = repository.findById(id);

        if (!paymentMethodOptional.isPresent()) {
            throw new ResourceNotFoundException(PaymentMethodConstants.PAYMENT_METHOD_NOT_FOUND);
        }

        PaymentMethod paymentMethod = paymentMethodOptional.get();

        return paymentMethod;
    }
}
