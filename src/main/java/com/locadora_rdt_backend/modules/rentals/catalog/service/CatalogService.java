package com.locadora_rdt_backend.modules.rentals.catalog.service;

import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemImageDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface CatalogService {

    Page<ItemDTO> findAllPaged(String name, Long categoryId, PageRequest pageRequest);

    ItemDTO findById(Long id);

    ItemImageDTO getItemImageById(Long id);

}
