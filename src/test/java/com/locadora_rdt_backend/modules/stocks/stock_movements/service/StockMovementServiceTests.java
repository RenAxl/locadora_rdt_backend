package com.locadora_rdt_backend.modules.stocks.stock_movements.service;

import com.locadora_rdt_backend.common.exception.DatabaseException;
import com.locadora_rdt_backend.common.exception.ResourceNotFoundException;
import com.locadora_rdt_backend.modules.stocks.categories.model.Category;
import com.locadora_rdt_backend.modules.stocks.item_units.constants.ItemUnitConstants;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitCondition;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.item_units.repository.ItemUnitRepository;
import com.locadora_rdt_backend.modules.stocks.items.constants.ItemConstants;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_movements.constants.StockMovementConstants;
import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.dto.StockMovementInsertDTO;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.mapper.StockMovementMapper;
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
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
public class StockMovementServiceTests {

    @Mock
    private StockMovementRepository repository;

    @Mock
    private ItemUnitRepository itemUnitRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockMovementMapper mapper;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private StockMovementServiceImpl service;

    private Item item;
    private ItemUnit unit;
    private StockMovement movement;
    private StockMovementDTO movementDTO;
    private StockMovementInsertDTO insertDTO;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Ferramentas");
        category.setActive(true);

        item = new Item();
        item.setId(2L);
        item.setName("Furadeira");
        item.setCategory(category);
        item.setActive(true);

        unit = new ItemUnit();
        unit.setId(3L);
        unit.setItem(item);
        unit.setAssetCode("ITEM-2-1234abcd");
        unit.setStatus(ItemUnitStatus.AVAILABLE);
        unit.setConditionStatus(ItemUnitCondition.GOOD);
        unit.setActive(true);

        movement = new StockMovement();
        movement.setId(1L);
        movement.setItem(item);
        movement.setType(StockMovementType.ENTRY);
        movement.setQuantity(1);
        movement.setReason("Movimentação de teste");

        movementDTO = new StockMovementDTO();
        movementDTO.setId(1L);
        movementDTO.setItemId(2L);
        movementDTO.setItemName("Furadeira");
        movementDTO.setType(StockMovementType.ENTRY);
        movementDTO.setQuantity(1);

        insertDTO = new StockMovementInsertDTO();
        insertDTO.setItemId(2L);
        insertDTO.setType(StockMovementType.ENTRY);
        insertDTO.setQuantity(1);
        insertDTO.setReason("Movimentação de teste");
    }

    @Test
    void findAllPagedShouldReturnPageOfStockMovements() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<StockMovement> movements = new PageImpl<>(Collections.singletonList(movement));

        when(repository.find("Furadeira", pageRequest)).thenReturn(movements);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        Page<StockMovementDTO> resultado = service.findAllPaged("  Furadeira  ", pageRequest);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Furadeira", resultado.getContent().get(0).getItemName());
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
    void findByIdShouldReturnStockMovement() {
        when(repository.findById(1L)).thenReturn(Optional.of(movement));
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        StockMovementDTO resultado = service.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Furadeira", resultado.getItemName());
    }

    @Test
    void findByIdShouldThrowExceptionWhenStockMovementDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.findById(1L));

        assertEquals(StockMovementConstants.STOCK_MOVEMENT_NOT_FOUND, exception.getMessage());
        verify(mapper, never()).toDTO(any());
    }

    @Test
    void insertEntryShouldCreateAvailableUnitsAndSaveMovement() {
        insertDTO.setQuantity(2);
        movement.setQuantity(2);
        movementDTO.setQuantity(2);
        LocalDate purchaseDate = LocalDate.now();

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(movement)).thenReturn(movement);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        StockMovementDTO resultado = service.insert(insertDTO);

        assertEquals(movementDTO, resultado);
        assertEquals(item, movement.getItem());
        assertEquals("Usuário Teste", movement.getCreatedBy());
        assertNull(movement.getItemUnit());
        verify(repository).save(movement);

        ArgumentCaptor<ItemUnit> captor = ArgumentCaptor.forClass(ItemUnit.class);
        verify(itemUnitRepository, times(2)).save(captor.capture());
        List<ItemUnit> createdUnits = captor.getAllValues();

        for (ItemUnit createdUnit : createdUnits) {
            assertEquals(item, createdUnit.getItem());
            assertTrue(createdUnit.getAssetCode().matches("ITEM-2-[0-9a-f]{8}"));
            assertTrue(createdUnit.getActive());
            assertEquals(ItemUnitStatus.AVAILABLE, createdUnit.getStatus());
            assertEquals(ItemUnitCondition.GOOD, createdUnit.getConditionStatus());
            assertEquals(purchaseDate, createdUnit.getPurchaseDate());
            assertEquals(StockMovementConstants.MOVEMENT_CREATION_NOTE, createdUnit.getNotes());
            assertEquals("Usuário Teste", createdUnit.getCreatedBy());
        }
        assertNotEquals(createdUnits.get(0).getAssetCode(), createdUnits.get(1).getAssetCode());
    }

    @Test
    void insertEntryShouldThrowExceptionWhenUnitRepositoryFails() {
        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.save(any(ItemUnit.class)))
                .thenThrow(new DataAccessResourceFailureException("Erro no banco"));

        assertThrows(DataAccessResourceFailureException.class, () -> service.insert(insertDTO));

        verify(repository, never()).save(any());
    }

    @Test
    void insertShouldThrowExceptionWhenQuantityIsInvalid() {
        insertDTO.setQuantity(0);

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(StockMovementConstants.INVALID_QUANTITY, exception.getMessage());
        verify(itemRepository, never()).findByIdForUpdate(any());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertShouldThrowExceptionWhenItemDoesNotExist() {
        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.insert(insertDTO));

        assertEquals(ItemConstants.ITEM_NOT_FOUND, exception.getMessage());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertEntryShouldThrowExceptionWhenCategoryIsInactive() {
        item.getCategory().setActive(false);
        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(ItemUnitConstants.INACTIVE_ITEM, exception.getMessage());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertExitShouldDeactivateSelectedUnitAndSaveMovement() {
        insertDTO.setType(StockMovementType.EXIT);
        insertDTO.setItemUnitId(3L);
        movement.setType(StockMovementType.EXIT);
        movementDTO.setType(StockMovementType.EXIT);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(movement)).thenReturn(movement);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        StockMovementDTO resultado = service.insert(insertDTO);

        assertEquals(movementDTO, resultado);
        assertFalse(unit.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        assertEquals(unit, movement.getItemUnit());
        assertEquals("Usuário Teste", movement.getCreatedBy());
        verify(itemUnitRepository).save(unit);
        verify(itemUnitRepository, never()).deleteById(any());
        verify(repository).save(movement);
    }

    @Test
    void insertExitShouldThrowExceptionWhenStockIsInsufficient() {
        insertDTO.setType(StockMovementType.EXIT);
        insertDTO.setQuantity(2);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.findAvailableByItemIdForUpdate(2L, 2))
                .thenReturn(Collections.singletonList(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(StockMovementConstants.INSUFFICIENT_STOCK, exception.getMessage());
        assertTrue(unit.getActive());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertAdjustmentShouldCreateUnitsForTheDifference() {
        insertDTO.setType(StockMovementType.ADJUSTMENT);
        insertDTO.setQuantity(4);
        movement.setType(StockMovementType.ADJUSTMENT);
        movement.setQuantity(4);
        movementDTO.setType(StockMovementType.ADJUSTMENT);
        movementDTO.setQuantity(4);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(2L);
        when(repository.save(movement)).thenReturn(movement);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        StockMovementDTO resultado = service.insert(insertDTO);

        assertEquals(movementDTO, resultado);
        ArgumentCaptor<ItemUnit> captor = ArgumentCaptor.forClass(ItemUnit.class);
        verify(itemUnitRepository, times(2)).save(captor.capture());
        List<ItemUnit> createdUnits = captor.getAllValues();

        assertTrue(createdUnits.get(0).getActive());
        assertTrue(createdUnits.get(1).getActive());
        assertEquals(item, createdUnits.get(0).getItem());
        assertEquals(item, createdUnits.get(1).getItem());
        verify(repository).save(movement);
    }

    @Test
    void insertAdjustmentShouldDeactivateUnitsForTheDifference() {
        insertDTO.setType(StockMovementType.ADJUSTMENT);
        movement.setType(StockMovementType.ADJUSTMENT);
        movementDTO.setType(StockMovementType.ADJUSTMENT);

        ItemUnit segundaUnidade = new ItemUnit();
        segundaUnidade.setId(4L);
        segundaUnidade.setItem(item);
        segundaUnidade.setStatus(ItemUnitStatus.AVAILABLE);
        segundaUnidade.setActive(true);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(3L);
        when(itemUnitRepository.findAvailableByItemIdForUpdate(2L, 2))
                .thenReturn(Arrays.asList(unit, segundaUnidade));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(movement)).thenReturn(movement);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        StockMovementDTO resultado = service.insert(insertDTO);

        assertEquals(movementDTO, resultado);
        assertFalse(unit.getActive());
        assertFalse(segundaUnidade.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        assertEquals("Usuário Teste", segundaUnidade.getUpdatedBy());
        assertNull(movement.getItemUnit());
        verify(itemUnitRepository).save(unit);
        verify(itemUnitRepository).save(segundaUnidade);
        verify(repository).save(movement);
    }

    @Test
    void insertAdjustmentShouldThrowExceptionWhenAvailableStockIsInsufficient() {
        insertDTO.setType(StockMovementType.ADJUSTMENT);
        insertDTO.setQuantity(0);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.countByItemIdAndActiveTrue(2L)).thenReturn(2L);
        when(itemUnitRepository.findAvailableByItemIdForUpdate(2L, 2))
                .thenReturn(Collections.singletonList(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(StockMovementConstants.INSUFFICIENT_STOCK, exception.getMessage());
        assertTrue(unit.getActive());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertStatusChangeShouldUpdateUnitAndSaveHistory() {
        insertDTO.setType(StockMovementType.STATUS_CHANGE);
        insertDTO.setItemUnitId(3L);
        insertDTO.setStatus(ItemUnitStatus.UNAVAILABLE);
        insertDTO.setReason("");
        movement.setType(StockMovementType.STATUS_CHANGE);
        movement.setReason("");
        movementDTO.setType(StockMovementType.STATUS_CHANGE);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(unit));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("Usuário Teste");
        when(repository.save(movement)).thenReturn(movement);
        when(mapper.toDTO(movement)).thenReturn(movementDTO);

        StockMovementDTO resultado = service.insert(insertDTO);

        assertEquals(movementDTO, resultado);
        assertEquals(ItemUnitStatus.UNAVAILABLE, unit.getStatus());
        assertTrue(unit.getActive());
        assertEquals("Usuário Teste", unit.getUpdatedBy());
        assertEquals(unit, movement.getItemUnit());
        assertEquals(ItemUnitStatus.AVAILABLE, movement.getPreviousStatus());
        assertEquals(ItemUnitStatus.UNAVAILABLE, movement.getNewStatus());
        assertEquals("Status alterado de AVAILABLE para UNAVAILABLE", movement.getReason());
        assertEquals("Usuário Teste", movement.getCreatedBy());
        verify(itemUnitRepository).save(unit);
        verify(repository).save(movement);
    }

    @Test
    void insertStatusChangeShouldThrowExceptionWhenUnitIsInactive() {
        insertDTO.setType(StockMovementType.STATUS_CHANGE);
        insertDTO.setItemUnitId(3L);
        insertDTO.setStatus(ItemUnitStatus.UNAVAILABLE);
        unit.setActive(false);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(ItemUnitConstants.INACTIVE_UNIT, exception.getMessage());
        assertEquals(ItemUnitStatus.AVAILABLE, unit.getStatus());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertStatusChangeShouldThrowExceptionWhenUnitDoesNotExist() {
        insertDTO.setType(StockMovementType.STATUS_CHANGE);
        insertDTO.setItemUnitId(3L);
        insertDTO.setStatus(ItemUnitStatus.UNAVAILABLE);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.findByIdForUpdate(3L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> service.insert(insertDTO));

        assertEquals(ItemUnitConstants.ITEM_UNIT_NOT_FOUND, exception.getMessage());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }

    @Test
    void insertStatusChangeShouldThrowExceptionWhenUnitBelongsToAnotherItem() {
        insertDTO.setType(StockMovementType.STATUS_CHANGE);
        insertDTO.setItemUnitId(3L);
        insertDTO.setStatus(ItemUnitStatus.UNAVAILABLE);

        Item outroItem = new Item();
        outroItem.setId(5L);
        unit.setItem(outroItem);

        when(itemRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(item));
        when(mapper.toEntity(insertDTO)).thenReturn(movement);
        when(itemUnitRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(unit));

        DatabaseException exception = assertThrows(DatabaseException.class,
                () -> service.insert(insertDTO));

        assertEquals(StockMovementConstants.UNIT_ITEM_MISMATCH, exception.getMessage());
        assertEquals(ItemUnitStatus.AVAILABLE, unit.getStatus());
        verify(itemUnitRepository, never()).save(any());
        verify(repository, never()).save(any());
    }
}
