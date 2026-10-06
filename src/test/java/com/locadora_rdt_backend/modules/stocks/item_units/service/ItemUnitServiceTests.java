package com.locadora_rdt_backend.modules.stocks.item_units.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitInsertDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitStatusUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.dto.ItemUnitUpdateDTO;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.mapper.ItemUnitMapper;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import com.locadora_rdt_backend.modules.stocks.stock_movements.repository.StockMovementRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    private AuthenticationFacade authenticationFacade;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private ItemUnitServiceImpl service;

    private Item item;
    private ItemUnit unit;
    private ItemUnitDTO unitDTO;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(3L);
        category.setName("Ferramentas");
        category.setActive(true);

        item = new Item();
        item.setId(2L);
        item.setName("Furadeira");
        item.setCategory(category);
        item.setActive(true);

        unit = new ItemUnit();
        unit.setId(1L);
        unit.setItem(item);
        unit.setAssetCode("ITEM-2-1234abcd");
        unit.setStatus(ItemUnitStatus.AVAILABLE);
        unit.setConditionStatus(ItemUnitCondition.GOOD);
        unit.setActive(true);

        unitDTO = new ItemUnitDTO();
        unitDTO.setId(1L);
        unitDTO.setAssetCode("ITEM-2-1234abcd");
        unitDTO.setStatus(ItemUnitStatus.AVAILABLE);
        unitDTO.setConditionStatus(ItemUnitCondition.GOOD);
    }

    @Test
    void findAllPagedShouldReturnPageOfItemUnits() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ItemUnit> units = new PageImpl<>(Collections.singletonList(unit));

        when(itemRepository.existsById(2L)).thenReturn(true);
        when(repository.find("Furadeira", 2L, 1, pageRequest)).thenReturn(units);
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        Page<ItemUnitDTO> resultado = service.findAllPaged("  Furadeira  ", 2L, true, pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("ITEM-2-1234abcd", resultado.getContent().get(0).getAssetCode());
        verify(repository).find("Furadeira", 2L, 1, pageRequest);
    }

    @Test
    void findAllPagedShouldThrowExceptionWhenItemDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(itemRepository.existsById(2L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findAllPaged("Furadeira", 2L, true, pageRequest));

        assertEquals(ItemConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(repository, never()).find(any(), any(), any(), any());
    }

    @Test
    void findByIdShouldReturnItemUnit() {
        when(repository.findById(1L)).thenReturn(Optional.of(unit));
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("ITEM-2-1234abcd", resultado.getAssetCode());
    }

    @Test
    void findByIdShouldThrowExceptionWhenItemUnitDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(ItemUnitConstants.ITEM_UNIT_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void insertShouldSaveItemUnitAndRegisterEntry() {
        ItemUnitInsertDTO insertDTO = new ItemUnitInsertDTO();
        insertDTO.setItemId(2L);
        insertDTO.setConditionStatus(ItemUnitCondition.GOOD);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(unit);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.saveAndFlush(unit)).thenReturn(unit);
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.insert(insertDTO);

        assertEquals(unitDTO, resultado);
        assertEquals(item, unit.getItem());
        assertTrue(unit.getAssetCode().matches("ITEM-2-[0-9a-f]{8}"));
        assertEquals("Usuário Teste", unit.getCreatedBy());
        verify(repository).saveAndFlush(unit);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(item, movement.getItem());
        assertEquals(unit, movement.getItemUnit());
        assertEquals(StockMovementType.ENTRY, movement.getType());
        assertEquals(1, movement.getQuantity());
        assertEquals("Cadastro da unidade " + unit.getAssetCode(), movement.getReason());
        assertNull(movement.getPreviousStatus());
        assertEquals(ItemUnitStatus.AVAILABLE, movement.getNewStatus());
        assertEquals("Usuário Teste", movement.getCreatedBy());
    }

    @Test
    void insertShouldThrowExceptionWhenDatabaseIntegrityIsViolated() {
        ItemUnitInsertDTO insertDTO = new ItemUnitInsertDTO();
        insertDTO.setItemId(2L);
        insertDTO.setConditionStatus(ItemUnitCondition.GOOD);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(unit);
        when(repository.saveAndFlush(unit))
                .thenThrow(new DataIntegrityViolationException("Erro no banco"));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(ItemUnitConstants.DATABASE_INTEGRITY_VIOLATION, exception.getMessage());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void updateShouldUpdateItemUnitAndPreserveAssetCode() {
        ItemUnitUpdateDTO updateDTO = new ItemUnitUpdateDTO();
        updateDTO.setItemId(2L);
        updateDTO.setConditionStatus(ItemUnitCondition.GOOD);
        updateDTO.setNotes("Unidade revisada");

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.saveAndFlush(unit)).thenReturn(unit);
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.update(1L, updateDTO);

        assertEquals(unitDTO, resultado);
        assertEquals("ITEM-2-1234abcd", unit.getAssetCode());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        verify(itemRepository).findByItemUnitIdForUpdate(1L);
        verify(mapper).updateEntity(unit, updateDTO);
        verify(repository).saveAndFlush(unit);
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowExceptionWhenItemUnitDoesNotExist() {
        ItemUnitUpdateDTO updateDTO = new ItemUnitUpdateDTO();
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.update(1L, updateDTO));

        assertEquals(ItemUnitConstants.ITEM_UNIT_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).updateEntity(any(), any());
        verify(repository, never()).saveAndFlush(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void deleteShouldDeactivateItemUnitAndRegisterExit() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.delete(1L);

        assertFalse(unit.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        verify(repository).saveAndFlush(unit);
        verify(repository, never()).deleteById(any());

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(unit, movement.getItemUnit());
        assertEquals(StockMovementType.EXIT, movement.getType());
        assertEquals(1, movement.getQuantity());
        assertEquals("Baixa da unidade ITEM-2-1234abcd", movement.getReason());
        assertEquals("Usuário Teste", movement.getCreatedBy());
    }

    @Test
    void deleteShouldThrowExceptionWhenItemUnitDoesNotExist() {
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.delete(1L));

        assertEquals(ItemUnitConstants.ITEM_UNIT_NOT_FOUND, exception.getMessage());
        verify(repository, never()).saveAndFlush(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void deleteAllShouldDeactivateAllItemUnitsAndRegisterExits() {
        ItemUnit segundaUnidade = new ItemUnit();
        segundaUnidade.setId(4L);
        segundaUnidade.setItem(item);
        segundaUnidade.setAssetCode("ITEM-2-5678abcd");
        segundaUnidade.setStatus(ItemUnitStatus.AVAILABLE);
        segundaUnidade.setActive(true);
        List<Long> ids = Arrays.asList(1L, 4L);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(repository.findByIdForUpdate(4L)).thenReturn(Optional.of(segundaUnidade));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.deleteAll(ids);

        assertFalse(unit.getActive());
        assertFalse(segundaUnidade.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        assertEquals("Usuário Teste", segundaUnidade.getUpdatedBy());
        verify(itemRepository).findByItemUnitIdsForUpdate(ids);
        verify(repository).saveAndFlush(unit);
        verify(repository).saveAndFlush(segundaUnidade);
        verify(repository, never()).deleteById(any());

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository, times(2)).save(captor.capture());
        List<StockMovement> movements = captor.getAllValues();

        assertEquals(unit, movements.get(0).getItemUnit());
        assertEquals(segundaUnidade, movements.get(1).getItemUnit());
        assertEquals(StockMovementType.EXIT, movements.get(0).getType());
        assertEquals(StockMovementType.EXIT, movements.get(1).getType());
        assertEquals(1, movements.get(0).getQuantity());
        assertEquals(1, movements.get(1).getQuantity());
    }

    @Test
    void deleteAllShouldThrowExceptionWhenIdListIsEmpty() {
        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.deleteAll(Collections.emptyList()));

        assertEquals(ItemUnitConstants.EMPTY_ID_LIST, exception.getMessage());
        verify(itemRepository, never()).findByItemUnitIdsForUpdate(any());
        verify(repository, never()).saveAndFlush(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void changeActiveStatusShouldReactivateItemUnitAndRegisterEntry() {
        unit.setActive(false);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");

        service.changeActiveStatus(1L, true);

        assertTrue(unit.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        verify(repository).saveAndFlush(unit);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(unit, movement.getItemUnit());
        assertEquals(StockMovementType.ENTRY, movement.getType());
        assertEquals("Reativação da unidade ITEM-2-1234abcd", movement.getReason());
        assertEquals("Usuário Teste", movement.getCreatedBy());
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenItemIsInactive() {
        unit.setActive(false);
        item.setActive(false);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.changeActiveStatus(1L, true));

        assertEquals(ItemUnitConstants.INACTIVE_ITEM, exception.getMessage());
        assertFalse(unit.getActive());
        verify(repository, never()).saveAndFlush(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void updateStatusShouldChangeStatusAndRegisterMovement() {
        ItemUnitStatusUpdateDTO statusDTO = new ItemUnitStatusUpdateDTO();
        statusDTO.setStatus(ItemUnitStatus.UNAVAILABLE);
        statusDTO.setReason("Inspeção da unidade");
        unitDTO.setStatus(ItemUnitStatus.UNAVAILABLE);

        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.updateStatus(1L, statusDTO);

        assertEquals(unitDTO, resultado);
        assertEquals(ItemUnitStatus.UNAVAILABLE, unit.getStatus());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        verify(repository).saveAndFlush(unit);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(unit, movement.getItemUnit());
        assertEquals(StockMovementType.STATUS_CHANGE, movement.getType());
        assertEquals(ItemUnitStatus.AVAILABLE, movement.getPreviousStatus());
        assertEquals(ItemUnitStatus.UNAVAILABLE, movement.getNewStatus());
        assertEquals("Inspeção da unidade", movement.getReason());
        assertEquals("Usuário Teste", movement.getCreatedBy());
    }

    @Test
    void updateStatusShouldThrowExceptionWhenItemUnitIsInactive() {
        unit.setActive(false);
        ItemUnitStatusUpdateDTO statusDTO = new ItemUnitStatusUpdateDTO();
        statusDTO.setStatus(ItemUnitStatus.UNAVAILABLE);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.updateStatus(1L, statusDTO));

        assertEquals(ItemUnitConstants.INACTIVE_UNIT, exception.getMessage());
        assertEquals(ItemUnitStatus.AVAILABLE, unit.getStatus());
        verify(repository, never()).saveAndFlush(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void changeMaintenanceStatusShouldPlaceItemUnitInMaintenance() {
        unitDTO.setStatus(ItemUnitStatus.MAINTENANCE);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(mapper.toDTO(unit)).thenReturn(unitDTO);

        ItemUnitDTO resultado = service.changeMaintenanceStatus(1L, true);

        assertEquals(unitDTO, resultado);
        assertEquals(ItemUnitStatus.MAINTENANCE, unit.getStatus());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        verify(repository).saveAndFlush(unit);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(unit, movement.getItemUnit());
        assertEquals(StockMovementType.STATUS_CHANGE, movement.getType());
        assertEquals(ItemUnitStatus.AVAILABLE, movement.getPreviousStatus());
        assertEquals(ItemUnitStatus.MAINTENANCE, movement.getNewStatus());
        assertEquals("Alteração de manutenção da unidade ITEM-2-1234abcd", movement.getReason());
    }

    @Test
    void changeMaintenanceStatusShouldThrowExceptionWhenStatusIsInvalid() {
        unit.setStatus(ItemUnitStatus.LOST);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.changeMaintenanceStatus(1L, true));

        assertEquals(ItemUnitConstants.INVALID_MAINTENANCE_CHANGE, exception.getMessage());
        assertEquals(ItemUnitStatus.LOST, unit.getStatus());
        verify(repository, never()).saveAndFlush(any());
        verify(stockMovementRepository, never()).save(any());
    }
}
