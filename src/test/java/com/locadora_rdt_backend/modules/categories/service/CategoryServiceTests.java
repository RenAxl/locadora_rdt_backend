package com.locadora_rdt_backend.modules.categories.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.constants.CategoryConstants;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryImageDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryInsertDTO;
import com.locadora_rdt_backend.modules.stocks.categories.dto.CategoryUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.categories.mapper.CategoryMapper;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.categories.repository.CategoryRepository;
import com.locadora_rdt_backend.modules.stocks.categories.service.CategoryServiceImpl;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @Mock
    private CategoryRepository repository;

    @Mock
    private CategoryMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private CategoryServiceImpl service;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Ferramentas");
        category.setActive(true);

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Ferramentas");
    }

    @Test
    void findAllPagedShouldReturnPageOfCategories() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Category> categories = new PageImpl<>(Collections.singletonList(category));

        when(repository.find("Ferramentas", pageRequest)).thenReturn(categories);
        when(mapper.toDTO(category)).thenReturn(categoryDTO);

        Page<CategoryDTO> resultado = service.findAllPaged("Ferramentas", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Ferramentas", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Ferramentas", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Ferramentas", pageRequest));
    }

    @Test
    void findByIdShouldReturnCategory() {
        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toDTO(category)).thenReturn(categoryDTO);

        CategoryDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenCategoryDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveCategory() {
        CategoryInsertDTO insertDTO = new CategoryInsertDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(category);
        when(repository.save(category)).thenReturn(category);
        when(mapper.toDTO(category)).thenReturn(categoryDTO);

        CategoryDTO resultado = service.insert(insertDTO);

        assertEquals(categoryDTO, resultado);
        verify(repository).save(category);
        assertEquals("Usuário Teste", category.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        CategoryInsertDTO insertDTO = new CategoryInsertDTO();

        when(mapper.toEntity(insertDTO)).thenReturn(category);
        when(repository.save(category))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateCategory() {
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.getOne(1L)).thenReturn(category);
        when(repository.save(category)).thenReturn(category);
        when(mapper.toDTO(category)).thenReturn(categoryDTO);

        CategoryDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(category, updateDTO);
        assertEquals(categoryDTO, resultado);
        assertEquals("Usuário Teste", category.getUpdatedBy());
    }

    @Test
    void updateShouldThrowExceptionWhenCategoryDoesNotExist() {
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteCategory() {
        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenCategoryIsInUse() {
        doThrow(new DataIntegrityViolationException("Categoria em uso")).when(repository).flush();

        DatabaseException exception = assertThrows(DatabaseException.class, () -> service.delete(1L));

        assertEquals(CategoryConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAllShouldDeleteAllCategories() {
        Category segundaCategoria = new Category();
        segundaCategoria.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(category, segundaCategoria));

        service.deleteAll(Arrays.asList(1L, 2L));

        verify(repository).deleteAllByIds(Arrays.asList(1L, 2L));
        verify(repository).flush();
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAll(Collections.emptyList()));

        verify(repository, never()).deleteAllByIds(any());
    }

    @Test
    void changeActiveStatusShouldChangeStatus() {
        when(repository.updateActiveById(1L, false)).thenReturn(1);

        service.changeActiveStatus(1L, false);

        verify(repository).updateActiveById(1L, false);
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenDatabaseFails() {
        when(repository.updateActiveById(1L, false))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DatabaseException.class,
                () -> service.changeActiveStatus(1L, false));
    }

    @Test
    void getCategoryImageByIdShouldReturnImage() {
        byte[] imagem = new byte[]{1, 2, 3};
        category.setImage(imagem);
        category.setImageContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        CategoryImageDTO resultado = service.getCategoryImageById(1L);

        assertNotNull(resultado);
        assertArrayEquals(imagem, resultado.getImage());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getCategoryImageByIdShouldThrowExceptionWhenCategoryDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCategoryImageById(1L));
    }

    @Test
    void updateImageShouldSaveImage() {
        byte[] image = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", image);

        when(repository.findById(1L)).thenReturn(Optional.of(category));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.updateImage(1L, file);

        assertArrayEquals(image, category.getImage());
        assertEquals("image/png", category.getImageContentType());
        assertEquals("Usuário Teste", category.getUpdatedBy());
        verify(repository).save(category);
    }

    @Test
    void updateImageShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);
        when(repository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(IllegalArgumentException.class, () -> service.updateImage(1L, file));

        verify(repository, never()).save(category);
    }

}
