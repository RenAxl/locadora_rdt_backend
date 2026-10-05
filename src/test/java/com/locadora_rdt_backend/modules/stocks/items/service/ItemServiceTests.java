package com.locadora_rdt_backend.modules.stocks.items.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.categories.repository.CategoryRepository;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemImageDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemInsertDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.items.mapper.ItemMapper;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.items.service.ItemServiceImpl;
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
import java.math.BigDecimal;
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
public class ItemServiceTests {

    @Mock
    private ItemRepository repository;

    @Mock
    private ItemMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ItemServiceImpl service;

    private Item item;
    private Category category;
    private ItemDTO itemDTO;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("PlayStation");
        item.setDescription("Console para locação");
        item.setPrice(new BigDecimal("50.00"));
        item.setActive(true);

        category = new Category();
        category.setId(2L);
        category.setName("Console");
        item.setCategory(category);

        itemDTO = new ItemDTO();
        itemDTO.setId(1L);
        itemDTO.setName("PlayStation");
    }

    @Test
    void findAllPagedShouldReturnPageOfItems() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Item> items = new PageImpl<>(Collections.singletonList(item));

        when(repository.find("PlayStation", pageRequest)).thenReturn(items);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        Page<ItemDTO> resultado = service.findAllPaged("  PlayStation  ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("PlayStation", resultado.getContent().get(0).getName());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("PlayStation", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("PlayStation", pageRequest));
    }

    @Test
    void findByIdShouldReturnItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenItemDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveItem() {
        ItemInsertDTO insertDTO = new ItemInsertDTO();
        insertDTO.setName("PlayStation");
        insertDTO.setDescription("Console para locação");
        insertDTO.setCategoryId(2L);
        insertDTO.setPrice(new BigDecimal("50.00"));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(mapper.toEntity(insertDTO)).thenReturn(item);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(repository.save(item)).thenReturn(item);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.insert(insertDTO);

        assertEquals(itemDTO, resultado);
        verify(repository).save(item);
        assertEquals("Usuário Teste", item.getCreatedBy());
        assertEquals(category, item.getCategory());
        assertEquals(true, item.getActive());
    }

    @Test
    void insertShouldThrowExceptionWhenNameAlreadyExists() {
        ItemInsertDTO insertDTO = new ItemInsertDTO();
        insertDTO.setName("PlayStation");
        insertDTO.setDescription("Console para locação");
        insertDTO.setCategoryId(2L);
        insertDTO.setPrice(new BigDecimal("50.00"));

        when(mapper.toEntity(insertDTO)).thenReturn(item);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(repository.save(item))
                .thenThrow(new DataIntegrityViolationException("Nome já existe"));

        assertThrows(DataIntegrityViolationException.class, () -> service.insert(insertDTO));
    }

    @Test
    void updateShouldUpdateItem() {
        item.setActive(false);

        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("PlayStation atualizado");
        updateDTO.setDescription("Console com controle");
        updateDTO.setCategoryId(2L);
        updateDTO.setPrice(new BigDecimal("60.00"));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        when(repository.getOne(1L)).thenReturn(item);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(repository.save(item)).thenReturn(item);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(item, updateDTO);
        assertEquals(itemDTO, resultado);
        assertEquals("Usuário Teste", item.getUpdatedBy());
        assertEquals(category, item.getCategory());
        assertEquals(false, item.getActive());
    }

    @Test
    void updateShouldThrowExceptionWhenItemDoesNotExist() {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("PlayStation atualizado");
        updateDTO.setDescription("Console com controle");
        updateDTO.setCategoryId(2L);
        updateDTO.setPrice(new BigDecimal("60.00"));
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> service.update(1L, updateDTO));
    }

    @Test
    void deleteShouldDeleteItem() {
        service.delete(1L);

        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenItemHasRelatedRecords() {
        doThrow(new DataIntegrityViolationException("Item possui vínculos")).when(repository).flush();

        assertThrows(DatabaseException.class, () -> service.delete(1L));

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteAllShouldDeleteAllItems() {
        Item segundoItem = new Item();
        segundoItem.setId(2L);

        when(repository.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(Arrays.asList(item, segundoItem));

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
    void getItemImageByIdShouldReturnImage() {
        byte[] image = new byte[]{1, 2, 3};
        item.setImage(image);
        item.setImageContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        ItemImageDTO resultado = service.getItemImageById(1L);

        assertNotNull(resultado);
        assertArrayEquals(image, resultado.getImage());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getItemImageByIdShouldThrowExceptionWhenItemDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getItemImageById(1L));
    }

    @Test
    void updateImageShouldSaveImage() {
        byte[] image = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", image);

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.updateImage(1L, file);

        assertArrayEquals(image, item.getImage());
        assertEquals("image/png", item.getImageContentType());
        assertEquals("Usuário Teste", item.getUpdatedBy());
        verify(repository).save(item);
    }

    @Test
    void updateImageShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", new byte[0]);
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class, () -> service.updateImage(1L, file));

        verify(repository, never()).save(item);
    }

}
