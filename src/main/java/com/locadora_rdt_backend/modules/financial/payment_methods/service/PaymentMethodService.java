package com.locadora_rdt_backend.modules.financial.payment_methods.service;

import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodInsertDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.dto.PaymentMethodUpdateDTO;
import com.locadora_rdt_backend.modules.financial.payment_methods.model.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface PaymentMethodService {

    Page<PaymentMethodDTO> findAllPaged(String name, PageRequest pageRequest);

    PaymentMethodDTO findById(Long id);

    PaymentMethodDTO insert(PaymentMethodInsertDTO dto);

    PaymentMethodDTO update(Long id, PaymentMethodUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    PaymentMethod findEntityById(Long id);
}
