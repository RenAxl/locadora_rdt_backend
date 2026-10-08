package com.locadora_rdt_backend.modules.financial.receivables.service;

import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableFilterDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableInsertDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivablePaymentDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableReportDTO;
import com.locadora_rdt_backend.modules.financial.receivables.dto.ReceivableUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

public interface ReceivableService {

    Page<ReceivableDTO> findAllPaged(String description, PageRequest pageRequest);

    Page<ReceivableDTO> findAllPaged(ReceivableFilterDTO filters, PageRequest pageRequest);

    ReceivableDTO findById(Long id);

    ReceivableDTO insert(ReceivableInsertDTO dto);

    ReceivableDTO update(Long id, ReceivableUpdateDTO dto);

    void delete(Long id);

    ReceivableDTO pay(Long id, ReceivablePaymentDTO dto);

    ReceivableReportDTO report(String description, LocalDate startDate, LocalDate endDate, String status, String dateType);

    byte[] receipt(Long id);

    byte[] fiscalCoupon(Long id);
}
