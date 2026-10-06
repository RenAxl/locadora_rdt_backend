package com.locadora_rdt_backend.modules.stocks.items.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.categories.repository.CategoryRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemImageDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemInsertDTO;
import com.locadora_rdt_backend.modules.stocks.items.dto.ItemUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.items.mapper.ItemMapper;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTests {

    @Mock
    private ItemRepository repository;

    @Mock
    private ItemMapper mapper;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    @InjectMocks
    private ItemServiceImpl service;

    private Category category;
    private Item item;
    private ItemDTO itemDTO;
    private StockBalance balance;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(3L);
        category.setName("Ferramentas");
        category.setActive(true);

        item = new Item();
        item.setId(1L);
        item.setName("Furadeira");
        item.setDescription("Furadeira elétrica");
        item.setCategory(category);
        item.setPrice(BigDecimal.TEN);
        item.setActive(true);

        itemDTO = new ItemDTO();
        itemDTO.setId(1L);
        itemDTO.setName("Furadeira");

        balance = new StockBalance();
        balance.setId(10L);
        balance.setItem(item);
        balance.setMinimumQuantity(0);
    }

    @Test
    void findAllPagedShouldReturnPageOfItems() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Item> items = new PageImpl<>(Collections.singletonList(item));

        when(repository.find("Furadeira", pageRequest)).thenReturn(items);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        Page<ItemDTO> resultado = service.findAllPaged("  Furadeira  ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Furadeira", resultado.getContent().get(0).getName());
        verify(repository).find("Furadeira", pageRequest);
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("Furadeira", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged("Furadeira", pageRequest));
    }

    @Test
    void findByIdShouldReturnItem() {
        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Furadeira", resultado.getName());
    }

    @Test
    void findByIdShouldThrowExceptionWhenItemDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(ItemConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void insertShouldSaveItemAndCreateStockBalance() {
        ItemInsertDTO insertDTO = new ItemInsertDTO();
        insertDTO.setName("Furadeira");
        insertDTO.setDescription("Furadeira elétrica");
        insertDTO.setCategoryId(3L);
        insertDTO.setPrice(BigDecimal.TEN);

        when(mapper.toEntity(insertDTO)).thenReturn(item);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(item)).thenReturn(item);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.insert(insertDTO);

        assertEquals(itemDTO, resultado);
        assertEquals(category, item.getCategory());
        assertEquals("Usuário Teste", item.getCreatedBy());
        verify(repository).save(item);

        ArgumentCaptor<StockBalance> captor = ArgumentCaptor.forClass(StockBalance.class);
        verify(stockBalanceRepository).save(captor.capture());
        StockBalance savedBalance = captor.getValue();

        assertEquals(item, savedBalance.getItem());
        assertEquals(0, savedBalance.getMinimumQuantity());
        assertEquals("Usuário Teste", savedBalance.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenRepositoryFails() {
        ItemInsertDTO insertDTO = new ItemInsertDTO();
        insertDTO.setCategoryId(3L);

        when(mapper.toEntity(insertDTO)).thenReturn(item);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(repository.save(item))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));

        verify(stockBalanceRepository, never()).save(any());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void updateShouldUpdateItem() {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        updateDTO.setName("Furadeira profissional");
        updateDTO.setDescription("Furadeira elétrica profissional");
        updateDTO.setCategoryId(3L);
        updateDTO.setPrice(BigDecimal.TEN);

        when(repository.getOne(1L)).thenReturn(item);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(item)).thenReturn(item);
        when(mapper.toDTO(item)).thenReturn(itemDTO);

        ItemDTO resultado = service.update(1L, updateDTO);

        assertEquals(itemDTO, resultado);
        assertEquals(category, item.getCategory());
        assertEquals("Usuário Teste", item.getUpdatedBy());
        verify(mapper).updateEntity(item, updateDTO);
        verify(repository).save(item);
        verify(stockBalanceRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowExceptionWhenItemDoesNotExist() {
        ItemUpdateDTO updateDTO = new ItemUpdateDTO();
        when(repository.getOne(1L)).thenThrow(new EntityNotFoundException());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L, updateDTO));

        assertEquals(ItemConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(repository, never()).save(any());
        verify(mapper, never()).updateEntity(any(), any());
    }

    @Test
    void deleteShouldDeleteItemAndStockBalance() {
        when(stockBalanceRepository.findByItemId(1L)).thenReturn(Optional.of(balance));

        service.delete(1L);

        verify(stockBalanceRepository).delete(balance);
        verify(stockBalanceRepository).flush();
        verify(repository).deleteById(1L);
        verify(repository).flush();
    }

    @Test
    void deleteShouldThrowExceptionWhenIdDoesNotExist() {
        when(stockBalanceRepository.findByItemId(1L)).thenReturn(Optional.empty());
        doThrow(new EmptyResultDataAccessException(1)).when(repository).deleteById(1L);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.delete(1L));

        assertEquals(ItemConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(repository, never()).flush();
        verify(stockBalanceRepository, never()).delete(any());
    }

    @Test
    void deleteAllShouldDeleteAllItemsAndStockBalances() {
        Item segundoItem = new Item();
        segundoItem.setId(2L);

        StockBalance segundoSaldo = new StockBalance();
        segundoSaldo.setId(20L);
        segundoSaldo.setItem(segundoItem);
        List<Long> ids = Arrays.asList(1L, 2L);

        when(repository.findAllById(ids)).thenReturn(Arrays.asList(item, segundoItem));
        when(stockBalanceRepository.findByItemId(1L)).thenReturn(Optional.of(balance));
        when(stockBalanceRepository.findByItemId(2L)).thenReturn(Optional.of(segundoSaldo));

        service.deleteAll(ids);

        verify(stockBalanceRepository).delete(balance);
        verify(stockBalanceRepository).delete(segundoSaldo);
        verify(stockBalanceRepository, times(2)).flush();
        verify(repository).deleteAllByIds(ids);
        verify(repository).flush();
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.deleteAll(Collections.emptyList()));

        assertEquals(ItemConstants.EMPTY_ID_LIST, exception.getMessage());
        verify(repository, never()).findAllById(any());
        verify(repository, never()).deleteAllByIds(any());
        verify(stockBalanceRepository, never()).delete(any());
    }

    @Test
    void changeActiveStatusShouldChangeStatus() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.updateActiveById(1L, false, "Usuário Teste")).thenReturn(1);

        service.changeActiveStatus(1L, false);

        verify(repository).updateActiveById(1L, false, "Usuário Teste");
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenDatabaseFails() {
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.updateActiveById(1L, false, "Usuário Teste"))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.changeActiveStatus(1L, false));

        assertEquals(ItemConstants.CHANGE_ACTIVE_STATUS_ERROR, exception.getMessage());
    }

    @Test
    void getItemImageByIdShouldReturnImage() {
        byte[] imagem = new byte[]{1, 2, 3};
        item.setImage(imagem);
        item.setImageContentType("image/png");
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        ItemImageDTO resultado = service.getItemImageById(1L);

        assertNotNull(resultado);
        assertArrayEquals(imagem, resultado.getImage());
        assertEquals("image/png", resultado.getContentType());
    }

    @Test
    void getItemImageByIdShouldThrowExceptionWhenItemDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.getItemImageById(1L));

        assertEquals(ItemConstants.ITEM_NOT_FOUND, exception.getMessage());
    }

    @Test
    void updateImageShouldSaveImage() {
        byte[] imagem = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", imagem);

        when(repository.findById(1L)).thenReturn(Optional.of(item));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.updateImage(1L, file);

        assertArrayEquals(imagem, item.getImage());
        assertEquals("image/png", item.getImageContentType());
        assertEquals("Usuário Teste", item.getUpdatedBy());
        verify(repository).save(item);
    }

    @Test
    void updateImageShouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "imagem.png", "image/png", new byte[0]);
        when(repository.findById(1L)).thenReturn(Optional.of(item));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.updateImage(1L, file));

        assertEquals(ItemConstants.EMPTY_IMAGE_FILE, exception.getMessage());
        verify(repository, never()).save(any());
    }
}
