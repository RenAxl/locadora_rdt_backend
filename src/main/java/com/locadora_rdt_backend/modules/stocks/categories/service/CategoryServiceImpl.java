package com.locadora_rdt_backend.modules.stocks.categories.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.FileException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.constants.CategoryConstants;
import com.locadora_rdt_backend.modules.stocks.categories.dto.*;
import com.locadora_rdt_backend.modules.stocks.categories.mapper.CategoryMapper;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.categories.repository.CategoryRepository;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;
    private final AuthenticationFacade authenticationFacade;

    public CategoryServiceImpl(
            CategoryRepository repository,
            CategoryMapper mapper,
            AuthenticationFacade authenticationFacade
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAllPaged(String name, PageRequest pageRequest) {

        String nameFilter = "";

        if (name != null) {
            nameFilter = name.trim();
        }

        Page<Category> categories = repository.find(nameFilter, pageRequest);

        Page<CategoryDTO> categoriesDTO = categories.map(category -> mapper.toDTO(category));

        return categoriesDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {

        Optional<Category> categoryOptional = repository.findById(id);

        if (!categoryOptional.isPresent()) {
            throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
        }

        Category category = categoryOptional.get();

        CategoryDTO categoryDTO = mapper.toDTO(category);

        return categoryDTO;
    }

    @Override
    @Transactional
    public CategoryDTO insert(CategoryInsertDTO dto) {

        Category category = mapper.toEntity(dto);

        category.setCreatedBy(authenticationFacade.getAuthenticatedUsername());

        Category savedCategory = repository.save(category);

        CategoryDTO categoryDTO = mapper.toDTO(savedCategory);

        return categoryDTO;
    }

    @Override
    @Transactional
    public CategoryDTO update(Long id, CategoryUpdateDTO dto) {

        try {

            Category category = repository.getOne(id);

            mapper.updateEntity(category, dto);

            category.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

            Category savedCategory = repository.save(category);

            CategoryDTO categoryDTO = mapper.toDTO(savedCategory);

            return categoryDTO;

        } catch (EntityNotFoundException e) {

            throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(CategoryConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(CategoryConstants.EMPTY_ID_LIST);
        }

        List<Category> categories = repository.findAllById(ids);

        List<Long> existingIds = new ArrayList<>();

        for (Category category : categories) {
            existingIds.add(category.getId());
        }

        if (existingIds.size() != ids.size()) {
            throw new ResourceNotFoundException(CategoryConstants.ONE_OR_MORE_IDS_NOT_FOUND);
        }

        try {

            repository.deleteAllByIds(ids);
            repository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new DatabaseException(CategoryConstants.DATABASE_INTEGRITY_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void changeActiveStatus(Long id, boolean active) {

        try {

            int updated = repository.updateActiveById(id, active);

            if (updated == 0) {
                throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
            }

        } catch (DataAccessException e) {

            throw new DatabaseException(CategoryConstants.CHANGE_ACTIVE_STATUS_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryImageDTO getCategoryImageById(Long id) {

        Optional<Category> categoryOptional = repository.findById(id);

        if (!categoryOptional.isPresent()) {
            throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
        }

        Category category = categoryOptional.get();

        byte[] image = category.getImage();

        if (image == null || image.length == 0) {
            return null;
        }

        String imageContentType = category.getImageContentType();

        CategoryImageDTO categoryImageDTO = new CategoryImageDTO(
                image,
                imageContentType
        );

        return categoryImageDTO;
    }

    @Override
    @Transactional
    public void updateImage(Long id, MultipartFile file) {

        Optional<Category> categoryOptional = repository.findById(id);

        if (!categoryOptional.isPresent()) {
            throw new ResourceNotFoundException(CategoryConstants.CATEGORY_NOT_FOUND);
        }

        Category category = categoryOptional.get();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(CategoryConstants.EMPTY_IMAGE_FILE);
        }

        String contentType = file.getContentType();

        if (contentType == null || !CategoryConstants.ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(CategoryConstants.INVALID_IMAGE_TYPE);
        }

        if (file.getSize() > CategoryConstants.MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException(CategoryConstants.IMAGE_TOO_LARGE);
        }

        try {

            byte[] image = file.getBytes();

            category.setImage(image);
            category.setImageContentType(contentType);

        } catch (IOException e) {

            throw new FileException(CategoryConstants.IMAGE_READ_ERROR, e);
        }

        category.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());

        repository.save(category);
    }

}
