package com.locadora_rdt_backend.modules.stocks.item_units.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitInsertDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.mapper.ItemUnitMapper;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.service.ItemUnitServiceImpl;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemUnitServiceTests {

    @Mock
    private ItemUnitRepository repository;

    @Mock
    private ItemUnitMapper mapper;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private ItemUnitServiceImpl service;

    private Item item;
    private ItemUnit unit;
    private ItemUnitDTO unitDTO;
    private StockBalance balance;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(2L);
        item.setName("PlayStation");
        item.setActive(true);

        unit = new ItemUnit();
        unit.setId(1L);
        unit.setItem(item);
        unit.setAssetCode("PS5-001");
        unit.setStatus("AVAILABLE");
        unit.setConditionStatus("GOOD");
        unit.setActive(true);

        unitDTO = new ItemUnitDTO();
        unitDTO.setId(1L);
        unitDTO.setAssetCode("PS5-001");

        balance = new StockBalance();
        balance.setId(1L);
        balance.setItem(item);
        balance.setTotalQuantity(4);
        balance.setReservedQuantity(1);
        balance.setUnavailableQuantity(1);
        balance.setMinimumQuantity(3);
    }

    @Test
    void findAllPagedShouldReturnPageOfItemUnits() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ItemUnit> units = new PageImpl<>(Collections.singletonList(unit));
        when(itemRepository.existsById(2L)).thenReturn(true);
        when(repository.find("PS5", 2L, pageRequest)).thenReturn(units);
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        Page<ItemUnitDTO> resultado = service.findAllPaged("  PS5  ", 2L, pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("PS5-001", resultado.getContent().get(0).getAssetCode());
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenItemDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(itemRepository.existsById(2L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.findAllPaged("", 2L, pageRequest));

        verify(repository, never()).find(any(), any(), any());
    }

    @Test
    void findByIdShouldReturnItemUnit() {
        when(repository.findById(1L)).thenReturn(Optional.of(unit));
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void findByIdShouldThrowExceptionWhenItemUnitDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }

    @Test
    void insertShouldSaveItemUnitAndUpdateStockBalance() {
        ItemUnitInsertDTO insertDTO = new ItemUnitInsertDTO();
        insertDTO.setItemId(2L);
        insertDTO.setAssetCode("PS5-001");
        insertDTO.setConditionStatus("GOOD");

        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(unit);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.saveAndFlush(unit)).thenReturn(unit);
        when(stockBalanceRepository.findByItemId(2L)).thenReturn(Optional.of(balance));
        when(repository.countByItemIdAndActiveTrue(2L)).thenReturn(5L);
        when(repository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(repository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.insert(insertDTO);

        assertEquals(unitDTO, resultado);
        assertEquals(item, unit.getItem());
        assertEquals("Usuário Teste", unit.getCreatedBy());
        assertEquals("AVAILABLE", unit.getStatus());
        verify(repository).saveAndFlush(unit);
        verify(stockBalanceRepository).saveAndFlush(balance);
        assertEquals(5, balance.getTotalQuantity());
        assertEquals(1, balance.getReservedQuantity());
        assertEquals(1, balance.getUnavailableQuantity());
        assertEquals(3, balance.getMinimumQuantity());
        assertEquals("Usuário Teste", balance.getUpdatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenAssetCodeAlreadyExists() {
        ItemUnitInsertDTO insertDTO = new ItemUnitInsertDTO();
        insertDTO.setItemId(2L);
        insertDTO.setAssetCode("PS5-001");
        insertDTO.setConditionStatus("GOOD");

        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(unit);
        when(repository.saveAndFlush(unit))
                .thenThrow(new DataIntegrityViolationException("Código já existe"));

        assertThrows(DatabaseException.class, () -> service.insert(insertDTO));

        verify(stockBalanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateShouldUpdateItemUnitWithoutChangingRentalStatus() {
        ItemUnitUpdateDTO updateDTO = new ItemUnitUpdateDTO();
        updateDTO.setItemId(2L);
        updateDTO.setAssetCode("PS5-001 atualizado");
        updateDTO.setConditionStatus("GOOD");

        unit.setStatus("RESERVED");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.saveAndFlush(unit)).thenReturn(unit);
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.update(1L, updateDTO);

        verify(mapper).updateEntity(unit, updateDTO);
        assertEquals(unitDTO, resultado);
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        assertEquals("RESERVED", unit.getStatus());
        assertEquals(true, unit.getActive());
        assertEquals(item, unit.getItem());
        verify(repository).saveAndFlush(unit);
    }

    @Test
    void updateShouldThrowExceptionWhenItemIsChanged() {
        ItemUnitUpdateDTO updateDTO = new ItemUnitUpdateDTO();
        updateDTO.setItemId(3L);
        updateDTO.setAssetCode("PS5-001 atualizado");
        updateDTO.setConditionStatus("GOOD");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));

        assertThrows(DatabaseException.class, () -> service.update(1L, updateDTO));

        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deleteShouldDeleteItemUnitAndUpdateStockBalance() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(stockBalanceRepository.findByItemId(2L)).thenReturn(Optional.of(balance));
        when(repository.countByItemIdAndActiveTrue(2L)).thenReturn(3L);
        when(repository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(repository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.delete(1L);

        verify(repository).delete(unit);
        verify(repository).flush();
        verify(stockBalanceRepository).saveAndFlush(balance);
        assertEquals(3, balance.getTotalQuantity());
        assertEquals(1, balance.getReservedQuantity());
        assertEquals(1, balance.getUnavailableQuantity());
        assertEquals(3, balance.getMinimumQuantity());
        assertEquals("Usuário Teste", balance.getUpdatedBy());
    }

    @Test
    void deleteShouldThrowExceptionWhenItemUnitHasRelatedRecords() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        doThrow(new DataIntegrityViolationException("Unidade possui vínculos")).when(repository).flush();

        assertThrows(DatabaseException.class, () -> service.delete(1L));

        verify(stockBalanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteAllShouldDeleteSelectedItemUnitsAndUpdateStockBalance() {
        ItemUnit secondUnit = new ItemUnit();
        secondUnit.setId(3L);
        secondUnit.setItem(item);
        secondUnit.setStatus("AVAILABLE");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(repository.findByIdForUpdate(3L)).thenReturn(Optional.of(secondUnit));
        when(stockBalanceRepository.findByItemId(2L)).thenReturn(Optional.of(balance));
        when(repository.countByItemIdAndActiveTrue(2L)).thenReturn(2L);
        when(repository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(repository.countUnavailableByItemId(2L)).thenReturn(1L);

        service.deleteAll(Arrays.asList(1L, 3L));

        verify(repository).deleteAll(Arrays.asList(unit, secondUnit));
        verify(repository).flush();
        verify(stockBalanceRepository).saveAndFlush(balance);
        assertEquals(2, balance.getTotalQuantity());
        assertEquals(1, balance.getReservedQuantity());
        assertEquals(1, balance.getUnavailableQuantity());
        assertEquals(3, balance.getMinimumQuantity());
    }

    @Test
    void deleteAllShouldThrowExceptionWhenSelectedUnitIsReserved() {
        ItemUnit reservedUnit = new ItemUnit();
        reservedUnit.setId(3L);
        reservedUnit.setStatus("RESERVED");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(repository.findByIdForUpdate(3L)).thenReturn(Optional.of(reservedUnit));

        assertThrows(DatabaseException.class,
                () -> service.deleteAll(Arrays.asList(1L, 3L)));

        verify(repository, never()).deleteAll(any());
        verify(stockBalanceRepository, never()).saveAndFlush(any());
    }

    @Test
    void changeActiveStatusShouldDeactivateItemUnitAndUpdateStockBalance() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(stockBalanceRepository.findByItemId(2L)).thenReturn(Optional.of(balance));
        when(repository.countByItemIdAndActiveTrue(2L)).thenReturn(3L);
        when(repository.countByItemIdAndStatusAndActiveTrue(2L, "RESERVED")).thenReturn(1L);
        when(repository.countUnavailableByItemId(2L)).thenReturn(1L);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.changeActiveStatus(1L, false);

        assertEquals(false, unit.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        verify(repository).saveAndFlush(unit);
        verify(stockBalanceRepository).saveAndFlush(balance);
        assertEquals(3, balance.getTotalQuantity());
        assertEquals(1, balance.getReservedQuantity());
        assertEquals(1, balance.getUnavailableQuantity());
        assertEquals(3, balance.getMinimumQuantity());
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenItemUnitIsRented() {
        unit.setStatus("RENTED");
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));

        assertThrows(DatabaseException.class, () -> service.changeActiveStatus(1L, false));

        assertEquals(true, unit.getActive());
        verify(repository, never()).saveAndFlush(any());
        verify(stockBalanceRepository, never()).saveAndFlush(any());
    }

}
