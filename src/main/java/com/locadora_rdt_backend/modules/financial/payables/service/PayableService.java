package com.locadora_rdt_backend.modules.financial.payables.service;

import com.locadora_rdt_backend.modules.financial.payables.dto.PayableDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableFilterDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableInsertDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayablePaymentDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableReportDTO;
import com.locadora_rdt_backend.modules.financial.payables.dto.PayableUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

public interface PayableService {

    Page<PayableDTO> findAllPaged(String description, PageRequest pageRequest);

    Page<PayableDTO> findAllPaged(PayableFilterDTO filters, PageRequest pageRequest);

    PayableDTO findById(Long id);

    PayableDTO insert(PayableInsertDTO dto);

    PayableDTO update(Long id, PayableUpdateDTO dto);

    void delete(Long id);

    PayableDTO pay(Long id, PayablePaymentDTO dto);

    PayableReportDTO report(String description, LocalDate startDate, LocalDate endDate, String status, String dateType);
}
