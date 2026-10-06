package com.locadora_rdt_backend.modules.stocks.stock_movements.service;

import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementInsertDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface StockMovementService {

    Page<StockMovementDTO> findAllPaged(String name, PageRequest pageRequest);

    StockMovementDTO findById(Long id);

    StockMovementDTO insert(StockMovementInsertDTO dto);
}
