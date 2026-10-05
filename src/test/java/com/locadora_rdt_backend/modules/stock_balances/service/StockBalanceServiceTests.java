package com.locadora_rdt_backend.modules.stock_balances.service;

import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.constants.StockBalanceConstants;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceMinimumUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.dto.StockBalanceUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.stock_balances.mapper.StockBalanceMapper;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.service.StockBalanceServiceImpl;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
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
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @InjectMocks
    private StockBalanceServiceImpl service;

    private Item item;
    private StockBalance stockBalance;
    private StockBalanceDTO stockBalanceDTO;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(2L);
        item.setName("Console");

        stockBalance = new StockBalance();
        stockBalance.setId(1L);
        stockBalance.setVersion(3L);
        stockBalance.setItem(item);
        stockBalance.setTotalQuantity(5);
        stockBalance.setReservedQuantity(2);
        stockBalance.setUnavailableQuantity(1);
        stockBalance.setMinimumQuantity(2);

        stockBalanceDTO = new StockBalanceDTO();
        stockBalanceDTO.setId(1L);
        stockBalanceDTO.setVersion(3L);
        stockBalanceDTO.setItemId(2L);
        stockBalanceDTO.setItemName("Console");
    }

    @Test
    void findAllPagedShouldReturnPageOfStockBalances() {
        PageRequest pageRequest = PageRequest.of(1, 10, Sort.Direction.DESC, "totalQuantity");
        PageRequest normalizedPageRequest = PageRequest.of(1, 10, Sort.Direction.DESC, "total_quantity");
        Page<StockBalance> stockBalances = new PageImpl<>(
                Collections.singletonList(stockBalance), normalizedPageRequest, 11
        );

        when(repository.find("Console", normalizedPageRequest)).thenReturn(stockBalances);
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(4L);
        when(itemUnitRepository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(itemUnitRepository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(mapper.toDTO(stockBalance)).thenReturn(stockBalanceDTO);

        Page<StockBalanceDTO> resultado = service.findAllPaged(" Console ", pageRequest);

        assertEquals(11, resultado.getTotalElements());
        assertEquals(1, resultado.getNumber());
        assertEquals(pageRequest.getSort(), resultado.getSort());
        assertEquals("Console", resultado.getContent().get(0).getItemName());
        assertEquals(4, stockBalance.getTotalQuantity());
        assertEquals(1, stockBalance.getReservedQuantity());
        assertEquals(1, stockBalance.getUnavailableQuantity());
        verify(repository, never()).save(any());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenRepositoryFails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(repository.find("", pageRequest))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class,
                () -> service.findAllPaged(null, pageRequest));

        verify(mapper, never()).toDTO(any());
    }

    @Test
    void findByIdShouldReturnStockBalance() {
        when(repository.findById(1L)).thenReturn(Optional.of(stockBalance));
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(5L);
        when(itemUnitRepository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(2L);
        when(itemUnitRepository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(mapper.toDTO(stockBalance)).thenReturn(stockBalanceDTO);

        StockBalanceDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(3L, resultado.getVersion());
        assertEquals(5, stockBalance.getTotalQuantity());
        assertEquals(2, stockBalance.getReservedQuantity());
        assertEquals(1, stockBalance.getUnavailableQuantity());
        verify(repository, never()).save(any());
    }

    @Test
    void findByIdShouldThrowExceptionWhenStockBalanceDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(StockBalanceConstants.STOCK_BALANCE_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void findByItemIdShouldReturnStockBalance() {
        when(repository.findByItemId(2L)).thenReturn(Optional.of(stockBalance));
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(3L);
        when(itemUnitRepository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(itemUnitRepository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(mapper.toDTO(stockBalance)).thenReturn(stockBalanceDTO);

        StockBalanceDTO resultado = service.findByItemId(2L);

        assertEquals(stockBalanceDTO, resultado);
        assertEquals(2L, resultado.getItemId());
        assertEquals(3, stockBalance.getTotalQuantity());
        assertEquals(1, stockBalance.getReservedQuantity());
        assertEquals(1, stockBalance.getUnavailableQuantity());
        verify(repository, never()).save(any());
    }

    @Test
    void findByItemIdShouldThrowExceptionWhenStockBalanceDoesNotExist() {
        when(repository.findByItemId(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findByItemId(2L));

        verify(mapper, never()).toDTO(any());
    }

    @Test
    void updateMinimumShouldUpdateMinimumQuantity() {
        StockBalanceMinimumUpdateDTO updateDTO = new StockBalanceMinimumUpdateDTO();
        updateDTO.setMinimumQuantity(3);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(stockBalance));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(5L);
        when(itemUnitRepository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(2L);
        when(itemUnitRepository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(repository.save(stockBalance)).thenReturn(stockBalance);
        when(mapper.toDTO(stockBalance)).thenReturn(stockBalanceDTO);

        StockBalanceDTO resultado = service.updateMinimum(1L, updateDTO);

        verify(mapper).updateEntity(stockBalance, updateDTO);
        verify(repository).save(stockBalance);
        verify(itemUnitRepository, never()).save(any());
        assertEquals(stockBalanceDTO, resultado);
        assertEquals("Usuário Teste", stockBalance.getUpdatedBy());
        assertEquals(5, stockBalance.getTotalQuantity());
    }

    @Test
    void updateMinimumShouldThrowExceptionWhenStockBalanceDoesNotExist() {
        StockBalanceMinimumUpdateDTO updateDTO = new StockBalanceMinimumUpdateDTO();
        updateDTO.setMinimumQuantity(3);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateMinimum(1L, updateDTO));

        verify(repository, never()).save(any());
        verify(mapper, never()).updateEntity(any(), any(StockBalanceMinimumUpdateDTO.class));
    }

    @Test
    void updateShouldUpdateStockBalanceAndPhysicalUnits() {
        ItemUnit rentedUnit = new ItemUnit();
        rentedUnit.setId(3L);
        rentedUnit.setItem(item);
        rentedUnit.setStatus("RESERVED");
        rentedUnit.setActive(true);

        ItemUnit manualUnit = new ItemUnit();
        manualUnit.setId(4L);
        manualUnit.setItem(item);
        manualUnit.setStatus("RESERVED");
        manualUnit.setActive(true);

        StockBalanceUpdateDTO updateDTO = new StockBalanceUpdateDTO();
        updateDTO.setTotalQuantity(1);
        updateDTO.setReservedQuantity(1);
        updateDTO.setUnavailableQuantity(0);
        updateDTO.setMinimumQuantity(2);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(stockBalance));
        when(itemUnitRepository.findActiveByItemIdForUpdate(2L))
                .thenReturn(Arrays.asList(rentedUnit, manualUnit));
        when(itemUnitRepository.findByStatusForUpdate(2L, "RESERVED", 1, 0L))
                .thenReturn(Collections.singletonList(manualUnit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(1L);
        when(itemUnitRepository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(itemUnitRepository.countUnavailableByItemId(2L)).thenReturn(0L);
        when(repository.save(stockBalance)).thenReturn(stockBalance);
        when(mapper.toDTO(stockBalance)).thenReturn(stockBalanceDTO);

        StockBalanceDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(stockBalance, updateDTO);
        verify(repository).save(stockBalance);
        verify(itemUnitRepository, times(2)).save(manualUnit);
        verify(itemUnitRepository, never()).save(rentedUnit);
        assertEquals(stockBalanceDTO, resultado);
        assertEquals("RESERVED", rentedUnit.getStatus());
        assertTrue(rentedUnit.getActive());
        assertEquals("AVAILABLE", manualUnit.getStatus());
        assertFalse(manualUnit.getActive());
        assertEquals("Usuário Teste", manualUnit.getUpdatedBy());
        assertEquals("Usuário Teste", stockBalance.getUpdatedBy());
        assertEquals(1, stockBalance.getTotalQuantity());
        assertEquals(1, stockBalance.getReservedQuantity());
        assertEquals(0, stockBalance.getUnavailableQuantity());
    }

    @Test
    void updateShouldThrowExceptionWhenUnitHasActiveRental() {
        ItemUnit rentedUnit = new ItemUnit();
        rentedUnit.setId(3L);
        rentedUnit.setItem(item);
        rentedUnit.setStatus("RESERVED");
        rentedUnit.setActive(true);

        StockBalanceUpdateDTO updateDTO = new StockBalanceUpdateDTO();
        updateDTO.setTotalQuantity(1);
        updateDTO.setReservedQuantity(0);
        updateDTO.setUnavailableQuantity(0);
        updateDTO.setMinimumQuantity(2);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(stockBalance));
        when(itemUnitRepository.findActiveByItemIdForUpdate(2L))
                .thenReturn(Collections.singletonList(rentedUnit));
        when(itemUnitRepository.findByStatusForUpdate(2L, "RESERVED", 1, 0L))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.update(1L, updateDTO));

        assertEquals(StockBalanceConstants.ACTIVE_RENTAL_UNITS_CANNOT_BE_RELEASED, exception.getMessage());
        assertEquals("RESERVED", rentedUnit.getStatus());
        assertTrue(rentedUnit.getActive());
        verify(repository, never()).save(any());
        verify(itemUnitRepository, never()).save(any());
        verify(mapper, never()).updateEntity(any(), any(StockBalanceUpdateDTO.class));
    }
}
