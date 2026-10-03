package com.locadora_rdt_backend.modules.stocks.items.service;

import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemImageDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemInsertDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {

    Page<ItemDTO> findAllPaged(String name, PageRequest pageRequest);

    ItemDTO findById(Long id);

    ItemDTO insert(ItemInsertDTO dto);

    ItemDTO update(Long id, ItemUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    ItemImageDTO getItemImageById(Long id);

    void updateImage(Long id, MultipartFile file);

}
