package com.locadora_rdt_backend.modules.rental.catalog.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.rentals.catalog.constants.CatalogConstants;
import com.locadora_rdt_backend.modules.rentals.catalog.service.CatalogServiceImpl;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemImageDTO;
import com.locadora_rdt_backend.modules.stocks.items.mapper.ItemMapper;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CatalogServiceTests {

    @Mock
    private ItemRepository repository;

    @Mock
    private ItemMapper mapper;

    @InjectMocks
    private CatalogServiceImpl service;

    private Item item;
    private ItemDTO itemDTO;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Furadeira");
        item.setActive(true);

        itemDTO = new ItemDTO();
        itemDTO.setId(1L);
        itemDTO.setName("Furadeira");
    }

    @Test
    void findAllPagedShouldReturnPageOfItems() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        Page<Item> items = new PageImpl<>(Collections.singletonList(item), pageRequest, 1);

        when(repository.findForCatalog("Furadeira", -1L, pageRequest)).thenReturn(items);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        Page<ItemDTO> resultado = service.findAllPaged(" Furadeira ", null, pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(8, resultado.getSize());
        assertEquals("Furadeira", resultado.getContent().get(0).getName());
        verify(repository).findForCatalog("Furadeira", -1L, pageRequest);
        verify(mapper).toDTO(item);
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        when(repository.findForCatalog("Furadeira", 2L, pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Furadeira", 2L, pageRequest));

        verify(repository).findForCatalog("Furadeira", 2L, pageRequest);
        verify(mapper, never()).toDTO(item);
    }

    @Test
    void findByIdShouldReturnItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(itemDTO, resultado);
        assertEquals(1L, resultado.getId());
        verify(mapper).toDTO(item);
    }

    @Test
    void findByIdShouldThrowExceptionWhenItemDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(CatalogConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDTO(item);
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
        verify(repository).findById(1L);
    }

    @Test
    void getItemImageByIdShouldThrowExceptionWhenItemDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.getItemImageById(1L));

        assertEquals(CatalogConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(repository).findById(1L);
    }
}
