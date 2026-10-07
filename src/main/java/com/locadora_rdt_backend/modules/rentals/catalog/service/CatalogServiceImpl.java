package com.locadora_rdt_backend.modules.rentals.catalog.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.rentals.catalog.constants.CatalogConstants;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemImageDTO;
import com.locadora_rdt_backend.modules.stocks.items.mapper.ItemMapper;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final ItemRepository repository;
    private final ItemMapper mapper;

    public CatalogServiceImpl(
            ItemRepository repository,
            ItemMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDTO> findAllPaged(String name, Long categoryId, PageRequest pageRequest) {

        String nameFilter = CatalogConstants.EMPTY_NAME_FILTER;

        if (name != null) {
            nameFilter = name.trim();
        }

        Long categoryIdFilter = CatalogConstants.CATEGORY_ID_FILTER_DISABLED;

        if (categoryId != null && categoryId > CatalogConstants.MINIMUM_CATEGORY_ID) {
            categoryIdFilter = categoryId;
        }

        Page<Item> items = repository.findForCatalog(nameFilter, categoryIdFilter, pageRequest);

        Page<ItemDTO> itemsDTO = items.map(item -> mapper.toDTO(item));

        return itemsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDTO findById(Long id) {

        Optional<Item> itemOptional = repository.findById(id);

        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(CatalogConstants.ITEM_NOT_FOUND);
        }

        Item item = itemOptional.get();

        ItemDTO itemDTO = mapper.toDTO(item);

        return itemDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemImageDTO getItemImageById(Long id) {

        Optional<Item> itemOptional = repository.findById(id);

        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(CatalogConstants.ITEM_NOT_FOUND);
        }

        Item item = itemOptional.get();

        byte[] image = item.getImage();

        if (image == null || image.length == 0) {
            return null;
        }

        String imageContentType = item.getImageContentType();

        ItemImageDTO itemImageDTO = new ItemImageDTO(
                image,
                imageContentType
        );

        return itemImageDTO;
    }

}
