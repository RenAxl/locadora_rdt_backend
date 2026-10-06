package com.locadora_rdt_backend.modules.stocks.item_units.service;

import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitStatusUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitInsertDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface ItemUnitService {

    Page<ItemUnitDTO> findAllPaged(String name, Long itemId, Boolean active, PageRequest pageRequest);

    ItemUnitDTO findById(Long id);

    ItemUnitDTO insert(ItemUnitInsertDTO dto);

    ItemUnitDTO update(Long id, ItemUnitUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    ItemUnitDTO updateStatus(Long id, ItemUnitStatusUpdateDTO dto);

    ItemUnitDTO changeMaintenanceStatus(Long id, boolean maintenance);

}
