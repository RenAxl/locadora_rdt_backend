package com.locadora_rdt_backend.modules.stocks.items.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.constants.CategoryConstants;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.categories.repository.CategoryRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.dto.*;
import com.locadora_rdt_backend.modules.stocks.items.mapper.ItemMapper;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository repository;
    private final ItemMapper mapper;
    private final CategoryRepository categoryRepository;
    private final AuthenticationFacade authenticationFacade;

    public ItemServiceImpl(
            ItemRepository repository,
            ItemMapper mapper,
            CategoryRepository categoryRepository,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.categoryRepository = categoryRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemDTO> findAllPaged(String name, PageRequest pageRequest) {

        String nameFilter = "";

        if (name != null) {
            nameFilter = name.trim();
        }

        Page<Item> items = repository.find(nameFilter, pageRequest);

        Page<ItemDTO> itemsDTO = items.map(item -> mapper.toDTO(item));

        return itemsDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDTO findById(Long id) {

        Optional<Item> itemOptional = repository.findById(id);

        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }

        Item item = itemOptional.get();

        ItemDTO itemDTO = mapper.toDTO(item);

        return itemDTO;
    }

    @Override
    @Transactional
    public ItemDTO insert(ItemInsertDTO dto) {

        Item item = mapper.toEntity(dto);

        Optional<Category> categoryOptional = categoryRepository.findById(dto.getCategoryId());

        if (!categoryOptional.isPresent()) {
            throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
        }

        Category category = categoryOptional.get();

        item.setCategory(category);

        item.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        Item savedItem = repository.save(item);

        ItemDTO itemDTO = mapper.toDTO(savedItem);

        return itemDTO;
    }

    @Override
    @Transactional
    public ItemDTO update(Long id, ItemUpdateDTO dto) {

        try {

            Item item = repository.getOne(id);

            Optional<Category> categoryOptional = categoryRepository.findById(dto.getCategoryId());

            if (!categoryOptional.isPresent()) {
                throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
            }

            Category category = categoryOptional.get();

            mapper.updateEntity(item, dto);
            item.setCategory(category);

            item.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            Item savedItem = repository.save(item);

            ItemDTO itemDTO = mapper.toDTO(savedItem);

            return itemDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(ItemConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(ItemConstants.EMPTY_ID_LIST);
        }

        List<Item> items = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (Item item : items) {
            existingIds.add(item.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(ItemConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        try {

            repository.deleteAllByIds(ids);
            repository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(ItemConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
            }

        } catch (DataAccessException e) {

            throw new DatabaseException(ItemConstants.CHANGE_ACTIVE_STATUS_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ItemImageDTO getItemImageById(Long id) {

        Optional<Item> itemOptional = repository.findById(id);

        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
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

    @Override
    @Transactional
    public void updateImage(Long id, MultipartFile file) {

        Optional<Item> itemOptional = repository.findById(id);

        if (!itemOptional.isPresent()) {
            throw new ResourceNotFoundException(ItemConstants.ITEM_NOT_FOUND);
        }

        Item item = itemOptional.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ItemConstants.EMPTY_IMAGE_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !ItemConstants.ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(ItemConstants.INVALID_IMAGE_TYPE);
        }

        if (file.getSize() > ItemConstants.MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException(ItemConstants.IMAGE_TOO_LARGE);
        }

        try {

            byte[] image = file.getBytes();

            item.setImage(image);
            item.setImageContentType(contentType);

        } catch (IOException e) {

            throw new FileException(ItemConstants.IMAGE_READ_ERROR, e);
        }

        item.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        repository.save(item);
    }

}
