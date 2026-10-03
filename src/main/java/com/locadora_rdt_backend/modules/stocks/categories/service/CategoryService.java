package com.locadora_rdt_backend.modules.stocks.categories.service;

import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryImageDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryInsertDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoryService {

    Page<CategoryDTO> findAllPaged(String name, PageRequest pageRequest);

    CategoryDTO findById(Long id);

    CategoryDTO insert(CategoryInsertDTO dto);

    CategoryDTO update(Long id, CategoryUpdateDTO dto);

    void delete(Long id);

    void deleteAll(List<Long> ids);

    void changeActiveStatus(Long id, boolean active);

    CategoryImageDTO getCategoryImageById(Long id);

    void updateImage(Long id, MultipartFile file);

}
