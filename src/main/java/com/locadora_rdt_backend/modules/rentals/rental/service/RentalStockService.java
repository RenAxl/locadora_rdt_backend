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
import com.locadora_rdt_backend.modules.stocks.items.repository.ItemRepository;
import com.locadora_rdt_backend.modules.stocks.stock_balances.model.StockBalance;
import com.locadora_rdt_backend.modules.stocks.stock_balances.repository.StockBalanceRepository;
import com.locadora_rdt_backend.modules.stocks.stock_movements.enums.StockMovementType;
import com.locadora_rdt_backend.modules.stocks.stock_movements.model.StockMovement;
import com.locadora_rdt_backend.modules.stocks.stock_movements.repository.StockMovementRepository;
import com.locadora_rdt_backend.shared.security.AuthenticationFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RentalStockService {

    private final ItemRepository inventoryItemRepository;
    private final RentalUnitRepository itemUnitRepository;
    private final RentalItemUnitRepository rentalItemUnitRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final AuthenticationFacade authenticationFacade;

    public RentalStockService(
            ItemRepository inventoryItemRepository,
            RentalUnitRepository itemUnitRepository,
            RentalItemUnitRepository rentalItemUnitRepository,
            StockBalanceRepository stockBalanceRepository,
            StockMovementRepository stockMovementRepository,
            AuthenticationFacade authenticationFacade
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.itemUnitRepository = itemUnitRepository;
        this.rentalItemUnitRepository = rentalItemUnitRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.authenticationFacade = authenticationFacade;
    }

    @Transactional
    public void reserveUnits(Rental rental, List<RentalItem> rentalItems) {

        for (RentalItem rentalItem : rentalItems) {
            reserveItemUnits(rentalItem);
            registerStockMovement(rental, rentalItem, RentalConstants.RESERVATION_REASON);
        }
    }

    @Transactional
    public void returnReservedUnits(Rental rental, List<RentalItem> rentalItems,
            List<RentalItemUnit> units, Instant returnDate) {

        validateReservedUnitQuantity(rentalItems, units);
        String username = authenticationFacade.getAuthenticatedUsername();

        for (RentalItemUnit rentalItemUnit : units) {
            if (rentalItemUnit.getStatus() != RentalItemUnitStatus.RESERVED) {
                throw new IllegalArgumentException(
                        RentalConstants.ALL_UNITS_MUST_BE_RESERVED);
            }
            ItemUnit itemUnit = rentalItemUnit.getItemUnit();
            if (itemUnit.getStatus() != ItemUnitStatus.UNAVAILABLE) {
                throw new IllegalArgumentException(RentalConstants.UNIT_MESSAGE_PREFIX + itemUnit.getAssetCode()
                        + RentalConstants.UNIT_NOT_RESERVED_SUFFIX);
            }
            itemUnit.setStatus(ItemUnitStatus.AVAILABLE);
            itemUnit.setUpdatedBy(username);
            rentalItemUnit.setStatus(RentalItemUnitStatus.RETURNED);
            rentalItemUnit.setDeliveredAt(returnDate);
            rentalItemUnit.setReturnedAt(returnDate);
            rentalItemUnit.setUpdatedBy(username);
            itemUnitRepository.save(itemUnit);
            rentalItemUnitRepository.save(rentalItemUnit);
        }

        for (RentalItem rentalItem : rentalItems) {
            updateStockBalanceAudit(rentalItem.getItem().getId());
            registerStockMovement(rental, rentalItem, RentalConstants.RETURN_REASON);
        }
    }

    @Transactional
    public void releaseUnitsForDeletion(Rental rental, List<RentalItem> rentalItems,
            List<RentalItemUnit> linkedUnits) {

        String username = authenticationFacade.getAuthenticatedUsername();
        for (RentalItemUnit linkedUnit : linkedUnits) {
            ItemUnit itemUnit = linkedUnit.getItemUnit();
            itemUnit.setStatus(ItemUnitStatus.AVAILABLE);
            itemUnit.setUpdatedBy(username);
            itemUnitRepository.save(itemUnit);
        }
        itemUnitRepository.flush();

        Set<Long> itemIds = new HashSet<>();
        for (RentalItem rentalItem : rentalItems) {
            itemIds.add(rentalItem.getItem().getId());
            registerStockMovement(rental, rentalItem, RentalConstants.CANCELLATION_REASON);
        }
        for (Long itemId : itemIds) {
            updateStockBalanceAudit(itemId);
        }
        stockBalanceRepository.flush();
    }

    private void reserveItemUnits(RentalItem rentalItem) {

        inventoryItemRepository.findByIdForUpdate(rentalItem.getItem().getId());
        int requestedQuantity = rentalItem.getQuantity();
        List<ItemUnit> availableUnits = itemUnitRepository.findAvailableForReservation(
                rentalItem.getItem().getId(), requestedQuantity);

        if (availableUnits.size() < requestedQuantity) {
            throw new IllegalArgumentException(RentalConstants.ITEM_QUANTITY_UNAVAILABLE_PREFIX
                    + rentalItem.getItem().getName() + ". Disponíveis: " + availableUnits.size()
                    + ", solicitadas: " + requestedQuantity + ".");
        }

        String username = authenticationFacade.getAuthenticatedUsername();
        Instant now = Instant.now();
        for (ItemUnit itemUnit : availableUnits) {
            validateAvailableUnit(rentalItem, itemUnit);
            itemUnit.setStatus(ItemUnitStatus.UNAVAILABLE);
            itemUnit.setUpdatedBy(username);
            itemUnitRepository.save(itemUnit);

            RentalItemUnit rentalItemUnit = new RentalItemUnit();
            rentalItemUnit.setRentalItem(rentalItem);
            rentalItemUnit.setItemUnit(itemUnit);
            rentalItemUnit.setStatus(RentalItemUnitStatus.RESERVED);
            rentalItemUnit.setReservedAt(now);
            rentalItemUnit.setCreatedBy(username);
            rentalItemUnitRepository.save(rentalItemUnit);
        }

        long linkedQuantity = rentalItemUnitRepository.countByRentalItemIdAndStatusIn(
                rentalItem.getId(), RentalConstants.ACTIVE_UNIT_STATUSES);
        if (linkedQuantity != requestedQuantity) {
            throw new IllegalArgumentException(
                    RentalConstants.LINKED_UNITS_QUANTITY_INVALID);
        }
        updateStockBalanceAudit(rentalItem.getItem().getId());
    }

    private void registerStockMovement(Rental rental, RentalItem rentalItem, String reason) {

        StockMovement movement = new StockMovement();
        movement.setItem(rentalItem.getItem());
        movement.setType(StockMovementType.STATUS_CHANGE);
        movement.setQuantity(rentalItem.getQuantity());
        movement.setReason(reason + " Locação " + rental.getRentalNumber() + " (Id: " + rental.getId() + ").");
        movement.setCreatedBy(authenticationFacade.getAuthenticatedUsername());
        stockMovementRepository.save(movement);
    }

    private void validateAvailableUnit(RentalItem rentalItem, ItemUnit itemUnit) {

        if (!itemUnit.getItem().getId().equals(rentalItem.getItem().getId())) {
            throw new IllegalArgumentException(RentalConstants.UNIT_NOT_FROM_RENTAL_ITEM);
        }
        if (itemUnit.getStatus() != ItemUnitStatus.AVAILABLE || !Boolean.TRUE.equals(itemUnit.getActive())) {
            throw new IllegalArgumentException(RentalConstants.UNIT_MESSAGE_PREFIX + itemUnit.getAssetCode()
                    + RentalConstants.UNIT_NOT_AVAILABLE_SUFFIX);
        }
        if (rentalItemUnitRepository.existsByItemUnitIdAndStatusIn(itemUnit.getId(), RentalConstants.ACTIVE_UNIT_STATUSES)) {
            throw new IllegalArgumentException(RentalConstants.UNIT_MESSAGE_PREFIX + itemUnit.getAssetCode()
                    + RentalConstants.UNIT_ALREADY_LINKED_SUFFIX);
        }
    }

    private void updateStockBalanceAudit(Long itemId) {

        Optional<StockBalance> stockBalanceOptional = stockBalanceRepository.findByItemId(itemId);
        if (!stockBalanceOptional.isPresent()) {
            throw new IllegalArgumentException(RentalConstants.STOCK_BALANCE_NOT_FOUND);
        }
        StockBalance stockBalance = stockBalanceOptional.get();
        stockBalance.setUpdatedBy(authenticationFacade.getAuthenticatedUsername());
        stockBalanceRepository.save(stockBalance);
    }

    private void validateReservedUnitQuantity(List<RentalItem> rentalItems, List<RentalItemUnit> units) {

        int expectedQuantity = 0;
        for (RentalItem rentalItem : rentalItems) {
            expectedQuantity = expectedQuantity + rentalItem.getQuantity();
        }
        if (units.size() != expectedQuantity) {
            throw new IllegalArgumentException(
                    RentalConstants.RESERVED_UNITS_QUANTITY_INVALID);
        }
    }
}
