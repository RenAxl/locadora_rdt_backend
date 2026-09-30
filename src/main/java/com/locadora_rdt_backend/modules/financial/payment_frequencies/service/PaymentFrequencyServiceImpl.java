package com.locadora_rdt_backend.modules.financial.payment_frequencies.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.constants.PaymentFrequencyConstants;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.*;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.mapper.PaymentFrequencyMapper;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.repository.PaymentFrequencyRepository;
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
public class PaymentFrequencyServiceImpl implements PaymentFrequencyService {

    private final PaymentFrequencyRepository repository;
    private final PaymentFrequencyMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public PaymentFrequencyServiceImpl(
            PaymentFrequencyRepository repository,
            PaymentFrequencyMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentFrequencyDTO> findAllPaged(String frequency, PageRequest pageRequest) {

        if (frequency == null) {
            frequency = "";
        }

        Page<PaymentFrequency> paymentFrequencies = repository.find(frequency.trim(), pageRequest);

        Page<PaymentFrequencyDTO> paymentFrequenciesDTO = paymentFrequencies.map(
                paymentFrequency -> mapper.toDTO(paymentFrequency)
        );

        return paymentFrequenciesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentFrequencyDTO findById(Long id) {

        Optional<PaymentFrequency> paymentFrequencyOptional = repository.findById(id);

        if (!paymentFrequencyOptional.isPresent()) {
            throw new ResourceNotFoundException(PaymentFrequencyConstants.PAYMENT_FREQUENCY_NOT_FOUND);
        }

        PaymentFrequency paymentFrequency = paymentFrequencyOptional.get();

        PaymentFrequencyDTO paymentFrequencyDTO = mapper.toDTO(paymentFrequency);

        return paymentFrequencyDTO;
    }

    @Override
    @Transactional
    public PaymentFrequencyDTO insert(PaymentFrequencyInsertDTO dto) {

        PaymentFrequency paymentFrequency = mapper.toEntity(dto);

        paymentFrequency.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        PaymentFrequency savedPaymentFrequency = repository.save(paymentFrequency);

        PaymentFrequencyDTO paymentFrequencyDTO = mapper.toDTO(savedPaymentFrequency);

        return paymentFrequencyDTO;
    }

    @Override
    @Transactional
    public PaymentFrequencyDTO update(Long id, PaymentFrequencyUpdateDTO dto) {

        try {

            PaymentFrequency paymentFrequency = repository.getOne(id);

            mapper.updateEntity(paymentFrequency, dto);

            paymentFrequency.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            PaymentFrequency savedPaymentFrequency = repository.save(paymentFrequency);

            PaymentFrequencyDTO paymentFrequencyDTO = mapper.toDTO(savedPaymentFrequency);

            return paymentFrequencyDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(PaymentFrequencyConstants.PAYMENT_FREQUENCY_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(PaymentFrequencyConstants.PAYMENT_FREQUENCY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(PaymentFrequencyConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(PaymentFrequencyConstants.EMPTY_ID_LIST);
        }

        List<PaymentFrequency> paymentFrequencies = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (PaymentFrequency paymentFrequency : paymentFrequencies) {
            existingIds.add(paymentFrequency.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(PaymentFrequencyConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        try {
            repository.deleteAllByIds(ids);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(PaymentFrequencyConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentFrequency findEntityById(Long id) {

        Optional<PaymentFrequency> paymentFrequencyOptional = repository.findById(id);

        if (!paymentFrequencyOptional.isPresent()) {
            throw new ResourceNotFoundException(PaymentFrequencyConstants.PAYMENT_FREQUENCY_NOT_FOUND);
        }

        PaymentFrequency paymentFrequency = paymentFrequencyOptional.get();

        return paymentFrequency;
    }

}
