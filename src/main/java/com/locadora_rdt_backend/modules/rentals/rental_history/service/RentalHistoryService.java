package com.locadora_rdt_backend.modules.rentals.rental_history.service;

import com.locadora_rdt_backend.modules.rentals.rental_history.dto.RentalHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface RentalHistoryService {

    Page<RentalHistoryDTO> findAllPaged(PageRequest pageRequest);

}
