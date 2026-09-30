package com.locadora_rdt_backend.modules.financial.payment_frequencies.service;

import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyInsertDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.dto.PaymentFrequencyUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payment_frequencies.model.PaymentFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface PaymentFrequencyService {

    Page<PaymentFrequencyDTO> findAllPaged(String frequency, PageRequest pageRequest);

    PaymentFrequencyDTO findById(Long id);

    PaymentFrequencyDTO insert(PaymentFrequencyInsertDTO dto);

    PaymentFrequencyDTO update(Long id, PaymentFrequencyUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    PaymentFrequency findEntityById(Long id);

}
