package com.locadora_rdt_backend.modules.stocks.stock_balances.service;

import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface StockBalanceService {

    Page<StockBalanceDTO> findAllPaged(String name, PageRequest pageRequest);

    StockBalanceDTO findById(Long id);

    StockBalanceDTO findByItemId(Long itemId);

    StockBalanceDTO updateMinimum(Long id, StockBalanceMinimumUpdateDTO dto);

}
