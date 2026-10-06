package com.locadora_rdt_backend.modules.stocks.stock_balances.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.StockQuantitySummary;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.stock_balances.constants.StockBalanceConstants;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.mapper.StockBalanceMapper;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockBalanceServiceTests {

    @Mock
    private StockBalanceRepository repository;

    @Mock
    private StockBalanceMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private ItemUnitRepository itemUnitRepository;

    @Mock
    private StockQuantitySummary quantities;

    @InjectMocks
    private StockBalanceServiceImpl service;

    private StockBalance balance;
    private StockBalanceDTO balanceDTO;

    @BeforeEach
    void setUp() {
        Item item = new Item();
        item.setId(2L);
        item.setName("Furadeira");

        balance = new StockBalance();
        balance.setId(1L);
        balance.setItem(item);
        balance.setMinimumQuantity(3);

        balanceDTO = new StockBalanceDTO();
        balanceDTO.setId(1L);
        balanceDTO.setItemId(2L);
        balanceDTO.setItemName("Furadeira");
        balanceDTO.setMinimumQuantity(3);
    }

    @Test
    void findAllPagedShouldReturnPageOfStockBalances() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "minimumQuantity");
        PageRequest normalizedPageRequest = PageRequest.of(0, 10, Sort.Direction.DESC, "minimum_quantity");
        Page<StockBalance> balances = new PageImpl<>(Collections.singletonList(balance), normalizedPageRequest, 1);

        when(repository.find("Furadeira", normalizedPageRequest)).thenReturn(balances);
        when(itemUnitRepository.summarizeByItemId(2L)).thenReturn(quantities);
        when(mapper.toDTO(balance, quantities)).thenReturn(balanceDTO);

        Page<StockBalanceDTO> resultado = service.findAllPaged("  Furadeira  ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Furadeira", resultado.getContent().get(0).getItemName());
        assertEquals(pageRequest, resultado.getPageable());
        verify(repository).find("Furadeira", normalizedPageRequest);
        verify(itemUnitRepository).summarizeByItemId(2L);
        verify(mapper).toDTO(balance, quantities);
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenSortIsInvalid() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.Direction.ASC, "totalQuantity");

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.findAllPaged("Furadeira", pageRequest));

        assertEquals(StockBalanceConstants.INVALID_SORT, exception.getMessage());
        verify(repository, never()).find(any(), any());
        verify(itemUnitRepository, never()).summarizeByItemId(any());
    }

    @Test
    void findByIdShouldReturnStockBalance() {
        when(repository.findById(1L)).thenReturn(Optional.of(balance));
        when(itemUnitRepository.summarizeByItemId(2L)).thenReturn(quantities);
        when(mapper.toDTO(balance, quantities)).thenReturn(balanceDTO);

        StockBalanceDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Furadeira", resultado.getItemName());
        verify(itemUnitRepository).summarizeByItemId(2L);
        verify(mapper).toDTO(balance, quantities);
    }

    @Test
    void findByIdShouldThrowExceptionWhenStockBalanceDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND, exception.getMessage());
        verify(itemUnitRepository, never()).summarizeByItemId(any());
        verify(mapper, never()).toDTO(any(), any());
    }

    @Test
    void findByItemIdShouldReturnStockBalance() {
        when(repository.findByItemId(2L)).thenReturn(Optional.of(balance));
        when(itemUnitRepository.summarizeByItemId(2L)).thenReturn(quantities);
        when(mapper.toDTO(balance, quantities)).thenReturn(balanceDTO);

        StockBalanceDTO resultado = service.findByItemId(2L);

        assertNotNull(resultado);
        assertEquals(2L, resultado.getItemId());
        assertEquals("Furadeira", resultado.getItemName());
        verify(repository).findByItemId(2L);
        verify(itemUnitRepository).summarizeByItemId(2L);
        verify(mapper).toDTO(balance, quantities);
    }

    @Test
    void findByItemIdShouldThrowExceptionWhenStockBalanceDoesNotExist() {
        when(repository.findByItemId(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findByItemId(2L));

        assertEquals(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND, exception.getMessage());
        verify(itemUnitRepository, never()).summarizeByItemId(any());
        verify(mapper, never()).toDTO(any(), any());
    }

    @Test
    void updateMinimumShouldUpdateMinimumQuantity() {
        StockBalanceMinimumUpdateDTO updateDTO = new StockBalanceMinimumUpdateDTO();
        updateDTO.setMinimumQuantity(5);
        balanceDTO.setMinimumQuantity(5);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(balance));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(balance)).thenReturn(balance);
        when(itemUnitRepository.summarizeByItemId(2L)).thenReturn(quantities);
        when(mapper.toDTO(balance, quantities)).thenReturn(balanceDTO);

        StockBalanceDTO resultado = service.updateMinimum(1L, updateDTO);

        assertEquals(balanceDTO, resultado);
        assertEquals(5, resultado.getMinimumQuantity());
        assertEquals("Usuário Teste", balance.getUpdatedBy());
        verify(mapper).updateEntity(balance, updateDTO);
        verify(repository).save(balance);
        verify(itemUnitRepository).summarizeByItemId(2L);
        verify(mapper).toDTO(balance, quantities);
        verify(itemUnitRepository, never()).save(any());
        verify(itemUnitRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateMinimumShouldThrowExceptionWhenMinimumQuantityIsNegative() {
        StockBalanceMinimumUpdateDTO updateDTO = new StockBalanceMinimumUpdateDTO();
        updateDTO.setMinimumQuantity(-1);

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.updateMinimum(1L, updateDTO));

        assertEquals(StockBalanceConstants.MINIMUM_QUANTITY_MINIMUM, exception.getMessage());
        verify(repository, never()).findByIdForUpdate(any());
        verify(repository, never()).save(any());
        verify(mapper, never()).updateEntity(any(), any());
        verify(itemUnitRepository, never()).summarizeByItemId(any());
    }
}
