package com.locadora_rdt_backend.modules.rentals.rental.service;

import com.locadora_rdt_backend.modules.rentals.rental.constants.RentalConstants;
import com.locadora_rdt_backend.modules.rentals.rental.model.Rental;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItem;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItemUnit;
import com.locadora_rdt_backend.modules.rentals.rental.model.RentalItemUnitStatus;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalItemUnitRepository;
import com.locadora_rdt_backend.modules.rentals.rental.repository.RentalUnitRepository;
import com.locadora_rdt_backend.modules.stocks.item_units.enums.ItemUnitStatus;
import com.locadora_rdt_backend.modules.stocks.item_units.model.ItemUnit;
import com.locadora_rdt_backend.modules.stocks.items.model.Item;
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
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

import java.time.Instant;
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
public class RentalStockServiceTests {

    @Mock
    private ItemRepository inventoryItemRepository;

    @Mock
    private RentalUnitRepository itemUnitRepository;

    @Mock
    private RentalItemUnitRepository rentalItemUnitRepository;

    @Mock
    private StockBalanceRepository stockBalanceRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @InjectMocks
    private RentalStockService service;

    private Rental rental;
    private Item item;
    private RentalItem rentalItem;
    private ItemUnit itemUnit;
    private RentalItemUnit rentalItemUnit;
    private StockBalance stockBalance;

    @BeforeEach
    void setUp() {
        rental = new Rental();
        rental.setId(1L);
        rental.setRentalNumber("LOC-1");

        item = new Item();
        item.setId(4L);
        item.setName("Furadeira");

        rentalItem = new RentalItem();
        rentalItem.setId(5L);
        rentalItem.setRental(rental);
        rentalItem.setItem(item);
        rentalItem.setQuantity(1);

        itemUnit = new ItemUnit();
        itemUnit.setId(6L);
        itemUnit.setItem(item);
        itemUnit.setAssetCode("PAT-1");
        itemUnit.setStatus(ItemUnitStatus.AVAILABLE);
        itemUnit.setActive(true);

        rentalItemUnit = new RentalItemUnit();
        rentalItemUnit.setId(7L);
        rentalItemUnit.setRentalItem(rentalItem);
        rentalItemUnit.setItemUnit(itemUnit);
        rentalItemUnit.setStatus(RentalItemUnitStatus.RESERVED);

        stockBalance = new StockBalance();
        stockBalance.setId(8L);
        stockBalance.setItem(item);
    }

    @Test
    void reserveUnitsShouldReserveUnitsAndRegisterStockMovement() {
        when(itemUnitRepository.findAvailableForReservation(4L, 1))
                .thenReturn(Collections.singletonList(itemUnit));
        when(rentalItemUnitRepository.countByRentalItemIdAndStatusIn(5L, RentalConstants.ACTIVE_UNIT_STATUSES))
                .thenReturn(1L);
        when(stockBalanceRepository.findByItemId(4L)).thenReturn(Optional.of(stockBalance));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");

        service.reserveUnits(rental, Collections.singletonList(rentalItem));

        ArgumentCaptor<RentalItemUnit> unitCaptor = ArgumentCaptor.forClass(RentalItemUnit.class);
        verify(rentalItemUnitRepository).save(unitCaptor.capture());
        RentalItemUnit savedUnit = unitCaptor.getValue();

        ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(movementCaptor.capture());
        StockMovement movement = movementCaptor.getValue();

        assertEquals(ItemUnitStatus.UNAVAILABLE, itemUnit.getStatus());
        assertEquals("joao@email.com", itemUnit.getUpdatedBy());
        assertEquals(rentalItem, savedUnit.getRentalItem());
        assertEquals(itemUnit, savedUnit.getItemUnit());
        assertEquals(RentalItemUnitStatus.RESERVED, savedUnit.getStatus());
        assertNotNull(savedUnit.getReservedAt());
        assertEquals("joao@email.com", savedUnit.getCreatedBy());
        assertEquals(item, movement.getItem());
        assertEquals(1, movement.getQuantity());
        assertEquals(StockMovementType.STATUS_CHANGE, movement.getType());
        assertEquals(RentalConstants.RESERVATION_REASON + " Locação LOC-1 (Id: 1).", movement.getReason());
        assertEquals("joao@email.com", movement.getCreatedBy());
        assertEquals("joao@email.com", stockBalance.getUpdatedBy());
        verify(inventoryItemRepository).findByIdForUpdate(4L);
        verify(rentalItemUnitRepository).existsByItemUnitIdAndStatusIn(6L, RentalConstants.ACTIVE_UNIT_STATUSES);
        verify(itemUnitRepository).save(itemUnit);
        verify(stockBalanceRepository).save(stockBalance);
    }

    @Test
    void reserveUnitsShouldThrowExceptionWhenQuantityIsUnavailable() {
        when(itemUnitRepository.findAvailableForReservation(4L, 1)).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.reserveUnits(rental, Collections.singletonList(rentalItem)));

        assertEquals(RentalConstants.ITEM_QUANTITY_UNAVAILABLE_PREFIX
                + "Furadeira. Disponíveis: 0, solicitadas: 1.", exception.getMessage());
        assertEquals(ItemUnitStatus.AVAILABLE, itemUnit.getStatus());
        verify(inventoryItemRepository).findByIdForUpdate(4L);
        verify(itemUnitRepository, never()).save(any());
        verify(rentalItemUnitRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void returnReservedUnitsShouldReturnUnitsAndRegisterStockMovement() {
        itemUnit.setStatus(ItemUnitStatus.UNAVAILABLE);
        Instant returnDate = Instant.parse("2026-10-08T12:00:00Z");
        when(stockBalanceRepository.findByItemId(4L)).thenReturn(Optional.of(stockBalance));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");

        service.returnReservedUnits(rental, Collections.singletonList(rentalItem),
                Collections.singletonList(rentalItemUnit), returnDate);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(ItemUnitStatus.AVAILABLE, itemUnit.getStatus());
        assertEquals("joao@email.com", itemUnit.getUpdatedBy());
        assertEquals(RentalItemUnitStatus.RETURNED, rentalItemUnit.getStatus());
        assertEquals(returnDate, rentalItemUnit.getDeliveredAt());
        assertEquals(returnDate, rentalItemUnit.getReturnedAt());
        assertEquals("joao@email.com", rentalItemUnit.getUpdatedBy());
        assertEquals(item, movement.getItem());
        assertEquals(1, movement.getQuantity());
        assertEquals(StockMovementType.STATUS_CHANGE, movement.getType());
        assertEquals(RentalConstants.RETURN_REASON + " Locação LOC-1 (Id: 1).", movement.getReason());
        assertEquals("joao@email.com", movement.getCreatedBy());
        assertEquals("joao@email.com", stockBalance.getUpdatedBy());
        verify(itemUnitRepository).save(itemUnit);
        verify(rentalItemUnitRepository).save(rentalItemUnit);
        verify(stockBalanceRepository).save(stockBalance);
    }

    @Test
    void returnReservedUnitsShouldThrowExceptionWhenReservedQuantityIsInvalid() {
        rentalItem.setQuantity(2);
        itemUnit.setStatus(ItemUnitStatus.UNAVAILABLE);
        Instant returnDate = Instant.parse("2026-10-08T12:00:00Z");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.returnReservedUnits(rental, Collections.singletonList(rentalItem),
                        Collections.singletonList(rentalItemUnit), returnDate));

        assertEquals(RentalConstants.RESERVED_UNITS_QUANTITY_INVALID, exception.getMessage());
        assertEquals(ItemUnitStatus.UNAVAILABLE, itemUnit.getStatus());
        assertEquals(RentalItemUnitStatus.RESERVED, rentalItemUnit.getStatus());
        verify(itemUnitRepository, never()).save(any());
        verify(rentalItemUnitRepository, never()).save(any());
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    void releaseUnitsForDeletionShouldReleaseUnitsAndRegisterStockMovement() {
        itemUnit.setStatus(ItemUnitStatus.UNAVAILABLE);
        when(stockBalanceRepository.findByItemId(4L)).thenReturn(Optional.of(stockBalance));
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");

        service.releaseUnitsForDeletion(rental, Collections.singletonList(rentalItem),
                Collections.singletonList(rentalItemUnit));

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();

        assertEquals(ItemUnitStatus.AVAILABLE, itemUnit.getStatus());
        assertEquals("joao@email.com", itemUnit.getUpdatedBy());
        assertEquals(item, movement.getItem());
        assertEquals(1, movement.getQuantity());
        assertEquals(StockMovementType.STATUS_CHANGE, movement.getType());
        assertEquals(RentalConstants.CANCELLATION_REASON + " Locação LOC-1 (Id: 1).", movement.getReason());
        assertEquals("joao@email.com", movement.getCreatedBy());
        assertEquals("joao@email.com", stockBalance.getUpdatedBy());
        verify(itemUnitRepository).save(itemUnit);
        verify(itemUnitRepository).flush();
        verify(stockBalanceRepository).save(stockBalance);
        verify(stockBalanceRepository).flush();
    }

    @Test
    void releaseUnitsForDeletionShouldThrowExceptionWhenStockBalanceDoesNotExist() {
        itemUnit.setStatus(ItemUnitStatus.UNAVAILABLE);
        when(stockBalanceRepository.findByItemId(4L)).thenReturn(Optional.empty());
        when(authenticationFacade.getAuthenticatedUsername()).thenReturn("joao@email.com");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.releaseUnitsForDeletion(rental, Collections.singletonList(rentalItem),
                        Collections.singletonList(rentalItemUnit)));

        assertEquals(RentalConstants.STOCK_BALANCE_NOT_FOUND, exception.getMessage());
        verify(stockBalanceRepository, never()).save(any());
        verify(stockBalanceRepository, never()).flush();
    }
}
